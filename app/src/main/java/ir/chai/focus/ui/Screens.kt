package ir.chai.focus.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.chai.focus.FocusSession
import ir.chai.focus.Goal
import ir.chai.focus.R
import ir.chai.focus.Stats
import ir.chai.focus.TimeFmt

/* ═══════════════════════ آمار ═══════════════════════ */

@Composable
fun StatsScreen(sessions: List<FocusSession>, modifier: Modifier = Modifier) {
    val today = Stats.todaySeconds(sessions)
    val week = Stats.last7Days(sessions)
    val streak = Stats.streakDays(sessions)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("داشبورد تمرکز", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("امروز", TimeFmt.humanHours(today), Modifier.weight(1f))
                StatCard("استریک", "$streak روز", Modifier.weight(1f), Chai.Gold2)
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chai_fire),
                    contentDescription = null,
                    modifier = Modifier.size(38.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ic_chai_medal),
                    contentDescription = null,
                    modifier = Modifier.size(38.dp)
                )
                Text(
                    "$streak روز پیاپی · ${sessions.count { it.completed }} جلسهٔ کامل",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Chai.Surface)
                    .padding(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("این هفته", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                    Text(
                        TimeFmt.humanHours(week.sumOf { it.second }),
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Spacer(Modifier.height(10.dp))
                WeeklyBarChart(data = week)
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Chai.Surface)
                    .padding(14.dp)
            ) {
                Text("۵ هفتهٔ اخیر", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                val days = (0 until 35).map { i ->
                    val idx = sessions.filter { it.dateKey == TimeFmt.dayKey(System.currentTimeMillis() - (34 - i) * 86400000L) }
                        .sumOf { it.actualSec } / 60
                    idx
                }
                FocusHeatmap(days = days)
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Chai.Surface)
                    .padding(14.dp)
            ) {
                Text("بر اساس هدف", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                GoalBreakdown(sessions = sessions)
            }
        }
    }
}

/* ═══════════════════════ لیگ ═══════════════════════ */

data class LeagueRow(
    val rank: Int,
    val nickname: String,
    val minutes: Int,
    val streak: Int,
    val score: Int,
    val isMe: Boolean = false
)

@Composable
fun LeagueScreen(rows: List<LeagueRow>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("لیگ هفتگی", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
            Spacer(Modifier.height(2.dp))
            Text("لیگ نقره — ۲ ساعت تا ارتقا به طلا", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Chai.Soft)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.62f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Chai.Gold2)
                )
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Chai.Surface)
                    .padding(horizontal = 12.dp)
            ) {
                rows.filter { !it.isMe }.forEach { r -> LeagueRowItem(r) }
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Chai.Soft)
                    .padding(horizontal = 12.dp)
            ) {
                rows.firstOrNull { it.isMe }?.let { LeagueRowItem(it) }
            }
        }
        item {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Chai.Gold2)
            ) { Text("⚔️  چالش دوئل با دوستت") }
        }
        item {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Chai.Soft)
            ) { Text("🎯  چالش روزانه: ۳۰ دقیقه", color = Chai.Primary) }
        }
    }
}

@Composable
private fun LeagueRowItem(r: LeagueRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${r.rank}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (r.rank <= 3) Chai.Gold2 else Chai.Text2,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.size(width = 20.dp, height = 20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.size(8.dp))
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (r.isMe) Chai.Primary else Chai.Milk),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    when (r.rank % 4) {
                        0 -> R.drawable.av1
                        1 -> R.drawable.av2
                        2 -> R.drawable.av3
                        else -> R.drawable.av4
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.size(9.dp))
        Column(Modifier.weight(1f)) {
            Text(r.nickname, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            Text(
                "${TimeFmt.humanHours(r.minutes * 60)} · ${r.streak} روز",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2
            )
        }
        Text(
            "${r.score}",
            style = MaterialTheme.typography.titleMedium,
            color = if (r.isMe) Chai.Primary else Chai.Text,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ═══════════════════════ پروفایل ═══════════════════════ */

@Composable
fun ProfileScreen(
    nickname: String,
    username: String,
    totalMinutes: Int,
    sessionCount: Int,
    bestStreak: Int,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_chai_cup),
                    contentDescription = null,
                    modifier = Modifier.size(88.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                Text(nickname.ifBlank { "مهمان" }, style = MaterialTheme.typography.titleLarge, color = Chai.Text)
                Text("@$username", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Chai.Soft)
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chai_leaf),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("تغییر آواتار", style = MaterialTheme.typography.labelMedium, color = Chai.Primary)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("ساعت کل", "$totalMinutes", Modifier.weight(1f))
                StatCard("جلسه", "$sessionCount", Modifier.weight(1f))
                StatCard("بهترین استریک", "$bestStreak", Modifier.weight(1f), Chai.Gold2)
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Chai.Surface)
                    .padding(14.dp)
            ) {
                Text("کد بازیابی", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                Text(
                    "کد را ذخیره کرده‌ای. اگر گم شد، با همین نام کاربری و کد وارد شو.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Chai.Milk)
                        .padding(12.dp)
                ) {
                    Text(
                        "••••-••••-••••",
                        style = MaterialTheme.typography.titleMedium,
                        color = Chai.Text2,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "🔄 بازیابی کد جدید",
                    style = MaterialTheme.typography.labelLarge,
                    color = Chai.Primary,
                    modifier = Modifier.clickable { }.padding(vertical = 6.dp)
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Chai.Surface)
                    .clickable { onOpenSettings() }
                    .padding(14.dp)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    tint = Chai.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text("تنظیمات", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.weight(1f))
                Text("›", style = MaterialTheme.typography.bodyLarge, color = Chai.Text2)
            }
        }
        item {
            Text(
                "خروج از حساب",
                style = MaterialTheme.typography.labelLarge,
                color = Chai.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Chai.Surface)
                    .clickable { onLogout() }
                    .padding(14.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
