package ir.chai.focus.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ir.chai.focus.R

/* ───────────────────────── پالت دوگانه (روشن/تیره) ─────────────────────────
 *
 * چرا این‌طور؟ قبلاً رنگ‌ها ثابت بودند و در تم تاریک متن مشکی روی پس‌زمینهٔ تیره
 * ناخوانا می‌شد. حالا هر رنگ از پالت فعال خوانده می‌شود و با عوض شدن تم،
 * همهٔ صفحه‌ها دوباره رنگ می‌گیرند.
 */
private data class Palette(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val primary: Color,
    val primary2: Color,
    val soft: Color,
    val tea: Color,
    val milk: Color,
    val gold: Color,
    val gold2: Color,
    val text: Color,
    val text2: Color,
    val line: Color,
    val danger: Color,
    val blockBg: Color,
    val heroTop: Color,
    val heroMid: Color,
    val heroBottom: Color,
    val onHero: Color
)

private val LightPalette = Palette(
    bg = Color(0xFFF5F0E6),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF1EADD),
    primary = Color(0xFF0F766E),
    primary2 = Color(0xFF12A594),
    soft = Color(0xFFD9F3EE),
    tea = Color(0xFF8B5E3C),
    milk = Color(0xFFF5EFDF),
    gold = Color(0xFFE0A63C),
    gold2 = Color(0xFFF0B85A),
    text = Color(0xFF12211E),
    text2 = Color(0xFF6E807C),
    line = Color(0xFFE7DFD1),
    danger = Color(0xFFDE5252),
    blockBg = Color(0xFF12211E),
    heroTop = Color(0xFF0B4F4A),
    heroMid = Color(0xFF0F766E),
    heroBottom = Color(0xFF139C8E),
    onHero = Color(0xFFF2FFFC)
)

private val DarkPalette = Palette(
    bg = Color(0xFF0B1614),
    surface = Color(0xFF122420),
    surface2 = Color(0xFF182E29),
    primary = Color(0xFF2DD4BF),
    primary2 = Color(0xFF5EEAD4),
    soft = Color(0xFF11352F),
    tea = Color(0xFFCFA075),
    milk = Color(0xFF162421),
    gold = Color(0xFFF0B85A),
    gold2 = Color(0xFFF5C97B),
    text = Color(0xFFEDF6F3),
    text2 = Color(0xFF8FA9A2),
    line = Color(0xFF213530),
    danger = Color(0xFFFF8A8A),
    blockBg = Color(0xFF08110F),
    heroTop = Color(0xFF083B37),
    heroMid = Color(0xFF0E6A61),
    heroBottom = Color(0xFF16A08F),
    onHero = Color(0xFFEDFFFB)
)

/**
 * رنگ‌های برند. هر رنگ از پالتِ تم فعال خوانده می‌شود؛ بنابراین همهٔ صفحه‌ها
 * بدون تغییر تک‌تک فراخوانی‌ها، در تم تاریک هم درست دیده می‌شوند.
 */
object Chai {
    /** وضعیت تم — از داخل ChaiTheme مقدار می‌گیرد */
    private val darkState = mutableStateOf(false)

    fun setDark(value: Boolean) {
        if (darkState.value != value) darkState.value = value
    }

    val isDark: Boolean get() = darkState.value

    private val p: Palette get() = if (darkState.value) DarkPalette else LightPalette

    val Bg get() = p.bg                     // کرم کاغذی
    val Surface get() = p.surface           // کارت
    val Surface2 get() = p.surface2         // کاشی داخلی
    val Primary get() = p.primary           // سبز چای
    val Teal get() = p.primary              // نام قدیمی، برای سازگاری
    val Primary2 get() = p.primary2         // سبز روشن
    val Soft get() = p.soft                 // پس‌زمینهٔ کاشی سبز
    val Tea get() = p.tea                   // قهوهٔ چای
    val Milk get() = p.milk                 // فوم شیر
    val Gold get() = p.gold                 // مدال
    val Gold2 get() = p.gold2               // طلایی روشن
    val Text get() = p.text                 // متن اصلی
    val Text2 get() = p.text2               // متن دوم
    val Line get() = p.line                 // خط جداکننده
    val Danger get() = p.danger
    val BlockBg get() = p.blockBg

    /* هیرو / گرادیان برند */
    val HeroTop get() = p.heroTop
    val HeroMid get() = p.heroMid
    val HeroBottom get() = p.heroBottom
    val OnHero get() = p.onHero

    /** گرادیان هیرو — آماده برای پس‌زمینهٔ کارت‌های بزرگ */
    val heroBrush: Brush
        get() = Brush.linearGradient(listOf(p.heroTop, p.heroMid, p.heroBottom))

    fun goldSoft(alpha: Float = 0.16f): Color = p.gold.copy(alpha = alpha)
    fun primarySoft(alpha: Float = 0.12f): Color = p.primary.copy(alpha = alpha)
}

/* ───────────────────────── فونت ───────────────────────── */
private val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

val ChaiTypography = Typography(
    displaySmall = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = Vazir, fontSize = 15.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Vazir, fontSize = 13.5.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Vazir, fontSize = 12.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Vazir, fontSize = 10.5.sp, textAlign = TextAlign.Center)
)

/* ───────────────────────── رنگ‌بندی متریال ───────────────────────── */
private val LightColors = lightColorScheme(
    primary = LightPalette.primary,
    onPrimary = Color.White,
    primaryContainer = LightPalette.soft,
    onPrimaryContainer = LightPalette.primary,
    secondary = LightPalette.tea,
    onSecondary = Color.White,
    background = LightPalette.bg,
    onBackground = LightPalette.text,
    surface = LightPalette.surface,
    onSurface = LightPalette.text,
    surfaceVariant = LightPalette.milk,
    onSurfaceVariant = LightPalette.text2,
    outline = LightPalette.line,
    error = LightPalette.danger
)

private val DarkColors = darkColorScheme(
    primary = DarkPalette.primary,
    onPrimary = Color(0xFF06201D),
    primaryContainer = DarkPalette.soft,
    onPrimaryContainer = DarkPalette.primary2,
    secondary = DarkPalette.tea,
    onSecondary = Color(0xFF1B120A),
    background = DarkPalette.bg,
    onBackground = DarkPalette.text,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.text,
    surfaceVariant = DarkPalette.surface2,
    onSurfaceVariant = DarkPalette.text2,
    outline = DarkPalette.line,
    error = DarkPalette.danger
)

@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // پالت فعال را قبل از ترکیب محتوا تنظیم می‌کنیم
    Chai.setDark(darkTheme)
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ChaiTypography,
        content = content
    )
}
