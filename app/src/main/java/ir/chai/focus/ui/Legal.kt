package ir.chai.focus.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ═══════════════════════ متن قوانین ═══════════════════════ */

/** لینک‌های رسمی (نسخهٔ وب) */
object LegalLinks {
    const val PRIVACY = "https://raw.githack.com/ABOLFAZL-SAMADI283/chai/main/store/privacy-policy.html"
    const val TERMS = "https://raw.githack.com/ABOLFAZL-SAMADI283/chai/main/store/terms.html"
}

enum class LegalKind { TERMS, PRIVACY }

/** یک بخش از متن */
private data class Section(val title: String, val body: String)

private val TERMS_SECTIONS = listOf(
    Section(
        "۱. پذیرش شرایط",
        "با نصب و استفاده از اپ «چای» (به‌以下简称 «اپ»)، شما این شرایط را می‌پذیرید. " +
            "اگر با هر بندی مخالفت دارید، لطفاً اپ را نصب نکنید یا آن را حذف کنید."
    ),
    Section(
        "۲. سرویس چیست",
        "«چای» یک ابزار تمرکز است: هدف و مدت تمرکز را انتخاب می‌کنید و در طول جلسه، اپ‌های " +
            "حواس‌پرت‌کنی که خودتان تعیین کرده‌اید قفل می‌شوند. اپ در پایان هر جلسه، آمار تمرکز " +
            "شما را ثبت می‌کند. اپ یک ابزار خودکار است و مسئولیت کیفیت یا بازدهی مطالعهٔ شما با خودتان است."
    ),
    Section(
        "۳. حساب کاربری و کد بازیابی",
        "حساب کاربری اختیاری است و فقط با «نام کاربری» و «کد بازیابی» ساخته می‌شود؛ نه رمز عبور، " +
            "نه پیامک، نه ایمیل. کد بازیابی تنها یک‌بار هنگام ساخت حساب نمایش داده می‌شود و " +
            "سازنده آن را نمی‌بیند؛ فقط هش (SHA-256) آن نگه داشته می‌شود.\n" +
            "مسئولیت نگه‌داری امن کد بازیابی بر عهدهٔ خود کاربر است. اگر آن را گم کنید، " +
            "امکان بازیابی حساب وجود ندارد و حساب باید از نو ساخته شود."
    ),
    Section(
        "۴. استفادهٔ مجاز",
        "استفاده از اپ برای تمرکز شخصی مجاز است. استفاده از آن برای آزار دیگران، دور زدن " +
            "امنیت دستگاه‌های دیگر، یا هر فعالیت غیرقانونی مجاز نیست. سازنده حق دارد در صورت " +
            "تخلف، دسترسی به حساب را حذف کند."
    ),
    Section(
        "۵. دسترسی‌های ویژه",
        "برای قفل کردن اپ‌های حواس‌پرت‌کن، اپ از سرویس Accessibility استفاده می‌کند و برای " +
            "محاسبهٔ زمان واقعی استفاده از اپ‌ها، از دسترسی Usage access.\n" +
            "این دسترسی‌ها فقط زمانی فعال می‌شوند که شما یک جلسهٔ تمرکز را شروع کرده باشید و " +
            "پس از پایان جلسه غیرفعال می‌شوند. اپ هیچ محتوایی از صفحهٔ شما را نمی‌خواند، " +
            "ذخیره نمی‌کند یا به سرور نمی‌فرستد. دسترسی‌ها باید با رضایت آگاهانهٔ خودتان فعال شوند."
    ),
    Section(
        "۶. تبلیغات",
        "در نسخهٔ فعلی هیچ تبلیغی برای کاربران نمایش داده نمی‌شود. ممکن است در آینده تبلیغات " +
            "نمایش داده شود؛ در آن صورت این شرایط به‌روزرسانی می‌شود و تبلیغات به‌صورت جدا از " +
            "محتوای اصلی اپ نمایش داده خواهند شد."
    ),
    Section(
        "۷. محتوا و کاربران",
        "اپ هیچ محتوای تولیدی از کاربران ندارد: نه پست، نه چت، نظر یا پیام. تنها چیزی که " +
            "در لیگ نمایش داده می‌شود نام نمایشی و مجموع زمان تمرکز است. رقابت صرفاً بر پایهٔ " +
            "عدد است و هیچ داده‌ای دربارهٔ محتوای جلسات شما جمع یا منتشر نمی‌شود."
    ),
    Section(
        "۸. داده‌ها و حذف حساب",
        "آمار تمرکز ابتدا فقط روی دستگاه شما ذخیره می‌شود. اگر حساب بسازید، برای نمایش در لیگ " +
            "همگام می‌شود. شما هر زمان می‌توانید حساب را از داخل اپ یا با پیام به سازنده حذف کنید؛ " +
            "با حذف حساب، نام کاربری، کد بازیابی و آمار شما پاک می‌شود."
    ),
    Section(
        "۹. تغییرات شرایط",
        "ممکن است این شرایط را به‌روز کند. نسخهٔ معتبر همیشه در صفحهٔ «قوانین و حریم خصوصی» " +
            "داخل اپ و روی وب‌سایت در دسترس است. ادامهٔ استفاده از اپ به‌معنای پذیرش نسخهٔ جدید است."
    ),
    Section(
        "۱۰. محدودیت مسئولیت",
        "اپ «چای» به‌صورت «همان‌طور که هست» ارائه می‌شود. سازنده هیچ تضمینی برای بی‌وقفه بودن " +
            "سرویس، دقت شمارش زمان، یا نتیجهٔ تمرکز شما ندارد. مسئولیت هرگونه خسارت ناشی از " +
            "استفاده از اپ، بر عهدهٔ خود کاربر است."
    ),
    Section(
        "۱۱. قانون حاکم",
        "این شرایط تابع قوانین جمهوری اسلامی ایران است."
    ),
    Section(
        "۱۲. تماس",
        "سازنده: ابوالفضل صمدی — اهل تهران. برای هر سؤال دربارهٔ این شرایط می‌توانید از داخل " +
            "اپ پیام بفرستید."
    )
)

private val PRIVACY_SECTIONS = listOf(
    Section(
        "چه داده‌ای جمع می‌کنیم؟",
        "• نام کاربری و نام نمایشی — فقط اگر خودتان حساب بسازید\n" +
            "• هش کد بازیابی (نه خودِ کد)\n" +
            "• آمار تمرکز: مدت جلسه‌ها، هدف انتخابی و زمان شروع\n" +
            "• شمارندهٔ بازدید: یک عدد ساده بدون اطلاعات شخصی\n" +
            "• یک شناسهٔ تصادفی دستگاه (UUID محلی) برای شمارش بازدید"
    ),
    Section(
        "چه داده‌ای جمع نمی‌کنیم",
        "• مخاطبین، پیام‌ها، فایل‌ها یا محتوای گوشی شما\n" +
            "• موقعیت مکانی، مخاطبان یا هر چیز مربوط به هویت شما\n" +
            "• شمارهٔ تلفن (مگر خودتان حساب بسازید)\n" +
            "• هیچ داده‌ای دربارهٔ محتوای جلسات تمرکز شما"
    ),
    Section(
        "دسترسی‌های ویژه",
        "برای قفل کردن اپ‌های حواس‌پرت‌کن، اپ از سرویس Accessibility استفاده می‌کند. این دسترسی " +
            "فقط زمانی فعال می‌شود که یک جلسهٔ تمرکز را شروع کرده باشید و پس از پایان جلسه غیرفعال " +
            "می‌شود. هیچ محتوایی از صفحهٔ شما خوانده یا ذخیره نمی‌شود.\n" +
            "دسترسی Usage access فقط برای محاسبهٔ زمان واقعی استفاده از اپ‌ها در داشبورد است."
    ),
    Section(
        "محل ذخیره‌سازی",
        "آمار تمرکز شما ابتدا روی خود گوشی ذخیره می‌شود. اگر وارد حساب شوید، برای نمایش در لیگ " +
            "روی سرور (Supabase) هم ذخیره می‌شود. حذف حساب، همهٔ این داده‌ها را پاک می‌کند."
    ),
    Section(
        "اشتراک‌گذاری با شخص ثالث",
        "ما دادهٔ شما را به هیچ شخص یا شرکتی نمی‌فروشیم و به اشتراک نمی‌گذاریم."
    ),
    Section(
        "حقوق شما",
        "هر زمان خواستید می‌توانید حسابتان را حذف کنید. با حذف حساب، نام کاربری، کد بازیابی و " +
            "آمار شما پاک می‌شود."
    ),
    Section(
        "تماس",
        "سازنده: ابوالفضل صمدی — برای هر سؤال دربارهٔ این سیاست با ما در تماس باشید."
    )
)

/* ═══════════════════════ صفحه ═══════════════════════ */

@Composable
fun LegalScreen(
    kind: LegalKind,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val sections = if (kind == LegalKind.TERMS) TERMS_SECTIONS else PRIVACY_SECTIONS
    val title = if (kind == LegalKind.TERMS) "قوانین و مقررات" else "سیاست حریم خصوصی"
    val url = if (kind == LegalKind.TERMS) LegalLinks.TERMS else LegalLinks.PRIVACY

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Chai.Text
                    )
                }
                Spacer(Modifier.size(4.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, color = Chai.Text)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Chai.Soft)
                    .clickable {
                        runCatching {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Chai.Primary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "مشاهدهٔ نسخهٔ وب",
                    style = MaterialTheme.typography.labelLarge,
                    color = Chai.Primary
                )
            }
        }

        items(sections.size) { i ->
            val s = sections[i]
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Chai.Surface)
                    .padding(14.dp)
            ) {
                Text(
                    s.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Chai.Primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    s.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Chai.Text,
                    lineHeight = 25.sp
                )
            }
        }

        item {
            Text(
                "آخرین به‌روزرسانی: ۱۴ آبان ۱۴۰۵ · نسخهٔ ۱.۱",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** دیالوگ کوتاه «پذیرش قوانین» هنگام اولین اجرا */
@Composable
fun LegalAcceptDialog(
    onAccept: () -> Unit,
    onView: (LegalKind) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        containerColor = Chai.Surface,
        title = {
            Text("یک نکتهٔ کوتاه", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
        },
        text = {
            Column {
                Text(
                    "«چای» برای قفل کردن اپ‌ها از دسترسی Accessibility استفاده می‌کند. " +
                        "هیچ محتوایی از صفحهٔ شما خوانده نمی‌شود.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Chai.Text
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "قوانین",
                        style = MaterialTheme.typography.labelLarge,
                        color = Chai.Primary,
                        modifier = Modifier.clickable { onView(LegalKind.TERMS) }
                    )
                    Text("•", style = MaterialTheme.typography.labelLarge, color = Chai.Text2)
                    Text(
                        "حریم خصوصی",
                        style = MaterialTheme.typography.labelLarge,
                        color = Chai.Primary,
                        modifier = Modifier.clickable { onView(LegalKind.PRIVACY) }
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onAccept) {
                Text("متوجه شدم", color = Chai.Primary)
            }
        }
    )
}
