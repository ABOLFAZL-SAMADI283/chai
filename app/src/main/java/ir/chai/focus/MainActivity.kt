package ir.chai.focus

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.chai.focus.auth.AuthRepository
import ir.chai.focus.ui.Chai
import ir.chai.focus.ui.ChaiBottomBar
import ir.chai.focus.ui.ChaiTab
import ir.chai.focus.ui.ChaiTheme
import ir.chai.focus.ui.ChaiSplashScreen
import ir.chai.focus.ui.ChaiSpring
import ir.chai.focus.ui.ChaiFadeIn
import ir.chai.focus.ui.ChaiFadeOut
import ir.chai.focus.ui.ProfileScreen
import ir.chai.focus.ui.SettingsScreen
import ir.chai.focus.ui.ThemeMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ثبت بازدید (محلی همیشه، سرور در پس‌زمینه)
        val visits = Visits(this)
        visits.registerOpen()
        setContent {
            var themeMode by remember { mutableStateOf(ThemePrefs.load(this@MainActivity)) }
            var splashDone by remember { mutableStateOf(false) }
            ChaiTheme(darkTheme = themeMode.resolve()) {
                if (!splashDone) {
                    ChaiSplashScreen(onFinished = { splashDone = true })
                }
                ChaiApp(
                    themeMode = themeMode,
                    onThemeChange = {
                        themeMode = it
                        ThemePrefs.save(this@MainActivity, it)
                    },
                    visits = visits
                )
            }
        }
    }
}

/** ذخیرهٔ حالت روشن/تیره/سیستم */
object ThemePrefs {
    private const val FILE = "chai_prefs"
    private const val KEY = "theme_mode"

    fun load(ctx: Context): ThemeMode {
        val v = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, "SYSTEM") ?: "SYSTEM"
        return runCatching { ThemeMode.valueOf(v) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun save(ctx: Context, mode: ThemeMode) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, mode.name).apply()
    }
}

@Composable
fun ThemeMode.resolve(): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun ChaiApp(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeChange: (ThemeMode) -> Unit = {},
    visits: Visits? = null
) {
    var tab by rememberSaveable { mutableStateOf(ChaiTab.CHAI) }
    var session by remember { mutableStateOf<ActiveSession?>(null) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var defaultMinutes by rememberSaveable { mutableIntStateOf(30) }

    // شمارندهٔ «همین الان چند نفر مثل تو فعال هستند»
    val ctx = LocalContext.current
    val repo = remember { AuthRepository(ctx) }
    var activeCount by remember { mutableIntStateOf(visits?.cachedActive() ?: 0) }
    var todayOpens by remember { mutableIntStateOf(visits?.todayOpens ?: 0) }

    LaunchedEffect(Unit) {
        // ثبت در سرور (fire & forget) + گرفتن شمارنده
        repo.logVisit(visits?.anonId ?: "anon", "app_open").onFailure { }
        repo.activeCount().onSuccess { c ->
            activeCount = c.total
            todayOpens = c.todayOpens
            visits?.cacheActive(c.total, System.currentTimeMillis())
        }
        // به‌روزرسانی دوره‌ای (هر ۶۰ ثانیه)
        while (true) {
            delay(60_000)
            repo.activeCount().onSuccess { c ->
                activeCount = c.total
                todayOpens = c.todayOpens
            }
        }
    }

    if (session != null) {
        val active = session!!
        SessionScreen(
            session = active,
            onExit = { completed, elapsed ->
                // ثبت جلسه در حافظهٔ محلی
                runCatching {
                    val store = SessionStore(ctx)
                    store.add(
                        FocusSession(
                            id = "s_" + System.currentTimeMillis(),
                            goal = active.goal.id,
                            plannedSec = active.totalSec,
                            actualSec = elapsed,
                            startedAt = active.startedAt,
                            completed = completed
                        )
                    )
                }
                session = null
            }
        )
        return
    }

    Scaffold(
        containerColor = Chai.Bg,
        bottomBar = {
            if (!settingsOpen) {
                ChaiBottomBar(current = tab, onSelect = { tab = it; settingsOpen = false })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // انتقال انیمیشنی بین تب‌ها (محو + لغزش)
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(ChaiFadeIn) + slideInHorizontally { it / 24 }) togetherWith
                        (fadeOut(ChaiFadeOut) + slideOutHorizontally { -it / 24 })
                },
                label = "tabContent"
            ) { t ->
                when (t) {
                    ChaiTab.CHAI -> HomeScreen(
                        defaultMinutes = defaultMinutes,
                        activeNow = activeCount,
                        todayOpens = todayOpens,
                        onStart = { goal, minutes ->
                            session = ActiveSession(goal = goal, totalSec = minutes * 60)
                        }
                    )
                    ChaiTab.PROFILE -> if (settingsOpen) {
                        SettingsScreen(
                            themeMode = themeMode,
                            onThemeChange = onThemeChange,
                            defaultMinutes = defaultMinutes,
                            onDefaultMinutes = { defaultMinutes = it },
                            onBack = { settingsOpen = false }
                        )
                    } else {
                        ProfileScreen(
                            nickname = "",
                            username = "",
                            totalMinutes = 0,
                            sessionCount = 0,
                            bestStreak = 0,
                            onOpenSettings = { settingsOpen = true },
                            onLogout = {}
                        )
                    }
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (t == ChaiTab.STATS) "آمار — در حال ساخت" else "لیگ — در حال ساخت",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Chai.Text2
                        )
                    }
                }
            }
        }
    }
}

/* ───────────────────────── صفحهٔ خانه ───────────────────────── */

@Composable
fun HomeScreen(
    defaultMinutes: Int = 30,
    activeNow: Int = 0,
    todayOpens: Int = 0,
    onStart: (Goal, Int) -> Unit
) {
    var goal by rememberSaveable { mutableStateOf(Goal.STUDY) }
    var minutes by rememberSaveable { mutableIntStateOf(defaultMinutes) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("سلام 👋", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
                    Text(
                        "امروز ۲ ساعت و ۱۵ دقیقه تمرکز",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                GoalIcon(goal)
            }

            // ── انگیزه: همین الان چند نفر مثل تو فعال هستند ──
            if (activeNow > 0) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Chai.Soft)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Chai.Primary)
                    )
                    Text(
                        "همین الان $activeNow نفر مثل تو در حال تمرکز هستند",
                        style = MaterialTheme.typography.labelMedium,
                        color = Chai.Primary
                    )
                }
            }
            if (todayOpens > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "امروز چای $todayOpens بار باز شده",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
            }

            Spacer(Modifier.height(14.dp))
            // ماگ چای با تنفّس ملایم (نشانهٔ زنده‌بودن صفحه)
            val breathe = rememberInfiniteTransition(label = "breathe")
            val pulse by breathe.animateFloat(
                initialValue = 1f,
                targetValue = 1.035f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_chai_cup),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                )
            }
            Text(
                "ماگ خالی — آمادهٔ پر شدن",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))
            Text("می‌خوام…", style = MaterialTheme.typography.bodyMedium, color = Chai.Text2)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(Goal.entries.size) { i ->
                    val g = Goal.entries[i]
                    GoalChip(g, selected = g == goal, onClick = { goal = g })
                }
            }

            Text("مدت", style = MaterialTheme.typography.bodyMedium, color = Chai.Text2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                listOf(15, 25, 30, 45, 60).forEach { m ->
                    DurationPill(m, selected = m == minutes, onClick = { minutes = m })
                }
            }

            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { onStart(goal, minutes) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary)
            ) {
                Text("☕  بریز چای", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun GoalIcon(goal: Goal) {
    val res = when (goal) {
        Goal.STUDY -> R.drawable.ic_chai_goal_study
        Goal.BOOK -> R.drawable.ic_chai_goal_book
        Goal.WORK -> R.drawable.ic_chai_goal_work
        Goal.GYM -> R.drawable.ic_chai_goal_gym
        Goal.SLEEP -> R.drawable.ic_chai_goal_sleep
    }
    androidx.compose.foundation.Image(
        painter = painterResource(res),
        contentDescription = goal.label,
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(19.dp))
    )
}

@Composable
fun GoalChip(goal: Goal, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (selected) Chai.Primary else Chai.Surface,
        animationSpec = tween(240),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else Chai.Text,
        animationSpec = tween(240),
        label = "chipFg"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = ChaiSpring,
        label = "chipScale"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(
                when (goal) {
                    Goal.STUDY -> R.drawable.ic_chai_goal_study
                    Goal.BOOK -> R.drawable.ic_chai_goal_book
                    Goal.WORK -> R.drawable.ic_chai_goal_work
                    Goal.GYM -> R.drawable.ic_chai_goal_gym
                    Goal.SLEEP -> R.drawable.ic_chai_goal_sleep
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .graphicsLayer { alpha = if (selected) 1f else 0.85f }
        )
        Text(
            goal.label,
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}

@Composable
fun DurationPill(minutes: Int, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (selected) Chai.Primary else Chai.Surface,
        animationSpec = tween(220),
        label = "pillBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else Chai.Text,
        animationSpec = tween(220),
        label = "pillFg"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = ChaiSpring,
        label = "pillScale"
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            if (minutes == 30) "۳۰ دقیقه" else minutes.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = fg
        )
    }
}
