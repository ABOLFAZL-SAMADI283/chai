package ir.chai.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * اسپلش انیمیشنی «چای»: ماگ پُر می‌شود، لوگو می‌آید، بعد محو می‌شود.
 * فقط بار اول اجرا (یا هر بار اگر کاربر بخواهد) نمایش داده می‌شود.
 */
@Composable
fun ChaiSplashScreen(
    onFinished: () -> Unit,
    showOnce: Boolean = true
) {
    var phase by remember { mutableStateOf(0) }   // ۰ = ورود، ۱ = پر شدن، ۲ = محو شدن
    var visible by remember { mutableStateOf(true) }

    val fill by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 1100),
        label = "cupFill"
    )
    val scale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0.86f,
        animationSpec = tween(durationMillis = 700),
        label = "logoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else 1f,
        animationSpec = tween(durationMillis = 450),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        delay(350)
        phase = 1
        delay(1400)
        phase = 2
        delay(500)
        visible = false
        onFinished()
    }

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Chai.Bg)
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-30).dp)
            ) {
                ChaiLogo(
                    modifier = Modifier.scale(scale),
                    size = 140.dp,
                    fill = fill,
                    showWord = true
                )
                Text(
                    "۳۰ دقیقه تمرکز، یک ماگ چای",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}
