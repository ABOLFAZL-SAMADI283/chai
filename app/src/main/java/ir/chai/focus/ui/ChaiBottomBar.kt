package ir.chai.focus.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** ۴ بخش اپ — مثل اینستا */
enum class ChaiTab(
    val title: String,
    val filled: ImageVector,
    val outlined: ImageVector
) {
    CHAI("چای", Icons.Filled.LocalCafe, Icons.Outlined.LocalCafe),
    STATS("آمار", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    LEAGUE("لیگ", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    PROFILE("پروفایل", Icons.Filled.Person, Icons.Outlined.Person)
}

private val TabWidth = 66.dp
private val IndicatorWidth = 24.dp

/**
 * نوار پایین با انیمیشن:
 *  • نشانگر (pill) بین تب‌ها **سُر می‌خزد**
 *  • آیکون تب فعال بزرگ‌تر و پررنگ‌تر می‌شود
 *  • برچسب تب فعال نمایان می‌شود
 */
@Composable
fun ChaiBottomBar(
    current: ChaiTab,
    onSelect: (ChaiTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val index = ChaiTab.entries.indexOf(current)
    val indicatorX by animateDpAsState(
        targetValue = TabWidth * index + (TabWidth - IndicatorWidth) / 2,
        animationSpec = ChaiSpringDp,
        label = "navIndicator"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Chai.Surface)
    ) {
        HorizontalDivider(color = Chai.Line, thickness = 1.dp)
        Box {
            // نشانگر لغزان
            Box(
                modifier = Modifier
                    .offset(x = indicatorX, y = 3.dp)
                    .size(width = IndicatorWidth, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Chai.Primary)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChaiTab.entries.forEach { tab ->
                    TabItem(
                        tab = tab,
                        active = tab == current,
                        onClick = { onSelect(tab) },
                        modifier = Modifier.width(TabWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: ChaiTab,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (active) 1.12f else 0.95f,
        animationSpec = ChaiSpring,
        label = "iconScale"
    )
    val tint by animateColorAsState(
        targetValue = if (active) Chai.Primary else Chai.Text2,
        animationSpec = tween(220),
        label = "iconTint"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0.55f,
        animationSpec = tween(220),
        label = "labelAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (active) tab.filled else tab.outlined,
            contentDescription = tab.title,
            tint = tint,
            modifier = Modifier
                .size(23.dp)
                .scale(iconScale)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = tint.copy(alpha = labelAlpha)
        )
    }
}
