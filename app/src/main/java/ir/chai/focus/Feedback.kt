package ir.chai.focus

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.Calendar

/**
 * بازخورد واقعی پایان جلسه: صدا + لرزش + اعلان.
 * تنظیمات «صدای پایان جلسه» و «لرزش پایان جلسه» دقیقاً همین‌جا اجرا می‌شوند.
 */
object Feedback {

    const val CHANNEL_FINISH = "chai_finish"
    const val CHANNEL_REMINDER = "chai_reminder"

    /** پایان جلسه — صدا و لرزش (طبق تنظیمات کاربر) */
    fun sessionFinished(ctx: Context) {
        if (Prefs.vibrateOn(ctx)) vibrate(ctx)
        if (Prefs.soundOn(ctx)) {
            ensureChannels(ctx)
            notify(
                ctx, CHANNEL_FINISH, 2001,
                "چای تمام شد ☕",
                "${TimeFmt.humanHours(Prefs.lastSessionSec(ctx))} تمرکز — آفرین!"
            )
        }
    }

    /** لرزش کوتاه و ملایم */
    fun vibrate(ctx: Context) {
        try {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vib.hasVibrator()) return
            val pattern = longArrayOf(0, 220, 120, 220)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // بی‌خیال
        }
    }

    /** کانال‌های اعلان (صدای پایان جلسه و یادآور) */
    fun ensureChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val mgr = ctx.getSystemService(NotificationManager::class.java) ?: return
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val finish = NotificationChannel(
                CHANNEL_FINISH, "پایان جلسه", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "صدا و اعلان وقتی چای تمام می‌شود"
                enableVibration(true)
                setSound(sound, android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build())
            }
            val reminder = NotificationChannel(
                CHANNEL_REMINDER, "یادآور تمرکز", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "یادآوری روزانهٔ شروع تمرکز"
                setSound(sound, android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build())
            }
            mgr.createNotificationChannel(finish)
            mgr.createNotificationChannel(reminder)
        } catch (e: Exception) {
            // بی‌خیال
        }
    }

    /** ارسال اعلان */
    fun notify(ctx: Context, channel: String, id: Int, title: String, body: String) {
        try {
            ensureChannels(ctx)
            val mgr = ctx.getSystemService(NotificationManager::class.java) ?: return
            val pi = PendingIntent.getActivity(
                ctx, id,
                ctx.packageManager.getLaunchIntentForPackage(ctx.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val n = Notification.Builder(ctx, channel)
                .setSmallIcon(R.drawable.ic_chai_cup)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
            mgr.notify(id, n)
        } catch (e: Exception) {
            // بی‌خیال
        }
    }
}

/**
 * یادآور روزانه — واقعی، با AlarmManager.
 * هر روز ساعت انتخابی یک اعلان می‌فرستد.
 */
object ReminderScheduler {

    private const val REQUEST = 4200

    fun schedule(ctx: Context, hour: Int, minute: Int) {
        try {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent(ctx)
            )
        } catch (e: Exception) {
            // بی‌خیال
        }
    }

    fun cancel(ctx: Context) {
        try {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            am.cancel(pendingIntent(ctx))
        } catch (e: Exception) {
            // بی‌خیال
        }
    }

    private fun pendingIntent(ctx: Context): PendingIntent {
        val intent = Intent(ctx, ReminderReceiver::class.java).apply {
            action = "ir.chai.focus.REMINDER"
        }
        return PendingIntent.getBroadcast(
            ctx, REQUEST, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** گیرندهٔ یادآور — اعلان می‌فرستد و برای فردا دوباره تنظیم می‌شود */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Feedback.ensureChannels(context)
        Feedback.notify(
            context, Feedback.CHANNEL_REMINDER, 3001,
            "وقتشه یک فنجان چای ☕",
            "۳۰ دقیقه تمرکز — اپ‌های حواس‌پرت‌کن قفل می‌شوند"
        )
        if (Prefs.reminderOn(context)) {
            ReminderScheduler.schedule(context, Prefs.reminderHour(context), Prefs.reminderMinute(context))
        }
    }
}

/** بعد از ریستارت گوشی، یادآور دوباره تنظیم می‌شود */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (Prefs.reminderOn(context)) {
            ReminderScheduler.schedule(context, Prefs.reminderHour(context), Prefs.reminderMinute(context))
        }
    }
}
