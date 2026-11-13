package ir.chai.focus.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** یک اپ نصب‌شده روی دستگاه */
data class InstalledApp(val pkg: String, val label: String)

/** خواندن فهرست اپ‌های قابل اجرا */
suspend fun loadInstalledApps(ctx: Context): List<InstalledApp> = withContext(Dispatchers.IO) {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    try {
        pm.queryIntentActivities(intent, 0)
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString().trim()) }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * انتخاب اپ‌هایی که در زمان تمرکز قفل شوند.
 * اپ‌های پیش‌فرض (تلگرام و…) همیشه قفل می‌شوند؛ اینجا می‌توانی اضافه کنی.
 */
@Composable
fun BlockedAppsScreen(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { apps = loadInstalledApps(ctx) }

    val filtered = remember(apps, query) {
        val list = apps.orEmpty()
        if (query.isBlank()) list
        else list.filter {
            it.label.contains(query.trim(), ignoreCase = true) ||
                it.pkg.contains(query.trim(), ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Chai.Text
                    )
                }
                Spacer(Modifier.size(4.dp))
                Column(Modifier.weight(1f)) {
                    Text("اپ‌های قفل‌شونده", style = MaterialTheme.typography.titleLarge, color = Chai.Text)
                    Text(
                        "${selected.size} اپ انتخاب شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = Chai.Text2
                    )
                }
            }
        }

        item {
            Text(
                "تلگرام، اینستاگرام، یوتیوب و… همیشه بر اساس هدف قفل می‌شوند. " +
                    "هر اپ دیگری که اینجا انتخاب کنی هم در هر جلسه قفل خواهد شد.",
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("جست‌وجوی اپ") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (apps == null) {
            item {
                Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Chai.Primary)
                }
            }
        } else if (filtered.isEmpty()) {
            item {
                Text(
                    "اپی پیدا نشد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Chai.Text2
                )
            }
        } else {
            items(filtered, key = { it.pkg }) { app ->
                val isOn = app.pkg in selected
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Chai.Surface)
                        .clickable { onToggle(app.pkg) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            app.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = Chai.Text,
                            fontWeight = if (isOn) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            app.pkg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Chai.Text2
                        )
                    }
                    if (isOn) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "قفل می‌شود",
                            tint = Chai.Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggle(app.pkg) }
                        )
                    }
                }
            }
        }
    }
}
