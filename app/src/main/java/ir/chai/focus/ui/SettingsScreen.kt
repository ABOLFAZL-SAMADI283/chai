package ir.chai.focus.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    const val VERSION = "نسخهٔ ۲.۰.۱"
    const val NOTE = "سازندهٔ «چای» — اپ تمرکز برای فارسی‌زبانان."
}

/* ───────────────────────── تنظیمات ───────────────────────── */

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
    versionName: String = "نسخهٔ ۲.۰.۱",
    onExport: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Chai.Bg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 10.dp, bottom = 26.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── هدر ──
        item {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .shadow(3.dp, RoundedCornerShape(13.dp))
                        .clip(RoundedCornerShape(13.dp))
                        .background(Chai.Surface)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Chai.Text,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, color = Chai.Text)
            }
        }

        // ═══ ظاهر ═══
        item { SectionTitle("ظاهر برنامه") }
        item {
            SettingsCard {
                ThemeSegment(themeMode, onThemeChange)
                Spacer(Modifier.height(4.dp))
                SettingSwitchRow(
                    emoji = "👥",
                    title = "شمارندهٔ افراد فعال",
                    subtitle = "«همین الان N نفر در حال تمرکزند»",
                    checked = showActive,
                    onChange = onShowActiveChange
                )
            }
        }

        // ═══ تمرکز ═══
        item { SectionTitle("تمرکز") }
        item {
            SettingsCard {
                Text("مدت پیش‌فرض", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(listOf(15, 25, 30, 45, 60, 90)) { m ->
                        SmallChip(
                            text = faNum(m),
                            selected = m == defaultMinutes
                        ) { onDefaultMinutes(m) }
                    }
                }
                Spacer(Modifier.height(6.dp))
                SettingSwitchRow(
                    emoji = "🔔",
                    title = "صدای پایان جلسه",
                    subtitle = "وقتی چای تمام شد",
                    checked = soundOn,
                    onChange = onSoundChange,
                    tone = Tone.TEA
                )
                SettingSwitchRow(
                    emoji = "📳",
                    title = "لرزش پایان جلسه",
                    subtitle = "بازخورد لمسی",
                    checked = vibrateOn,
                    onChange = onVibrateChange,
                    tone = Tone.TEA
                )
                SettingSwitchRow(
                    emoji = "⏰",
                    title = "یادآور روزانه",
                    subtitle = if (reminderOn) "هر روز ساعت ${faTime(reminderHour, reminderMinute)}"
                    else "یک یادآوری کوچک برای تمرکز",
                    checked = reminderOn,
                    onChange = onReminderChange,
                    tone = Tone.GOLD
                )
                if (reminderOn) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Chai.Soft)
                            .clickable { showTimePicker = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🕗", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "تغییر ساعت یادآور",
                            style = MaterialTheme.typography.labelMedium,
                            color = Chai.Primary
                        )
                    }
                }
                RowDivider()
                MenuRow(
                    emoji = "🔒",
                    title = "اپ‌های قفل‌شونده",
                    subtitle = "انتخاب کن کدام اپ‌ها قفل شوند",
                    onClick = onOpenBlockedApps,
                    tone = Tone.DANGER
                )
            }
        }

        // ═══ دسترسی‌ها ═══
        item { SectionTitle("دسترسی‌ها") }
        item {
            SettingsCard {
                PermissionLine(
                    icon = Icons.Filled.Lock,
                    title = "سرویس دسترسی (Accessibility)",
                    subtitle = "لازم برای قفل کردن اپ‌ها",
                    granted = perms.accessibility,
                    onClick = onOpenAccessibility
                )
                PermissionLine(
                    icon = Icons.Filled.Settings,
                    title = "دسترسی آمار مصرف",
                    subtitle = "برای شمارش بازشدن‌ها و زمان استفاده",
                    granted = perms.usageAccess,
                    onClick = onOpenUsage
                )
                if (!perms.accessibility) {
                    Spacer(Modifier.height(8.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Chai.Soft)
                            .padding(12.dp)
                    ) {
                        Text(
                            "گوشی اجازه نداد؟",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Chai.Primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "اگر اپ را از مرورگر یا پیام‌رسان نصب کرده‌ای، اندروید آن را «برنامهٔ محافظت‌شده» " +
                                "می‌داند. تنظیمات ← برنامه‌ها ← چای ← «اجازهٔ تنظیمات محدود» را روشن کن، " +
                                "بعد برگرد و Accessibility را فعال کن.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.Text
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Chai.Primary)
                                .clickable { onOpenAppDetails() }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "باز کردن صفحهٔ برنامه",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Chai.Primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "همهٔ دسترسی‌ها فعال است — اپ‌ها قفل می‌شوند",
                            style = MaterialTheme.typography.labelSmall,
                            color = Chai.Text2
                        )
                    }
                }
            }
        }

        // ═══ داده‌ها ═══
        item { SectionTitle("داده‌ها") }
        item {
            SettingsCard {
                MenuRow(
                    emoji = "📤",
                    title = "خروجی گزارش تمرکز",
                    subtitle = "اشتراک‌گذاری خلاصهٔ آمار",
                    onClick = onExport
                )
                RowDivider()
                MenuRow(
                    emoji = "🗑️",
                    title = "حذف همهٔ داده‌ها",
                    subtitle = "جلسات، تنظیمات و حساب این دستگاه",
                    onClick = { showDelete = true },
                    tone = Tone.DANGER,
                    danger = true
                )
            }
        }

        // ═══ قوانین ═══
        item { SectionTitle("قوانین و اطلاعات") }
        item {
            SettingsCard {
                MenuRow(
                    emoji = "📄",
                    title = "قوانین و مقررات",
                    subtitle = "شرایط استفاده از سرویس",
                    onClick = { onOpenLegal(LegalKind.TERMS) }
                )
                RowDivider()
                MenuRow(
                    emoji = "🛡️",
                    title = "سیاست حریم خصوصی",
                    subtitle = "چه داده‌ای جمع می‌کنیم",
                    onClick = { onOpenLegal(LegalKind.PRIVACY) }
                )
                RowDivider()
                MenuRow(
                    emoji = "🏷️",
                    title = "نسخهٔ برنامه",
                    subtitle = versionName,
                    onClick = {}
                )
            }
        }

        // ═══ دربارهٔ سازنده ═══
        item { SectionTitle("دربارهٔ سازنده") }
        item {
            SettingsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChaiLogoMark(size = 44.dp)
                    Spacer(Modifier.width(10.dp))
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
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ChaiLogo(size = 80.dp, fill = 0.65f, showWord = true)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    About.NOTE,
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
                Text(
                    "ساخته‌شده با ❤️ برای وقت‌هایی که گوشی رها می‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
            }
        }

        item {
            Text(
                "${About.VERSION} · چای",
                style = MaterialTheme.typography.labelSmall,
                color = Chai.Text2,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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

/* ─────────────── اجزای تنظیمات ─────────────── */

enum class Tone { MINT, GOLD, TEA, DANGER }

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Chai.Text2,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Chai.Surface)
            .padding(14.dp)
    ) { content() }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 0.dp)
            .background(Chai.Line)
    )
}

/** کنترل سگمنتی تم — انتخاب فوری */
@Composable
private fun ThemeSegment(current: ThemeMode, onChange: (ThemeMode) -> Unit) {
    val options = listOf(
        ThemeMode.LIGHT to "☀️ روشن",
        ThemeMode.SYSTEM to "🌗 سیستم",
        ThemeMode.DARK to "🌙 تیره"
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Chai.Surface2)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { (mode, label) ->
            val selected = mode == current
            val bg by animateColorAsState(
                if (selected) Chai.Surface else Color.Transparent,
                animationSpec = tween(180),
                label = "segBg"
            )
            val fg by animateColorAsState(
                if (selected) Chai.Primary else Chai.Text2,
                animationSpec = tween(180),
                label = "segFg"
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(bg)
                    .clickable { onChange(mode) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = fg
                )
            }
        }
    }
}

/** آیکون رنگیِ هر ردیف */
@Composable
private fun RowIcon(emoji: String, tone: Tone) {
    val (bg, fg) = when (tone) {
        Tone.MINT -> Chai.Soft to Chai.Primary
        Tone.GOLD -> Chai.goldSoft(0.22f) to Chai.Gold
        Tone.TEA -> Chai.primarySoft(0.08f) to Chai.Tea
        Tone.DANGER -> Chai.Danger.copy(alpha = 0.12f) to Chai.Danger
    }
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium, color = fg)
    }
}

/** ردیف با کلید روشن/خاموش واقعی */
@Composable
fun SettingSwitchRow(
    emoji: String,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    tone: Tone = Tone.MINT
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(emoji, tone)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            }
        }
        Spacer(Modifier.width(6.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Chai.Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Chai.Line,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/** ردیف منو با فلش */
@Composable
fun MenuRow(
    emoji: String,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    tone: Tone = Tone.MINT,
    danger: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(emoji, tone)
        Spacer(Modifier.width(10.dp))
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
            tint = if (danger) Chai.Danger.copy(alpha = 0.8f) else Chai.Text2,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
fun SmallChip(text: String, selected: Boolean, onClick: () -> Unit) {
    // بازخورد لمسی: چیپ هنگام فشردن نرم کوچک می‌شود
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = androidx.compose.animation.core.spring(stiffness = 500f),
        label = "chipScale"
    )
    Box(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Chai.Primary else Chai.Surface2)
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 13.dp, vertical = 9.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Chai.Text2
        )
    }
}

/** عدد به فارسی */
fun faNum(n: Int): String {
    val digits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return n.toString().map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")
}

/** ساعت «۰۸:۳۰» — همیشه دو رقمی */
fun faTime(h: Int, m: Int): String = faNum(h).padStart(2, '۰') + ":" + faNum(m).padStart(2, '۰')
