package ir.chai.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.chai.focus.ui.Chai
import ir.chai.focus.ui.ChaiTheme
import kotlinx.coroutines.delay

/**
 * صفحهٔ بازدارنده: وقتی کاربر سراغ اپ قفل‌شده می‌رود.
 * تنها راه برگشت: دکمهٔ «برگشت به تمرکز».
 */
class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChaiTheme(darkTheme = false) {
                BlockScreen(
                    goalLabel = FocusBlockerService.goalLabel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun BlockScreen(goalLabel: String, onBack: () -> Unit) {
    var remaining by remember { mutableIntStateOf(FocusBlockerService.remainingSec) }

    LaunchedEffect(Unit) {
        while (true) {
            remaining = FocusBlockerService.remainingSec
            delay(500)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Chai.BlockBg)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔒", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(10.dp))
            Text(
                "حواس‌پرتی ممنوع!",
                style = MaterialTheme.typography.headlineSmall,
                color = Chai.Milk
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "در زمان «${goalLabel.ifBlank { "تمرکز" }}» این اپ قفل است",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Milk.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = size.minDimension * 0.07f
                    drawArc(
                        color = Chai.Milk.copy(alpha = 0.18f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                    )
                    drawArc(
                        color = Chai.Gold,
                        startAngle = -90f,
                        sweepAngle = 360f * (remaining.coerceAtLeast(0) / 1800f).coerceIn(0f, 1f),
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        TimeFmt.mm:ss(remaining),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Chai.Milk
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "تا برگشت به تمرکز وقت داری",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Milk.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxSize(0.7f).height(52.dp)
            ) {
                Text("برگشت به تمرکز", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}
