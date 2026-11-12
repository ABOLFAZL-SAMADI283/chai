package ir.chai.focus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.chai.focus.PermState

/**
 * بنر هشدار روی صفحهٔ خانه وقتی دسترسی‌ها کامل نیست.
 * کاربر را مستقیم به صفحهٔ درست تنظیمات می‌برد.
 */
@Composable
fun PermissionBanner(
    perms: PermState,
    onOpenAccessibility: () -> Unit,
    onOpenUsage: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !perms.allGranted,
        enter = fadeIn(tween(220)) + expandVertically(tween(260)),
        exit = fadeOut(tween(180)) + shrinkVertically(tween(220)),
        modifier = modifier
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Chai.Milk)
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Chai.Gold2, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(
                    "برای قفل کردن اپ‌ها یک دسترسی لازم است",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Chai.Text
                )
            }
            Spacer(Modifier.height(9.dp))
            if (!perms.accessibility) {
                PermissionLine(
                    icon = Icons.Filled.Lock,
                    title = "دسترسی Accessibility",
                    subtitle = "بدون این، اپ‌های حواس‌پرت‌کن قفل نمی‌شوند",
                    granted = false,
                    onClick = onOpenAccessibility
                )
            }
            if (!perms.usageAccess) {
                PermissionLine(
                    icon = Icons.Filled.Info,
                    title = "دسترسی Usage access",
                    subtitle = "برای نمایش زمان واقعی استفاده از اپ‌ها",
                    granted = false,
                    onClick = onOpenUsage
                )
            }
        }
    }
}

/**
 * کارت کامل دسترسی‌ها (صفحهٔ تنظیمات) — وضعیت + راهنمای «Protected apps».
 */
@Composable
fun PermissionCard(
    perms: PermState,
    onOpenAccessibility: () -> Unit,
    onOpenUsage: () -> Unit,
    onOpenAppDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Chai.Surface)
            .padding(14.dp)
    ) {
        Text("دسترسی‌های لازم", style = MaterialTheme.typography.titleMedium, color = Chai.Text)
        Spacer(Modifier.height(10.dp))

        PermissionLine(
            icon = Icons.Filled.Lock,
            title = "دسترسی Accessibility",
            subtitle = "برای قفل کردن تلگرام و اپ‌های حواس‌پرت‌کن",
            granted = perms.accessibility,
            onClick = onOpenAccessibility
        )
        PermissionLine(
            icon = Icons.Filled.Info,
            title = "دسترسی Usage access",
            subtitle = "برای محاسبهٔ زمان واقعی استفاده از اپ‌ها",
            granted = perms.usageAccess,
            onClick = onOpenUsage
        )

        if (!perms.accessibility) {
            Spacer(Modifier.height(6.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Chai.Soft)
                    .padding(12.dp)
            ) {
                Text(
                    "گوشی اجازه نداد؟",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Chai.Primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "اگر اپ را از مرورگر یا پیام‌رسان نصب کرده‌ای، اندروید آن را «برنامهٔ محافظت‌شده» " +
                        "(Protected) می‌داند و اجازهٔ روشن‌کردن Accessibility را نمی‌دهد.\n" +
                        "راه‌حل: تنظیمات ← برنامه‌ها ← چای ← «اجازهٔ تنظیمات محدود» را روشن کن، " +
                        "بعد برگرد و دسترسی Accessibility را فعال کن.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Chai.Text
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Chai.Primary)
                        .clickable { onOpenAppDetails() }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("باز کردن صفحهٔ برنامه", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}

/** یک ردیف وضعیت دسترسی */
@Composable
fun PermissionLine(
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (granted) 0.55f else 1f, tween(220), label = "permAlpha")
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = !granted) { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (granted) Chai.Soft else Chai.Milk),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (granted) Chai.Primary else Chai.Text2,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Chai.Text.copy(alpha = alpha)
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Chai.Text2.copy(alpha = alpha)
            )
        }
        if (granted) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "فعال", tint = Chai.Primary, modifier = Modifier.size(20.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("روشن کن", style = MaterialTheme.typography.labelMedium, color = Chai.Primary)
                Spacer(Modifier.width(3.dp))
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Chai.Primary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
