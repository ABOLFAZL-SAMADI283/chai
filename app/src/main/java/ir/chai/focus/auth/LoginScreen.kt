package ir.chai.focus.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.chai.focus.ui.Chai
import ir.chai.focus.ui.ChaiLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * صفحهٔ ورود/ثبت‌نام «چای».
 *  - ثبت‌نام: نام کاربری (با بررسی آزاد بودن + پیشنهاد جایگزین) → کد بازیابی یک‌بار
 *  - ورود: نام کاربری + کد بازیابی
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    repo: AuthRepository,
    online: Boolean = true,
    onLoggedIn: (String) -> Unit,
    onSkip: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRegister by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var showCode by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf<RecoveryResult?>(null) }
    var saved by remember { mutableStateOf(false) }

    var nameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var checking by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // بررسی خودکار با تأخیر (debounce) هنگام تایپ
    LaunchedEffect(username, isRegister) {
        if (!isRegister) {
            nameAvailable = null; suggestions = emptyList(); return@LaunchedEffect
        }
        if (username.trim().length < 3) {
            nameAvailable = null; suggestions = emptyList(); return@LaunchedEffect
        }
        delay(450)
        checking = true
        repo.checkUsername(username.trim(), wantSuggestions = true)
            .onSuccess {
                nameAvailable = it.available
                suggestions = it.suggestions
            }
            .onFailure { nameAvailable = null }
        checking = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Chai.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── لوگو ──
        ChaiLogo(modifier = Modifier.size(112.dp), fill = 0.7f, showWord = false)
        Spacer(Modifier.height(10.dp))
        Text(
            "چای",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Chai.Teal
        )
        Text(
            "۳۰ دقیقه تمرکز، یک ماگ چای",
            style = MaterialTheme.typography.bodyMedium,
            color = Chai.Text2
        )

        Spacer(Modifier.height(22.dp))

        // ── کارت فرم ──
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Chai.Surface)
                .padding(18.dp)
        ) {
            // سوییچ ثبت‌نام / ورود
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Chai.Milk)
                    .padding(4.dp)
            ) {
                TabPill("ثبت‌نام", isRegister, Modifier.weight(1f)) {
                    isRegister = true; error = ""; suggestions = emptyList()
                }
                TabPill("ورود", !isRegister, Modifier.weight(1f)) {
                    isRegister = false; error = ""; suggestions = emptyList()
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── نمایش کد بازیابی (فقط یک‌بار) ──
            val rec = recovery
            if (rec != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Chai.Milk)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "کد بازیابی تو",
                        fontWeight = FontWeight.Bold,
                        color = Chai.Teal,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        rec.code,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Chai.Tea,
                        letterSpacing = 3.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "این کد فقط همین یک‌بار نشان داده می‌شود.\nهمین الان ذخیره‌اش کن!",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Chai.Gold2
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip(
                            onClick = { copyToClipboard(ctx, rec.code) },
                            label = { Text("کپی کد") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        AssistChip(
                            onClick = { shareText(ctx, "کد بازیابی چای من: ${rec.code}") },
                            label = { Text("اشتراک‌گذاری") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { saved = !saved }
                    ) {
                        Checkbox(checked = saved, onCheckedChange = { saved = it })
                        Text("کد را ذخیره کردم", style = MaterialTheme.typography.bodyMedium, color = Chai.Text)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            // کد بازیابی را روی همین دستگاه نگه می‌داریم تا گم نشود
                            ir.chai.focus.Prefs.setSavedRecoveryCode(ctx, rec.code)
                            onLoggedIn(rec.username)
                        },
                        enabled = saved,
                        colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("بریم داخل چای") }
                }
                return@Column
            }

            // ── فرم ──
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("نام کاربری") },
                singleLine = true,
                isError = nameAvailable == false,
                supportingText = {
                    when {
                        !online -> Text("اینترنت قطع است — برای بررسی نام، وصل شو", color = Chai.Gold2)
                        checking -> Text("در حال بررسی…")
                        nameAvailable == true -> Text("✅ این نام آزاد است", color = Chai.Primary)
                        nameAvailable == false -> Text("❌ این نام گرفته شده", color = Chai.Danger)
                        else -> Text("۳ تا ۱۲ حرف یا رقم")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (isRegister) {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("نام نمایشی (اختیاری)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "پیشنهاد ما:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        suggestions.take(6).forEach { s ->
                            AssistChip(
                                onClick = {
                                    username = s; nameAvailable = null; suggestions = emptyList()
                                },
                                label = { Text(s) }
                            )
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("کد بازیابی") },
                    singleLine = true,
                    visualTransformation = if (showCode) androidx.compose.ui.text.input.VisualTransformation.None
                    else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCode = !showCode }) {
                            Icon(
                                if (showCode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showCode) "پنهان کن" else "نشان بده"
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Chai.Danger, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    busy = true; error = ""
                    scope.launch {
                        if (isRegister) {
                            repo.register(username.trim(), nickname.trim())
                                .onSuccess { recovery = it; busy = false }
                                .onFailure { error = it.message ?: "خطا"; busy = false }
                        } else {
                            repo.login(username.trim(), code.trim())
                                .onSuccess { onLoggedIn(it); busy = false }
                                .onFailure { error = it.message ?: "خطا"; busy = false }
                        }
                    }
                },
                enabled = !busy && online && username.trim().length >= 3 &&
                    (!isRegister || nameAvailable != false),
                colors = ButtonDefaults.buttonColors(containerColor = Chai.Primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(if (isRegister) "ساخت حساب" else "ورود", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onSkip) {
                Text("فعلاً بدون حساب ادامه می‌دم", color = Chai.Text2)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "با ساخت حساب، قوانین و سیاست حریم خصوصی را می‌پذیرید.",
            style = MaterialTheme.typography.bodySmall,
            color = Chai.Text2,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Chai.Primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Chai.Text2
        )
    }
}

private fun copyToClipboard(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("chai-recovery", text))
}

private fun shareText(ctx: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری کد بازیابی"))
}
