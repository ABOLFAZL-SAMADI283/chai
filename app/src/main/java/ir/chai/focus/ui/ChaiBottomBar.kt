package ir.chai.focus.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** ۴ بخش اپ */
enum class ChaiTab(
    val title: String,
    val filled: ImageVector,
    val outlined: ImageVector
) {
    CHAI("چای", Icons.Filled.LocalCafe, Icons.Outlined.LocalCafe),
    STATS("آمار", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    LEAGUE("لیگ", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    PROFILE("من", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * نوار پایین شناور (کپسولی) — هم‌سبک با موکاپ تأییدشده.
 * تب فعال یک کاشی سبز نرم می‌گیرد و آیکونش پر می‌شود.
 */
@Composable
fun ChaiBottomBar(
    current: ChaiTab,
    onSelect: (ChaiTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Chai.Surface)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChaiTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    active = tab == current,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
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
    val tint by animateColorAsState(
        targetValue = if (active) Chai.Primary else Chai.Text2,
        animationSpec = tween(200),
        label = "tabTint"
    )
    val bg by animateColorAsState(
        targetValue = if (active) Chai.Soft else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (active) tab.filled else tab.outlined,
            contentDescription = tab.title,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = tint
        )
    }
}
