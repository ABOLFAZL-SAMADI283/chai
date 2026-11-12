package ir.chai.focus.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Tea = Color(0xFF0F766E)
private val Cream = Color(0xFFF5F0E6)

/**
 * صفحهٔ ورود/ثبت‌نام «چای».
 * - ثبت‌نام: نام کاربری (با بررسی آزاد بودن + پیشنهاد جایگزین) → کد بازیابی یک‌بار
 * - ورود: نام کاربری + کد بازیابی
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    repo: AuthRepository,
    onLoggedIn: (String) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRegister by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf<RecoveryResult?>(null) }
    var saved by remember { mutableStateOf(false) }

    // وضعیت آزاد بودن نام کاربری
    var nameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var checking by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // بررسی خودکار با تأخیر (debounce) هنگام تایپ
    LaunchedEffect(username, isRegister) {
        if (!isRegister) { nameAvailable = null; suggestions = emptyList(); return@LaunchedEffect }
        if (username.trim().length < 3) { nameAvailable = null; suggestions = emptyList(); return@LaunchedEffect }
        delay(400)
        checking = true
        repo.checkUsername(username.trim(), wantSuggestions = true)
            .onSuccess {
                nameAvailable = it.available
                suggestions = it.suggestions
            }
            .onFailure { nameAvailable = null }
        checking = false
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Cream) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("☕", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text("چای", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Tea)
            Text("۳۰ دقیقه تمرکز، یک ماگ چای", color = Tea.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))

            // ── نمایش کد بازیابی (فقط یک‌بار) ──────────────────
            val rec = recovery
            if (rec != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("کد بازیابی تو", fontWeight = FontWeight.Bold, color = Tea)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            rec.code,
                            fontSize = 26.sp, fontWeight = FontWeight.Black,
                            color = Color(0xFF8B5E3C), letterSpacing = 3.sp, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "این کد فقط همین یک‌بار نشان داده می‌شود.\nهمین الان ذخیره‌اش کن!",
                            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                            color = Color(0xFFB45309)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row {
                            TextButton(onClick = { copyToClipboard(ctx, rec.code) }) { Text("کپی کد") }
                            TextButton(onClick = { shareText(ctx, "کد بازیابی چای من: ${rec.code}") }) { Text("اشتراک‌گذاری") }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = saved, onCheckedChange = { saved = it })
                            Text("کد را ذخیره کردم", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onLoggedIn(rec.username) },
                            enabled = saved,
                            colors = ButtonDefaults.buttonColors(containerColor = Tea),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("بریم داخل چای") }
                    }
                }
                return@Column
            }

            // ── فرم ورود/ثبت‌نام ────────────────────────────────
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("نام کاربری") },
                singleLine = true,
                isError = nameAvailable == false,
                supportingText = {
                    when {
                        checking -> Text("در حال بررسی…")
                        nameAvailable == true -> Text("✅ این نام آزاد است", color = Color(0xFF15803D))
                        nameAvailable == false -> Text("❌ این نام گرفته شده", color = Color(0xFFB91C1C))
                        else -> Text("۳ تا ۱۲ حرف یا رقم")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("نام نمایشی (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // پیشنهاد نام‌های جایگزین
                if (suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "پیشنهاد ما:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Tea.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        suggestions.forEach { s ->
                            AssistChip(
                                onClick = { username = s; nameAvailable = null; suggestions = emptyList() },
                                label = { Text(s) }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("کد بازیابی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color(0xFFB91C1C), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
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
                enabled = !busy && username.trim().length >= 3 && (!isRegister || nameAvailable != false),
                colors = ButtonDefaults.buttonColors(containerColor = Tea),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (busy) "..." else if (isRegister) "ساخت حساب" else "ورود")
            }

            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { isRegister = !isRegister; error = ""; suggestions = emptyList() }) {
                Text(if (isRegister) "حساب داری؟ وارد شو" else "حساب نداری؟ ثبت‌نام کن")
            }
        }
    }
}

private fun copyToClipboard(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("chai-recovery", text))
}

private fun shareText(ctx: Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(android.content.Intent.createChooser(intent, "اشتراک‌گذاری کد بازیابی"))
}
