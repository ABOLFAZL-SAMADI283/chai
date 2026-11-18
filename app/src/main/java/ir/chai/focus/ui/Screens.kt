package ir.chai.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.chai.focus.DailyChallenge
import ir.chai.focus.Duel
import ir.chai.focus.FocusSession
import ir.chai.focus.Goal
import ir.chai.focus.LeagueRow
import ir.chai.focus.Leagues
import ir.chai.focus.R
import ir.chai.focus.Stats
import ir.chai.focus.TimeFmt

/* ═══════════════════════ کارت‌های مشترک ═══════════════════════ */

@Composable
fun ChaiCard(
    modifier: Modifier = Modifier,
    index: Int = 0,
    content: @Composable () -> Unit
) {
    // ورود پله‌ای — هر کارت کمی بعد از قبلی ظاهر می‌شود
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val dy = remember { androidx.compose.animation.core.Animatable(24f) }
    androidx.compose.runtime.LaunchedEffect(index) {
        kotlinx.coroutines.delay((index.coerceIn(0, 12) * 60).toLong())
        alpha.animateTo(
            1f,
            androidx.compose.animation.core.tween(340, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
        dy.animateTo(
            0f,
            androidx.compose.animation.core.spring(
                dampingRatio = 0.85f,
                stiffness = 260f
            )
        )
    }
    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Chai.Surface)
            .padding(16.dp)
            .graphicsLayer {
                this.alpha = alpha.value
                translationY = dy.value
            }
    ) { content() }
}

/** حلقهٔ پیشرفت گرد */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp,
    trackColor: Color = Chai.Surface2,
    progressColor: Color = Chai.Primary,
    center: @Composable () -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val r = (size.minDimension - sw) / 2f
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            drawArc(
                color = trackColor,
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
            )
        }
        center()
    }
}

/* ═══════════════════════ آمار ═══════════════════════ */

@Composable
fun StatsScreen(
    sessions: List<FocusSession>,
    todayVisits: Int = 0,
    totalVisits: Int = 0,
    modifier: Modifier = Modifier
) {
    val today = Stats.todaySeconds(sessions)
    val week = Stats.last7Days(sessions)
    val streak = Stats.streakDays(sessions)
    val completed = sessions.count { it.completed }
    val dailyGoal = 120 * 60                      // هدف پیش‌فرض روز: ۲ ساعت
    val goalProgress = (today.toFloat() / dailyGoal).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("داشبورد تمرکز", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
                    Text(
                        "این هفته چطور پیش رفتی؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Chai.Soft)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_chai_fire),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        "${faNum(streak)} روز",
                        style = MaterialTheme.typography.labelMedium,
                        color = Chai.Primary
                    )
                }
            }
        }

        // ── کارت بزرگ امروز ──
        item {
            ChaiCard(index = 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = goalProgress,
                        modifier = Modifier.size(96.dp),
                        strokeWidth = 9.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${faNum((goalProgress * 100).toInt())}٪",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Chai.Primary
                            )
                            Text("هدف ۲ ساعت", style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("امروز", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                        Text(
                            TimeFmt.humanHours(today),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Chai.Text
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (today >= dailyGoal) "هدف امروز کامل شد 🎉"
                            else "تا هدف امروز ${TimeFmt.humanHours(dailyGoal - today)} مانده",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (today >= dailyGoal) Chai.Primary else Chai.Text2
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat(TimeFmt.humanHours(week.sumOf { it.second }), "این هفته", Modifier.weight(1f))
                    MiniStat(faNum(completed), "جلسهٔ کامل", Modifier.weight(1f))
                    MiniStat(faNum(todayVisits), "بازدید امروز", Modifier.weight(1f))
                }
            }
        }

        // ── نمودار هفته ──
        item {
            ChaiCard(index = 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("این هفته", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                    Text(
                        TimeFmt.humanHours(week.sumOf { it.second }),
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Spacer(Modifier.height(12.dp))
                WeeklyBarChart(data = week)
            }
        }

        // ── نقشهٔ حرارتی ──
        item {
            ChaiCard(index = 3) {
                Text("۵ هفتهٔ اخیر", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                val days = remember(sessions) {
                    (0 until 35).map { i ->
                        val ts = System.currentTimeMillis() - (34 - i) * 86400000L
                        sessions.filter { it.dateKey == TimeFmt.dayKey(ts) }.sumOf { it.actualSec } / 60
                    }
                }
                FocusHeatmap(days = days)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("کم", style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
                    Spacer(Modifier.width(4.dp))
                    listOf(
                        Chai.Milk,
                        Chai.Soft,
                        Chai.Primary.copy(alpha = 0.35f),
                        Chai.Primary.copy(alpha = 0.62f),
                        Chai.Primary
                    ).forEach { c ->
                        Box(
                            Modifier
                                .padding(horizontal = 2.dp)
                                .size(11.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(c)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("زیاد", style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
                }
            }
        }

        // ── تفکیک هدف ──
        if (sessions.isNotEmpty()) {
            item {
                ChaiCard(index = 4) {
                    Text("بر اساس هدف", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                    Spacer(Modifier.height(10.dp))
                    GoalBreakdown(sessions = sessions)
                }
            }
        }

        // ── نشان‌ها ──
        item {
            val badges = computeBadges(sessions, streak)
            ChaiCard(index = 5) {
                Text("نشان‌ها", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                if (badges.isEmpty()) {
                    Text(
                        "هنوز نشانی نگرفته‌ای — یک جلسهٔ تمرکز شروع کن ☕",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                } else {
                    badges.chunked(3).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { b ->
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Chai.Soft.copy(alpha = 0.55f))
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(b.emoji, style = MaterialTheme.typography.titleLarge)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        b.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Chai.Text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        // ── خلاصهٔ کل ──
        item {
            ChaiCard(index = 6) {
                Text("خلاصهٔ کل", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat(TimeFmt.humanHours(sessions.sumOf { it.actualSec }), "مجموع تمرکز", Modifier.weight(1f))
                    MiniStat(faNum(sessions.size), "تعداد جلسه", Modifier.weight(1f))
                    MiniStat(faNum(totalVisits), "کل بازدید", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MiniStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Chai.Surface2)
            .padding(horizontal = 11.dp, vertical = 10.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Chai.Primary
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Chai.Text2)
    }
}

/** نشان‌ها — از روی آمار واقعی */
private data class Badge(val emoji: String, val title: String)

private fun computeBadges(sessions: List<FocusSession>, streak: Int): List<Badge> {
    val out = ArrayList<Badge>()
    val total = sessions.sumOf { it.actualSec }
    val done = sessions.count { it.completed }
    if (sessions.isNotEmpty()) out.add(Badge("🌱", "اولین فنجان"))
    if (total >= 3600) out.add(Badge("⏳", "یک ساعت تمرکز"))
    if (total >= 36000) out.add(Badge("🏆", "ده ساعت تمرکز"))
    if (done >= 10) out.add(Badge("🔥", "ده جلسهٔ کامل"))
    if (done >= 50) out.add(Badge("👑", "پنجاه جلسه"))
    if (streak >= 3) out.add(Badge("📅", "سه روز پیاپی"))
    if (streak >= 7) out.add(Badge("🗓", "یک هفتهٔ کامل"))
    if (streak >= 30) out.add(Badge("⚡", "یک ماه پیوسته"))
    return out
}

/* ═══════════════════════ لیگ ═══════════════════════ */

@Composable
fun LeagueScreen(
    rows: List<LeagueRow>,
    duels: List<Duel> = emptyList(),
    daily: DailyChallenge? = null,
    myLeague: String = "bronze",
    myScore: Int = 0,
    online: Boolean = true,
    onCreateDuel: (String, Int) -> Unit = { _, _ -> },
    onJoinDaily: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDuel by remember { mutableStateOf(false) }
    var duelName by remember { mutableStateOf("") }
    var duelMinutes by remember { mutableStateOf(30) }
    var duelMsg by remember { mutableStateOf<String?>(null) }

    val me = rows.firstOrNull { it.isMe }
    // نام هدف چالش روزانه (پارسر از قبل به فارسی ترجمه می‌کند)
    val dailyGoalLabel = daily?.goal?.takeIf { it.isNotBlank() } ?: "تمرکز"
    val nextLeague = when (myLeague.lowercase()) {
        "bronze" -> "silver"
        "silver" -> "gold"
        "gold" -> "platinum"
        else -> "platinum"
    }
    val curThreshold = Leagues.threshold(myLeague)
    val nextThreshold = Leagues.threshold(nextLeague)
    val progress = if (nextThreshold <= curThreshold) 1f
    else ((myScore - curThreshold).toFloat() / (nextThreshold - curThreshold)).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!online) {
            item { OfflineNotice("لیگ آفلاین در دسترس نیست — آمارت محلی ذخیره شده") }
        }

        // ── کارت لیگ من ──
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .shadow(9.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Chai.heroBrush)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFDF9E),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "لیگ ${Leagues.label(myLeague)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Chai.OnHero
                        )
                        Text(
                            "امتیاز این هفته: ${faNum(myScore)} دقیقه",
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.OnHero.copy(alpha = 0.74f)
                        )
                    }
                    Text(
                        me?.let { "#${faNum(it.rank)}" } ?: "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Chai.OnHero
                    )
                }
                if (nextLeague != myLeague.lowercase() && myLeague.lowercase() != "platinum") {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFF5EEAD4))
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "${faNum((nextThreshold - myScore).coerceAtLeast(0))} دقیقه تا لیگ ${Leagues.label(nextLeague)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Chai.OnHero.copy(alpha = 0.72f)
                    )
                }
            }
        }

        // ── جدول ──
        item {
            ChaiCard(index = 2) {
                Text("جدول هفتگی", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                if (rows.isEmpty()) {
                    Text(
                        if (online) "هنوز کسی در این هفته ثبت نشده — بیا اولین نفر باش!"
                        else "برای دیدن جدول، اینترنت لازم است",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                } else {
                    if (rows.size < 3) {
                        Text(
                            "تازه شروع شده — با هر جلسه بالا می‌روی",
                            style = MaterialTheme.typography.labelSmall,
                            color = Chai.Text2
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    rows.take(10).forEach { LeagueRowItem(it) }
                    if (rows.size > 10) {
                        Text(
                            "و ${faNum(rows.size - 10)} نفر دیگر…",
                            style = MaterialTheme.typography.labelSmall,
                            color = Chai.Text2
                        )
                    }
                }
            }
        }

        // ── جای من (اگر بیرون از ۱۰ نفر اول بود) ──
        if (me != null && me.rank > 10) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Chai.Soft)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    LeagueRowItem(me)
                }
            }
        }

        // ── دوئل ──
        item {
            ChaiCard(index = 3) {
                Text("⚔️ دوئل", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                Text(
                    "با یکی از دوستانت رقابت کن؛ هرکدام زودتر به هدف برسد، برنده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
                if (duels.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    duels.take(3).forEach { d ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Chai.Primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${d.hostName} ⚔️ ${d.guestName} — ${faNum(d.durationMin)} دقیقه",
                                style = MaterialTheme.typography.bodySmall,
                                color = Chai.Text,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (d.finished) "تمام شد" else "باز",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (d.finished) Chai.Text2 else Chai.Primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { showDuel = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Chai.Gold2)
                ) { Text("ساخت دوئل") }
            }
        }

        // ── چالش روزانه ──
        item {
            ChaiCard(index = 4) {
                Text("🎯 چالش روزانه", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Spacer(Modifier.height(4.dp))
                if (daily != null) {
                    Text(
                        "${dailyGoalLabel} — ${faNum(daily.durationMin)} دقیقه",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Chai.Text
                    )
                    Text(
                        "${faNum(daily.participants)} نفر امروز شرکت کردند",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                } else {
                    Text(
                        "امروز ۳۰ دقیقه تمرکز کن و به بقیه هم انگیزه بده",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onJoinDaily,
                    enabled = online,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (online) Chai.Soft else Chai.Milk,
                        contentColor = if (online) Chai.Primary else Chai.Text2
                    )
                ) { Text("شرکت می‌کنم") }
            }
        }
    }

    if (showDuel) {
        AlertDialog(
            onDismissRequest = { showDuel = false },
            containerColor = Chai.Surface,
            title = { Text("ساخت دوئل", color = Chai.Text) },
            text = {
                Column {
                    OutlinedTextField(
                        value = duelName,
                        onValueChange = { duelName = it },
                        label = { Text("نام کاربری یا نام نمایشی دوستت") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Chai.Text),
                        shape = RoundedCornerShape(14.dp),
                        colors = chaiFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("مدت", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60).forEach { m ->
                            SmallChip(text = "${faNum(m)} دقیقه", selected = duelMinutes == m) {
                                duelMinutes = m
                            }
                        }
                    }
                    if (duelMsg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            duelMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (duelMsg!!.startsWith("✅") || duelMsg!!.startsWith("⏳")) Chai.Primary else Chai.Danger
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (duelName.isBlank()) {
                            duelMsg = "نام دوستت را بنویس"
                        } else {
                            onCreateDuel(duelName.trim(), duelMinutes)
                            showDuel = false
                            duelName = ""
                            duelMsg = null
                        }
                    }
                ) { Text("بفرست", color = Chai.Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDuel = false; duelMsg = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun LeagueRowItem(r: LeagueRow) {
    val rankColor = when (r.rank) {
        1 -> Chai.Gold
        2 -> Color(0xFF9AA5B1)
        3 -> Color(0xFFB45309)
        else -> Chai.Text2
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            Text(
                faNum(r.rank),
                style = MaterialTheme.typography.bodyMedium,
                color = rankColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (r.isMe) Chai.Primary else Chai.Milk),
            contentAlignment = Alignment.Center
        ) {
            Text(r.avatar, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(
                r.nickname + if (r.isMe) " (من)" else "",
                style = MaterialTheme.typography.titleMedium,
                color = if (r.isMe) Chai.Primary else Chai.Text,
                fontWeight = if (r.isMe) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "${TimeFmt.humanHours(r.minutes * 60)} · ${faNum(r.streak)} روز",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2
            )
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(
                faNum(r.score),
                style = MaterialTheme.typography.titleMedium,
                color = if (r.isMe) Chai.Primary else Chai.Text,
                fontWeight = FontWeight.Bold
            )
            Text(
                "دقیقه",
                style = MaterialTheme.typography.labelSmall,
                color = Chai.Text2
            )
        }
    }
}

/* ═══════════════════════ بنر آفلاین ═══════════════════════ */

@Composable
fun OfflineNotice(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Chai.Milk)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📴", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Chai.Text)
    }
}

/* ═══════════════════════ پروفایل ═══════════════════════ */

@Composable
fun ProfileScreen(
    nickname: String,
    username: String,
    avatar: String,
    totalMinutes: Int,
    sessionCount: Int,
    bestStreak: Int,
    savedCode: String = "",
    onOpenSettings: () -> Unit,
    onOpenLegal: (LegalKind) -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var askLogout by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "حساب من",
                style = MaterialTheme.typography.headlineSmall,
                color = Chai.Text,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
            )
        }

        // ── کارت پروفایل ──
        item {
            ChaiCard(index = 0) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(78.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Chai.Soft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatar.ifBlank { "☕" }, style = MaterialTheme.typography.headlineMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        nickname.ifBlank { "مهمان" },
                        style = MaterialTheme.typography.titleLarge,
                        color = Chai.Text
                    )
                    if (username.isNotBlank()) {
                        Text("@$username", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
                    } else {
                        Text(
                            "بدون حساب — داده‌ها فقط روی این دستگاه ذخیره می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.Text2,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfilePill("🔥 ${faNum(bestStreak)} روز", Chai.goldSoft(0.22f), Chai.Gold)
                        ProfilePill("☕ ${faNum(sessionCount)} جلسه", Chai.Soft, Chai.Primary)
                    }
                    if (username.isBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary)
                        ) { Text("ساخت حساب یا ورود", style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
        }

        // ── آمار ──
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(TimeFmt.humanHours(totalMinutes * 60), "مجموع تمرکز", Modifier.weight(1f))
                MiniStat(faNum(sessionCount), "جلسه", Modifier.weight(1f))
                MiniStat(faNum(bestStreak), "بهترین استریک", Modifier.weight(1f))
            }
        }

        // ── کد بازیابی ──
        item {
            ChaiCard(index = 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Chai.primarySoft(0.10f)),
                        contentAlignment = Alignment.Center
                    ) { Text("🔑", style = MaterialTheme.typography.titleMedium) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("کد بازیابی", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                        Text(
                            "برای ورود روی گوشی دیگر — یک‌جا نگهش دار",
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.Text2
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Chai.Surface2)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (savedCode.isBlank()) "••••-••••-••••" else savedCode,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (savedCode.isBlank()) Chai.Text2 else Chai.Tea,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (savedCode.isBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "کد بازیابی فقط یک‌بار هنگام ساخت حساب نشان داده می‌شود. " +
                            "اگر ذخیره‌اش نکرده‌ای، باید حساب را از نو بسازی.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Chai.Text2
                    )
                }
            }
        }

        // ── تنظیمات و قوانین ──
        item {
            ChaiCard(index = 2) {
                ProfileMenuRow("⚙️", "تنظیمات", "دسترسی‌ها، تم، یادآور", onOpenSettings)
                ProfileDivider()
                ProfileMenuRow("📄", "قوانین و مقررات", "شرایط استفاده از سرویس") {
                    onOpenLegal(LegalKind.TERMS)
                }
                ProfileDivider()
                ProfileMenuRow("🛡️", "سیاست حریم خصوصی", "چه داده‌ای جمع می‌کنیم") {
                    onOpenLegal(LegalKind.PRIVACY)
                }
            }
        }

        // ── خروج ──
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Chai.Surface)
                    .clickable { askLogout = true }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Chai.Danger.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Text("⏏️", style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("خروج از حساب", style = MaterialTheme.typography.titleMedium, color = Chai.Danger)
                    Text(
                        "آمار روی این گوشی می‌ماند",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
            }
        }

        item {
            Text(
                About.VERSION + " · چای",
                style = MaterialTheme.typography.labelSmall,
                color = Chai.Text2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    // ── تأیید خروج از حساب ──
    if (askLogout) {
        AlertDialog(
            onDismissRequest = { askLogout = false },
            containerColor = Chai.Surface,
            title = { Text("از حساب خارج شوی؟", color = Chai.Text) },
            text = {
                Text(
                    "حسابت از این گوشی خارج می‌شود. جلسات و آمار تمرکزت روی همین گوشی می‌ماند، " +
                        "و با کد بازیابی می‌توانی دوباره وارد شوی.",
                    color = Chai.Text
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askLogout = false
                    onLogout()
                }) { Text("بله، خارج شو", color = Chai.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { askLogout = false }) { Text("بمان") }
            }
        )
    }
}

@Composable
private fun ProfilePill(text: String, bg: Color, fg: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    )
}

@Composable
private fun ProfileMenuRow(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Chai.Soft),
            contentAlignment = Alignment.Center
        ) { Text(emoji, style = MaterialTheme.typography.titleMedium) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Chai.Text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
        }
        Icon(
            Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = Chai.Text2,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Chai.Line)
    )
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Chai.Line)
    )
}
