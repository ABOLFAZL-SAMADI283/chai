package ir.chai.focus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * نوار پایین (مثل اینستا): آیکون + برچسب، حالت فعال سبز چای.
 */
@Composable
fun ChaiBottomBar(
    current: ChaiTab,
    onSelect: (ChaiTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Chai.Surface)
    ) {
        HorizontalDivider(color = Chai.Line, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChaiTab.entries.forEach { tab ->
                val active = tab == current
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (active) tab.filled else tab.outlined,
                        contentDescription = tab.title,
                        tint = if (active) Chai.Primary else Chai.Text2,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Chai.Primary else Chai.Text2
                    )
                }
            }
        }
    }
}
