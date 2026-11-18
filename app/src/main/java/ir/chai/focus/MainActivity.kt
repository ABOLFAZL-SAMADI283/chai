package ir.chai.focus

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ir.chai.focus.ui.ChaiCard
import ir.chai.focus.ui.MiniStat
import ir.chai.focus.ui.faNum
import ir.chai.focus.ui.BlockedAppsScreen
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
            if (granted) {
                Feedback.ensureChannels(this)
                Feedback.notify(
                    this, Feedback.CHANNEL_REMINDER, 3001,
                    "یادآور روزانه روشن شد 🔔",
                    "هر روز ساعت انتخابی یادآوری تمرکز می‌فرستیم."
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Feedback.ensureChannels(this)
        // یادآور روزانه اگر روشن است، دوباره تنظیم می‌شود
        if (Prefs.reminderOn(this)) {
            ReminderScheduler.schedule(this, Prefs.reminderHour(this), Prefs.reminderMinute(this))
        }
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
                                Feedback.ensureChannels(this@MainActivity)
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
    var netOnline by remember { mutableStateOf(net.isOnline()) }
    var serverOk by remember { mutableStateOf(false) }
    // اینترنت هست اگر یا سیستم بگوید وصل است، یا سرور خودش جواب داده باشد
    // (روی بعضی شبکه‌ها پرچم VALIDATED اشتباه false است ⇒ فقط به آن تکیه نمی‌کنیم)
    val online = netOnline || serverOk
    var activeCount by remember {
        // کش فقط اگر تازه باشد — عدد کهنه نمایش داده نمی‌شود
        mutableIntStateOf(
            if (visits != null && visits.activeCacheAgeMs() < 5 * 60_000L) visits.cachedActive() else 0
        )
    }
    var todayOpens by remember { mutableIntStateOf(visits?.todayOpens ?: 0) }
    var leagueRows by remember { mutableStateOf(emptyList<LeagueRow>()) }
    var duels by remember { mutableStateOf(emptyList<Duel>()) }
    var daily by remember { mutableStateOf<DailyChallenge?>(null) }
    var defaultMinutes by remember { mutableIntStateOf(Prefs.defaultMinutes(ctx)) }
    var soundOn by remember { mutableStateOf(Prefs.soundOn(ctx)) }
    var vibrateOn by remember { mutableStateOf(Prefs.vibrateOn(ctx)) }
    var reminderOn by remember { mutableStateOf(Prefs.reminderOn(ctx)) }
    var reminderHourS by remember { mutableIntStateOf(Prefs.reminderHour(ctx)) }
    var reminderMinuteS by remember { mutableIntStateOf(Prefs.reminderMinute(ctx)) }
    var showActive by remember { mutableStateOf(Prefs.showActiveCount(ctx)) }
    var toast by remember { mutableStateOf<String?>(null) }
    var showLogin by remember { mutableStateOf(false) }
    var legalKind by remember { mutableStateOf<LegalKind?>(null) }
    var blockedAppsOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    // با خروج از حساب یا حذف داده‌ها زیاد می‌شود تا پروفایل/تنظیمات فوراً بازخوانی شوند
    var authTick by remember { mutableIntStateOf(0) }

    // ── پایش اینترنت ──
    DisposableEffect(net) {
        val stop = net.observe { netOnline = it }
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
    suspend fun tick() {
        val anon = visits?.anonId ?: "anon"
        // ضربان حضور: فقط یک ردیف برای هر کاربر — با این کار شمارنده «الکی» بالا نمی‌رود
        repo.heartbeat(anon, session?.goal?.id ?: "app_open")
            .onSuccess { serverOk = true }
            .onFailure { serverOk = false }

        repo.activeCount()
            .onSuccess { c ->
                serverOk = true
                activeCount = c.total
                todayOpens = c.todayOpens
                visits?.cacheActive(c.total, System.currentTimeMillis())
            }
            .onFailure { serverOk = false }

        if (serverOk && repo.isLoggedIn) sync.pushPending()
    }

    // ثبت بازدید سروری — فقط یک‌بار در هر باز شدن اپ (نه هر دقیقه)
    LaunchedEffect(Unit) {
        repo.logVisit(visits?.anonId ?: "anon", "app_open")
    }

    LaunchedEffect(netOnline) {
        while (true) {
            tick()
            delay(60_000)
        }
    }

    // ── لیگ و دوئل و چالش ──
    suspend fun refreshLeague() {
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
                scope.launch {
                    sync.pushPending()
                    refreshLeague()
                }
            },
            onSkip = { showLogin = false },
            onOpenLegal = { legalKind = it }
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
                    scope.launch {
                        sync.pushPending()
                        refreshLeague()
                    }
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
                        sessionCount = sessions.size,
                        streak = Stats.streakDays(sessions),
                        nickname = repo.nickname,
                        showActive = showActive,
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
                                        .onSuccess {
                                            toast = "✅ دوئل ساخته شد"
                                            scope.launch { refreshLeague() }
                                        }
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
                                            toast = "🎯 ثبت شد — ${faNum(n)} نفر امروز شرکت کردند"
                                            scope.launch { refreshLeague() }
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
                            soundOn = soundOn,
                            onSoundChange = { on ->
                                soundOn = on
                                Prefs.setSoundOn(ctx, on)
                                // تست فوری صدا
                                if (on) Feedback.sessionFinished(ctx)
                            },
                            vibrateOn = vibrateOn,
                            onVibrateChange = { on ->
                                vibrateOn = on
                                Prefs.setVibrateOn(ctx, on)
                                if (on) Feedback.vibrate(ctx)
                            },
                            reminderOn = reminderOn,
                            onReminderChange = { on ->
                                reminderOn = on
                                Prefs.setReminderOn(ctx, on)
                                if (on) {
                                    ReminderScheduler.schedule(ctx, reminderHourS, reminderMinuteS)
                                    onRequestNotifications()
                                } else {
                                    ReminderScheduler.cancel(ctx)
                                }
                            },
                            reminderHour = reminderHourS,
                            reminderMinute = reminderMinuteS,
                            onReminderTimeChange = { h, m ->
                                reminderHourS = h
                                reminderMinuteS = m
                                Prefs.setReminderHour(ctx, h)
                                Prefs.setReminderMinute(ctx, m)
                                if (reminderOn) {
                                    ReminderScheduler.schedule(ctx, h, m)
                                }
                            },
                            showActive = showActive,
                            onShowActiveChange = { on ->
                                showActive = on
                                Prefs.setShowActiveCount(ctx, on)
                            },
                            versionName = appVersion(ctx),
                            onExport = { exportReport(ctx, store) },
                            onDeleteAll = {
                                store.save(emptyList())
                                sessions = emptyList()
                                ReminderScheduler.cancel(ctx)
                                Prefs.wipeAll(ctx)
                                // همهٔ stateها فوراً به پیش‌فرض برمی‌گردند (UI بدون ری‌استارت درست می‌شود)
                                defaultMinutes = 30
                                soundOn = true
                                vibrateOn = true
                                reminderOn = false
                                reminderHourS = 8
                                reminderMinuteS = 0
                                showActive = true
                                activeCount = 0
                                todayOpens = 0
                                leagueRows = emptyList()
                                duels = emptyList()
                                daily = null
                                onThemeChange(ThemeMode.SYSTEM)
                                authTick++
                                settingsOpen = false
                                toast = "همهٔ داده‌ها پاک شد"
                            }
                        )
                    } else key(authTick) {
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
                                authTick++
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
    sessionCount: Int = 0,
    streak: Int = 0,
    nickname: String = "",
    showActive: Boolean = true,
    perms: PermState = PermState(),
    onOpenAccessibility: () -> Unit = {},
    onOpenUsage: () -> Unit = {},
    onStart: (Goal, Int) -> Unit
) {
    var goal by rememberSaveable { mutableStateOf(Goal.STUDY) }
    var minutes by rememberSaveable { mutableIntStateOf(defaultMinutes) }
    // اگر مدت پیش‌فرض در تنظیمات عوض شود، همین‌جا هم‌راه می‌شود
    LaunchedEffect(defaultMinutes) { minutes = defaultMinutes }

    val who = nickname.trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        // ── سلام + آواتار ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (who.isBlank()) "سلام 👋" else "سلام $who 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Chai.Text
                )
                Text(
                    if (todaySeconds > 0) "امروز ${TimeFmt.humanHours(todaySeconds)} تمرکز — عالی پیش می‌ری"
                    else "امروز اولین فنجان چای توئه",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text2
                )
            }
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Chai.Soft),
                contentAlignment = Alignment.Center
            ) { Text("☕", style = MaterialTheme.typography.titleLarge) }
        }

        // ── بنر دسترسی‌ها ──
        PermissionBanner(
            perms = perms,
            onOpenAccessibility = onOpenAccessibility,
            onOpenUsage = onOpenUsage,
            modifier = Modifier.padding(top = 12.dp)
        )

        // ── هیروی فوکوس ──
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .background(Chai.heroBrush)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "آماده‌ای؟",
                        style = MaterialTheme.typography.labelMedium,
                        color = Chai.OnHero.copy(alpha = 0.8f)
                    )
                    Text(
                        "یک فنجان چای\nبرای تمرکز",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Chai.OnHero
                    )
                }
                if (streak > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${faNum(streak)} روز",
                            style = MaterialTheme.typography.labelMedium,
                            color = Chai.OnHero
                        )
                    }
                }
            }

            TeaCup(
                progress = 0f,
                remainingSec = 0,
                showTimer = false,
                modifier = Modifier
                    .size(132.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                "${faNum(minutes)} دقیقه · هدف: ${goal.label}",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.OnHero.copy(alpha = 0.86f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(15, 25, 30, 45, 60).forEach { m ->
                    HeroMinutesChip(
                        minutes = m,
                        selected = m == minutes,
                        onClick = { minutes = m },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Chai.OnHero)
                    .clickable { onStart(goal, minutes) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "☕  شروع تمرکز",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Chai.HeroTop
                )
            }
        }

        // ── انتخاب هدف ──
        Spacer(Modifier.height(12.dp))
        ChaiCard(index = 1) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("می‌خوام…", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
                Text("هدف امروز", style = MaterialTheme.typography.bodySmall, color = Chai.Text2)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Goal.entries.forEach { g ->
                    GoalTile(
                        goal = g,
                        selected = g == goal,
                        onClick = { goal = g },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── آمار امروز ──
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat(
                value = "${faNum(todaySeconds / 60)}′",
                label = "امروز",
                modifier = Modifier.weight(1f)
            )
            MiniStat(
                value = faNum(streak),
                label = "روز پیاپی",
                modifier = Modifier.weight(1f)
            )
            MiniStat(
                value = faNum(sessionCount),
                label = "جلسه",
                modifier = Modifier.weight(1f)
            )
        }
        if (todayOpens > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                "امروز چای ${faNum(todayOpens)} بار باز شد",
                style = MaterialTheme.typography.labelSmall,
                color = Chai.Text2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // ── شمارندهٔ زنده (فقط عدد واقعی؛ اگر یکی باشی، صادقانه می‌گوید) ──
        if (showActive && activeNow > 0) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Chai.Surface)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Chai.Primary)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (activeNow > 1) "همین الان ${faNum(activeNow)} نفر مثل تو تمرکز می‌کنن"
                        else "همین الان فقط تو بیداری و تمرکز می‌کنی",
                        style = MaterialTheme.typography.titleMedium,
                        color = Chai.Text
                    )
                    Text(
                        if (activeNow > 1) "به جمع ما بپیوند" else "قهوه‌ات را دم کن، بقیه هم می‌آیند",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
                Text(if (activeNow > 1) "👥" else "☕", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun GoalIcon(goal: Goal) {
    Image(
        painter = painterResource(goalRes(goal)),
        contentDescription = goal.label,
        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(19.dp))
    )
}

/** آیکون هر هدف (منبع drawable) */
fun goalRes(goal: Goal): Int = when (goal) {
    Goal.STUDY -> R.drawable.ic_chai_goal_study
    Goal.BOOK -> R.drawable.ic_chai_goal_book
    Goal.WORK -> R.drawable.ic_chai_goal_work
    Goal.GYM -> R.drawable.ic_chai_goal_gym
    Goal.SLEEP -> R.drawable.ic_chai_goal_sleep
}

/** کاشی هدف در صفحهٔ خانه — وقتی انتخاب شده سبز چای می‌شود */
@Composable
private fun GoalTile(
    goal: Goal,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Chai.Primary else Chai.Surface2,
        animationSpec = tween(200),
        label = "goalTileBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else Chai.Text2,
        animationSpec = tween(200),
        label = "goalTileFg"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 9.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(goalRes(goal)),
            contentDescription = null,
            modifier = Modifier.size(22.dp).clip(RoundedCornerShape(7.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(
            goal.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = fg,
            maxLines = 1
        )
    }
}

/** چیپ مدت داخل هیرو — شیشه‌ای روی سبز */
@Composable
private fun HeroMinutesChip(
    minutes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Chai.OnHero else Color.White.copy(alpha = 0.14f),
        animationSpec = tween(200),
        label = "heroChipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Chai.HeroTop else Chai.OnHero,
        animationSpec = tween(200),
        label = "heroChipFg"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            faNum(minutes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg
        )
    }
}

/* ───────────────────────── کمکی‌ها ───────────────────────── */

/** نسخهٔ برنامه */
private fun appVersion(ctx: Context): String = try {
    val p = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
    "نسخهٔ ${p.versionName}"
} catch (e: Exception) {
    "نسخهٔ ۲.۰.۱"
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
