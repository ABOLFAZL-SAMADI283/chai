package ir.chai.focus.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import ir.chai.focus.R

/* ───────────────────────── پالت ───────────────────────── */
object Chai {
    val Bg = Color(0xFFFBF7F0)          // کرم کاغذی
    val Surface = Color(0xFFFFFFFF)
    val Primary = Color(0xFF0F766E)      // سبز چای
    val Teal = Color(0xFF0F766E)         // سبز چای — برای متن برند (روشن و تیره)
    val Primary2 = Color(0xFF14B8A6)     // سبز روشن
    val Soft = Color(0xFFCCFBF1)         // پس‌زمینهٔ کاشی
    val Tea = Color(0xFF8B5E3C)          // قهوهٔ چای
    val Milk = Color(0xFFF5EFDF)         // فوم شیر
    val Gold = Color(0xFFD4A017)         // مدال
    val Gold2 = Color(0xFFF59E0B)
    val Text = Color(0xFF1C1917)         // مشکی گرم
    val Text2 = Color(0xFF78716C)
    val Line = Color(0xFFEDE4D8)
    val Danger = Color(0xFFDC2626)
    val BlockBg = Color(0xFF2A1F18)      // پس‌زمینهٔ صفحهٔ بازدارنده
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
    headlineSmall = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = Vazir, fontSize = 15.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Vazir, fontSize = 13.5.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Vazir, fontSize = 11.5.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Vazir, fontSize = 10.5.sp, textAlign = TextAlign.Center)
)

private val LightColors = lightColorScheme(
    primary = Chai.Primary,
    onPrimary = Color.White,
    primaryContainer = Chai.Soft,
    onPrimaryContainer = Chai.Primary,
    secondary = Chai.Tea,
    onSecondary = Color.White,
    background = Chai.Bg,
    onBackground = Chai.Text,
    surface = Chai.Surface,
    onSurface = Chai.Text,
    surfaceVariant = Chai.Milk,
    onSurfaceVariant = Chai.Text2,
    outline = Chai.Line,
    error = Chai.Danger
)

private val DarkColors = darkColorScheme(
    primary = Chai.Primary2,
    onPrimary = Color(0xFF06201D),
    primaryContainer = Color(0xFF12433E),
    onPrimaryContainer = Chai.Soft,
    secondary = Chai.Tea,
    background = Color(0xFF15100C),
    onBackground = Chai.Milk,
    surface = Color(0xFF201913),
    onSurface = Chai.Milk,
    outline = Color(0xFF3A2F26),
    error = Chai.Danger
)

@Composable
fun ChaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ChaiTypography,
        content = content
    )
}
