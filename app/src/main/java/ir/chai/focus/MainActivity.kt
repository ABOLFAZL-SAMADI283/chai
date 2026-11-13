package ir.chai.focus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.chai.focus.auth.AuthRepository
import ir.chai.focus.auth.LoginScreen
import ir.chai.focus.ui.Chai
import ir.chai.focus.ui.ChaiBottomBar
import ir.chai.focus.ui.ChaiTab
import ir.chai.focus.ui.ChaiTheme
import ir.chai.focus.ui.ChaiSplashScreen
import ir.chai.focus.ui.ChaiSpring
import ir.chai.focus.ui.ChaiFadeIn
import ir.chai.focus.ui.ChaiFadeOut
import ir.chai.focus.ui.BlockedAppsScreen
import ir.chai.focus.ui.LeagueRow
import ir.chai.focus.ui.LegalKind
import ir.chai.focus.ui.LegalScreen
import ir.chai.focus.ui.OfflineNotice
import ir.chai.focus.ui.PermissionBanner
import ir.chai.focus.ui.ProfileScreen
import ir.chai.focus.ui.SettingsScreen
import ir.chai.focus.ui.StatsScreen
import ir.chai.focus.ui.LeagueScreen
import ir.chai.focus.ui.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** اجازهٔ اعلان (برای یادآور روزانه) */
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) showTestNotification(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureNotificationChannel(this)
        // ثبت بازدید (محلی همیشه، سرور در پس‌زمینه)
        val visits = Visits(this)
        visits.registerOpen()
        setContent {
            var themeMode by remember { mutableStateOf(ThemePrefs.load(this@MainActivity)) }
            var splashDone by remember { mutableStateOf(false) }
            ChaiTheme(darkTheme = themeMode.resolve()) {
                if (!splashDone) {
                    ChaiSplashScreen(onFinished = { splashDone = true })
                } else {
                    ChaiApp(
                        themeMode = themeMode,
                        onThemeChange = {
                            themeMode = it
                            ThemePrefs.save(this@MainActivity, it)
                        },
                        visits = visits,
                        onRequestNotifications = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                showTestNotification(this@MainActivity)
                            }
                        }
                    )
                }
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
    visits: Visits? = null,
    onRequestNotifications: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val repo = remember { AuthRepository(ctx) }
    val store = remember { SessionStore(ctx) }
    val sync = remember { SyncManager(store, repo) }
    val net = remember { NetMonitor(ctx) }
    val scope = rememberCoroutineScope()

    var tab by rememberSaveable { mutableStateOf(ChaiTab.CHAI) }
    var session by remember { mutableStateOf<ActiveSession?>(null) }
    var sessions by remember { mutableStateOf(store.load()) }
    var perms by remember { mutableStateOf(PermState.read(ctx)) }
    var online by remember { mutableStateOf(net.isOnline()) }
    var activeCount by remember { mutableIntStateOf(visits?.cachedActive() ?: 0) }
    var todayOpens by remember { mutableIntStateOf(visits?.todayOpens ?: 0) }
    var leagueRows by remember { mutableStateOf(emptyList<LeagueRow>()) }
    var duels by remember { mutableStateOf(emptyList<Duel>()) }
    var daily by remember { mutableStateOf<DailyChallenge?>(null) }
    var defaultMinutes by remember { mutableIntStateOf(Prefs.defaultMinutes(ctx)) }
    var toast by remember { mutableStateOf<String?>(null) }
    var showLogin by remember { mutableStateOf(false) }
    var legalKind by remember { mutableStateOf<LegalKind?>(null) }
    var blockedAppsOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }

    // ── پایش اینترنت ──
    DisposableEffect(net) {
        val stop = net.observe { online = it }
        onDispose { stop() }
    }

    // ── وضعیت دسترسی‌ها: هر بار که کاربر از تنظیمات برمی‌گردد دوباره بررسی می‌شود ──
    val activity = ctx as? ComponentActivity
    DisposableEffect(activity) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) perms = PermState.read(ctx)
        }
        activity?.lifecycle?.addObserver(obs)
        onDispose { activity?.lifecycle?.removeObserver(obs) }
    }

    // ── بازدید، شمارندهٔ فعال و همگام‌سازی ──
    LaunchedEffect(online) {
        repo.logVisit(visits?.anonId ?: "anon", "app_open").onFailure { }
        repo.activeCount().onSuccess { c ->
            activeCount = c.total
            todayOpens = c.todayOpens
            visits?.cacheActive(c.total, System.currentTimeMillis())
        }
        if (online && repo.isLoggedIn) {
            sync.pushPending()
        }
        while (true) {
            delay(60_000)
            repo.activeCount().onSuccess { c ->
                activeCount = c.total
                todayOpens = c.todayOpens
                visits?.cacheActive(c.total, System.currentTimeMillis())
            }
            if (online && repo.isLoggedIn) sync.pushPending()
        }
    }

    // ── لیگ و دوئل و چالش ──
    fun refreshLeague() {
        if (!online) return
        repo.league().onSuccess { json ->
            leagueRows = LeagueParser.parseLeague(json, repo.userId)
        }
        repo.myDuels().onSuccess { duels = LeagueParser.parseDuels(it) }
        repo.dailyChallenge().onSuccess { daily = LeagueParser.parseDaily(it) }
    }

    LaunchedEffect(tab, online, showLogin) {
        if (tab == ChaiTab.LEAGUE && online && !showLogin) refreshLeague()
    }

    // ── پیام کوتاه (snackbar ساده) ──
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2600)
            toast = null
        }
    }

    // ── صفحهٔ ورود ──
    if (showLogin) {
        LoginScreen(
            repo = repo,
            online = online,
            onLoggedIn = { u ->
                showLogin = false
                toast = "خوش آمدی $u ☕"
                scope.launch { sync.pushPending() }
                refreshLeague()
            },
            onSkip = { showLogin = false }
        )
        return
    }

    // ── صفحهٔ قوانین ──
    val legal = legalKind
    if (legal != null) {
        LegalScreen(kind = legal, onBack = { legalKind = null })
        return
    }

    // ── انتخاب اپ‌های قفل‌شونده ──
    if (blockedAppsOpen) {
        var selected by remember { mutableStateOf(Prefs.extraBlocked(ctx)) }
        BlockedAppsScreen(
            selected = selected,
            onToggle = { pkg ->
                selected = if (pkg in selected) selected - pkg else selected + pkg
                Prefs.setExtraBlocked(ctx, selected)
            },
            onBack = { blockedAppsOpen = false }
        )
        return
    }

    // ── جلسهٔ تمرکز ──
    val active = session
    if (active != null) {
        SessionScreen(
            session = active,
            onExit = { completed, elapsed ->
                store.add(
                    FocusSession(
                        id = "s_" + System.currentTimeMillis(),
                        goal = active.goal.id,
                        plannedSec = active.totalSec,
                        actualSec = elapsed,
                        startedAt = active.startedAt,
                        completed = completed
                    )
                ).also { sessions = it }
                session = null
                if (online && repo.isLoggedIn) {
                    scope.launch { sync.pushPending() }
                    refreshLeague()
                }
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

            // ── بنر قطع اینترنت ──
            if (!online) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OfflineNotice("اینترنت قطع است — تمرکزت محلی ذخیره می‌شود، نگران نباش")
                }
            }

            // ── انتقال انیمیشنی بین تب‌ها ──
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
                        todaySeconds = Stats.todaySeconds(sessions),
                        showActive = Prefs.showActiveCount(ctx),
                        perms = perms,
                        onOpenAccessibility = { Permissions.openAccessibilitySettings(ctx) },
                        onOpenUsage = { Permissions.openUsageAccessSettings(ctx) },
                        onStart = { goal, minutes ->
                            session = ActiveSession(goal = goal, totalSec = minutes * 60)
                        }
                    )

                    ChaiTab.STATS -> StatsScreen(
                        sessions = sessions,
                        todayVisits = todayOpens,
                        totalVisits = visits?.totalOpens ?: 0
                    )

                    ChaiTab.LEAGUE -> LeagueScreen(
                        rows = leagueRows,
                        duels = duels,
                        daily = daily,
                        myLeague = leagueRows.firstOrNull { it.isMe }?.league ?: "bronze",
                        myScore = leagueRows.firstOrNull { it.isMe }?.score ?: 0,
                        online = online,
                        onCreateDuel = { name, minutes ->
                            if (!online) {
                                toast = "برای دوئل اینترنت لازم است"
                            } else {
                                scope.launch {
                                    repo.createDuel(name, minutes)
                                        .onSuccess { toast = "✅ دوئل ساخته شد"; refreshLeague() }
                                        .onFailure { toast = it.message ?: "خطا" }
                                }
                            }
                        },
                        onJoinDaily = {
                            if (!online) {
                                toast = "برای شرکت در چالش اینترنت لازم است"
                            } else {
                                scope.launch {
                                    repo.joinDailyChallenge("study", 30)
                                        .onSuccess { n ->
                                            toast = "🎯 ثبت شد — $n نفر امروز شرکت کردند"
                                            refreshLeague()
                                        }
                                        .onFailure { toast = it.message ?: "خطا" }
                                }
                            }
                        }
                    )

                    ChaiTab.PROFILE -> if (settingsOpen) {
                        SettingsScreen(
                            themeMode = themeMode,
                            onThemeChange = onThemeChange,
                            defaultMinutes = defaultMinutes,
                            onDefaultMinutes = {
                                defaultMinutes = it
                                Prefs.setDefaultMinutes(ctx, it)
                            },
                            onBack = { settingsOpen = false },
                            perms = perms,
                            onOpenAccessibility = { Permissions.openAccessibilitySettings(ctx) },
                            onOpenUsage = { Permissions.openUsageAccessSettings(ctx) },
                            onOpenAppDetails = { Permissions.openAppDetails(ctx) },
                            onOpenBlockedApps = { blockedAppsOpen = true },
                            onOpenLegal = { legalKind = it },
                            soundOn = Prefs.soundOn(ctx),
                            onSoundChange = { Prefs.setSoundOn(ctx, it) },
                            vibrateOn = Prefs.vibrateOn(ctx),
                            onVibrateChange = { Prefs.setVibrateOn(ctx, it) },
                            reminderOn = Prefs.reminderOn(ctx),
                            onReminderChange = { on ->
                                Prefs.setReminderOn(ctx, on)
                                if (on) onRequestNotifications()
                            },
                            reminderHour = Prefs.reminderHour(ctx),
                            reminderMinute = Prefs.reminderMinute(ctx),
                            onReminderTimeChange = { h, m ->
                                Prefs.setReminderHour(ctx, h)
                                Prefs.setReminderMinute(ctx, m)
                            },
                            showActive = Prefs.showActiveCount(ctx),
                            onShowActiveChange = { Prefs.setShowActiveCount(ctx, it) },
                            versionName = appVersion(ctx),
                            onExport = { exportReport(ctx, store) },
                            onDeleteAll = {
                                store.save(emptyList())
                                sessions = emptyList()
                                Prefs.wipeAll(ctx)
                                defaultMinutes = 30
                                toast = "همهٔ داده‌ها پاک شد"
                            }
                        )
                    } else {
                        ProfileScreen(
                            nickname = repo.nickname,
                            username = repo.username,
                            avatar = repo.avatar,
                            totalMinutes = sessions.sumOf { it.actualSec } / 60,
                            sessionCount = sessions.size,
                            bestStreak = Stats.streakDays(sessions),
                            savedCode = Prefs.savedRecoveryCode(ctx),
                            onOpenSettings = { settingsOpen = true },
                            onOpenLegal = { legalKind = it },
                            onLogin = { showLogin = true },
                            onLogout = {
                                repo.clearSession()
                                toast = "از حساب خارج شدی"
                            }
                        )
                    }
                }
            }
        }
    }

    // ── پیام کوتاه ──
    val msg = toast
    if (msg != null) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Chai.Text)
                    .clickable { toast = null }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(msg, style = MaterialTheme.typography.bodyMedium, color = Color.White)
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
    todaySeconds: Int = 0,
    showActive: Boolean = true,
    perms: PermState = PermState(),
    onOpenAccessibility: () -> Unit = {},
    onOpenUsage: () -> Unit = {},
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
                        if (todaySeconds > 0) "امروز ${TimeFmt.humanHours(todaySeconds)} تمرکز"
                        else "امروز اولین فنجان چای توئه",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                GoalIcon(goal)
            }

            // ── بنر دسترسی‌ها ──
            PermissionBanner(
                perms = perms,
                onOpenAccessibility = onOpenAccessibility,
                onOpenUsage = onOpenUsage
            )

            // ── انگیزه ──
            if (showActive && activeNow > 0) {
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
            // ماگ چای با تنفّس ملایم
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
                Image(
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
    Image(
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
        Image(
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
            if (minutes == 30) "۳۰ دقیقه" else ir.chai.focus.ui.faNum(minutes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = fg
        )
    }
}

/* ───────────────────────── کمکی‌ها ───────────────────────── */

/** ساخت کانال اعلان (برای یادآور روزانه) */
fun ensureNotificationChannel(ctx: Context) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(android.app.NotificationManager::class.java)
            if (mgr?.getNotificationChannel("chai") == null) {
                val ch = android.app.NotificationChannel(
                    "chai", "یادآور تمرکز", android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
                ch.description = "یادآوری روزانهٔ شروع تمرکز"
                mgr?.createNotificationChannel(ch)
            }
        }
    } catch (e: Exception) {
        // بی‌خیال
    }
}

/** تست اعلان (اگر مجوز داده شده باشد) */
private fun showTestNotification(ctx: Context) {
    try {
        ensureNotificationChannel(ctx)
        val mgr = ctx.getSystemService(android.app.NotificationManager::class.java)
        mgr?.notify(
            1001,
            androidx.core.app.NotificationCompat.Builder(ctx, "chai")
                .setSmallIcon(R.drawable.ic_chai_cup)
                .setContentTitle("چای")
                .setContentText("وقتشه یک فنجان چای بریزی ☕")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
        )
    } catch (e: Exception) {
        // اعلان بدون مجوز — بی‌خیال
    }
}

/** نسخهٔ برنامه */
private fun appVersion(ctx: Context): String = try {
    val p = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
    "نسخهٔ ${p.versionName}"
} catch (e: Exception) {
    "نسخهٔ ۱.۱"
}

/** اشتراک‌گذاری گزارش تمرکز */
private fun exportReport(ctx: Context, store: SessionStore) {
    val text = store.exportText()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        ctx.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری گزارش تمرکز"))
    } catch (e: Exception) {
        // بی‌خیال
    }
}
