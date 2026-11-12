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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    const val VERSION = "نسخهٔ ۱.۰"
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Chai.Text
                    )
                }
                Spacer(Modifier.size(4.dp))
                Text("تنظیمات", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
            }
        }

        // ── ظاهر برنامه ──
        item {
            SettingsCard(title = "ظاهر برنامه") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip("☀️ روشن", ThemeMode.LIGHT, themeMode, onThemeChange)
                    ThemeChip("🌙 تیره", ThemeMode.DARK, themeMode, onThemeChange)
                    ThemeChip("🌗 سیستم", ThemeMode.SYSTEM, themeMode, onThemeChange)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "رنگ‌ها و ماگ چای با تم هماهنگ می‌شوند",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
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

        // ── تمرکز ──
        item {
            SettingsCard(title = "تمرکز") {
                Text("مدت پیش‌فرض", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 25, 30, 45, 60).forEach { m ->
                        SmallChip(
                            text = if (m == 30) "۳۰ دقیقه" else "$m",
                            selected = m == defaultMinutes,
                            onClick = { onDefaultMinutes(m) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SettingsRow("یادآور پایان جلسه", "صدا و لرزش", true)
                SettingsRow("هشدار روزانه", "یادآوری شروع تمرکز", true, "۸:۰۰")
            }
        }

        // ── دربارهٔ سازنده ──
        item {
            SettingsCard(title = "دربارهٔ سازنده") {
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
                    color = Chai.Text2,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }

        // ── سایر ──
        item {
            SettingsCard(title = null) {
                SettingsRow("پشتیبانی", null, false, "›")
                SettingsRow("قوانین و حریم خصوصی", null, false, "›")
                SettingsRow(About.VERSION, null, false, null)
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String?, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Chai.Surface)
            .padding(14.dp)
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            Spacer(Modifier.height(10.dp))
        }
        content()
    }
}

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

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String?,
    enabled: Boolean,
    value: String? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            }
        }
        if (value != null) {
            if (enabled) {
                Box(
                    Modifier
                        .size(42.dp, 24.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Chai.Primary),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        Modifier
                            .padding(3.dp)
                            .size(18.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White)
                    )
                }
            } else {
                Text(value, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            }
        }
    }
}
