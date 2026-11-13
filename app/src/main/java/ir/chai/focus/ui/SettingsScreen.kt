package ir.chai.focus.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.chai.focus.PermState

/** حالت نمایش برنامه */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** اطلاعات سازنده — در صفحهٔ «دربارهٔ سازنده» */
object About {
    const val DEVELOPER = "ابوالفضل صمدی"
    const val FROM = "اهل تهران"
    const val BORN = "متولد ۱۳۹۲"
    const val VERSION = "نسخهٔ ۱.۱"
    const val NOTE = "سازندهٔ «چای» — اپ تمرکز برای فارسی‌زبانان."
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    defaultMinutes: Int,
    onDefaultMinutes: (Int) -> Unit,
    onBack: () -> Unit,
    perms: PermState = PermState(),
    onOpenAccessibility: () -> Unit = {},
    onOpenUsage: () -> Unit = {},
    onOpenAppDetails: () -> Unit = {},
    onOpenBlockedApps: () -> Unit = {},
    onOpenLegal: (LegalKind) -> Unit = {},
    soundOn: Boolean = true,
    onSoundChange: (Boolean) -> Unit = {},
    vibrateOn: Boolean = true,
    onVibrateChange: (Boolean) -> Unit = {},
    reminderOn: Boolean = false,
    onReminderChange: (Boolean) -> Unit = {},
    reminderHour: Int = 8,
    reminderMinute: Int = 0,
    onReminderTimeChange: (Int, Int) -> Unit = { _, _ -> },
    showActive: Boolean = true,
    onShowActiveChange: (Boolean) -> Unit = {},
    versionName: String = "1.1",
    onExport: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

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
                Text("تنظیمات", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
            }
        }

        // ── دسترسی‌ها ──
        item {
            PermissionCard(
                perms = perms,
                onOpenAccessibility = onOpenAccessibility,
                onOpenUsage = onOpenUsage,
                onOpenAppDetails = onOpenAppDetails
            )
        }

        // ── ظاهر ──
        item {
            ChaiCard {
                Text("ظاهر برنامه", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip("☀️ روشن", ThemeMode.LIGHT, themeMode, onThemeChange)
                    ThemeChip("🌙 تیره", ThemeMode.DARK, themeMode, onThemeChange)
                    ThemeChip("🌗 سیستم", ThemeMode.SYSTEM, themeMode, onThemeChange)
                }
                Spacer(Modifier.height(4.dp))
                SwitchRow(
                    "نمایش شمارندهٔ فعال",
                    "«همین الان N نفر مثل تو در حال تمرکز هستند»",
                    showActive,
                    onShowActiveChange
                )
            }
        }

        // ── تمرکز ──
        item {
            ChaiCard {
                Text("تمرکز", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(8.dp))
                Text("مدت پیش‌فرض", style = MaterialTheme.typography.bodyMedium, color = Chai.Text)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(15, 25, 30, 45, 60, 90)) { m ->
                        SmallChip(
                            text = if (m == 30) "۳۰ دقیقه" else "$m",
                            selected = m == defaultMinutes
                        ) { onDefaultMinutes(m) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwitchRow("صدای پایان جلسه", "وقتی چای تمام شد", soundOn, onSoundChange)
                SwitchRow("لرزش پایان جلسه", "بازخورد لمسی", vibrateOn, onVibrateChange)
                Spacer(Modifier.height(6.dp))
                SwitchRow(
                    "یادآور روزانه",
                    "هر روز ساعت ${faTime(reminderHour, reminderMinute)} یادآوری تمرکز",
                    reminderOn,
                    onReminderChange
                )
                if (reminderOn) {
                    Spacer(Modifier.height(8.dp))
                    SmallChip("تغییر ساعت یادآور", selected = false) { showTimePicker = true }
                }
                Spacer(Modifier.height(10.dp))
                MenuRow(
                    "اپ‌های قفل‌شونده",
                    "انتخاب کن کدام اپ‌ها قفل شوند",
                    onClick = onOpenBlockedApps
                )
            }
        }

        // ── داده‌ها ──
        item {
            ChaiCard {
                Text("داده‌ها", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(6.dp))
                MenuRow("خروجی گزارش تمرکز", "اشتراک‌گذاری خلاصهٔ آمار", onClick = onExport)
                MenuRow(
                    "حذف همهٔ داده‌ها",
                    "جلسات، تنظیمات و حساب روی این دستگاه پاک می‌شود",
                    onClick = { showDelete = true },
                    danger = true
                )
            }
        }

        // ── دربارهٔ سازنده ──
        item {
            ChaiCard {
                Text("دربارهٔ سازنده", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChaiLogoMark(size = 46.dp)
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text(
                            About.DEVELOPER,
                            style = MaterialTheme.typography.titleMedium,
                            color = Chai.Text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${About.FROM} · ${About.BORN}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.Text2
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ChaiLogo(size = 84.dp, fill = 0.65f, showWord = true)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    About.NOTE + "\n" + About.VERSION + " · ساخته‌شده با ❤️ برای وقت‌هایی که گوشی رها می‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
            }
        }

        // ── سایر ──
        item {
            ChaiCard {
                MenuRow("قوانین و مقررات", "شرایط استفاده از سرویس", onClick = { onOpenLegal(LegalKind.TERMS) })
                MenuRow("سیاست حریم خصوصی", "چه داده‌ای جمع می‌کنیم", onClick = { onOpenLegal(LegalKind.PRIVACY) })
                MenuRow("نسخهٔ برنامه", versionName, onClick = {})
            }
        }
    }

    // ── انتخاب ساعت یادآور ──
    if (showTimePicker) {
        var h by remember { mutableIntStateOf(reminderHour) }
        var m by remember { mutableIntStateOf(reminderMinute) }
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Chai.Surface,
            title = { Text("ساعت یادآور", color = Chai.Text) },
            text = {
                Column {
                    Text("ساعت", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf(6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22)) { x ->
                            SmallChip(text = faNum(x), selected = h == x) { h = x }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("دقیقه", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 15, 30, 45).forEach { x ->
                            SmallChip(text = faNum(x), selected = m == x) { m = x }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeChange(h, m)
                    showTimePicker = false
                }) { Text("ثبت", color = Chai.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("انصراف") }
            }
        )
    }

    // ── حذف داده‌ها ──
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = Chai.Surface,
            title = { Text("حذف همهٔ داده‌ها؟", color = Chai.Text) },
            text = {
                Text(
                    "همهٔ جلسات تمرکز، تنظیمات و اطلاعات حساب روی این دستگاه پاک می‌شود. " +
                        "این کار برگشت‌پذیر نیست.",
                    color = Chai.Text
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    onDeleteAll()
                }) { Text("بله، پاک کن", color = Chai.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("بی‌خیال") }
            }
        )
    }
}

/* ─────────────── اجزای کوچک ─────────────── */

@Composable
private fun ThemeChip(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onChange: (ThemeMode) -> Unit
) {
    SmallChip(label, mode == current) { onChange(mode) }
}

@Composable
fun SmallChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Chai.Primary else Chai.Milk)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Chai.Text
        )
    }
}

/** ردیف با کلید روشن/خاموش واقعی */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            }
        }
        Spacer(Modifier.size(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Chai.Primary,
                uncheckedTrackColor = Chai.Line
            )
        )
    }
}

/** ردیف منو با فلش */
@Composable
fun MenuRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (danger) Chai.Danger else Chai.Text
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            }
        }
        Icon(
            Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = if (danger) Chai.Danger else Chai.Text2,
            modifier = Modifier.size(17.dp)
        )
    }
}

/** عدد به فارسی */
fun faNum(n: Int): String {
    val digits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return n.toString().map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")
}

/** ساعت «۰۸:۳۰» */
fun faTime(h: Int, m: Int): String = faNum(h) + ":" + faNum(m)
