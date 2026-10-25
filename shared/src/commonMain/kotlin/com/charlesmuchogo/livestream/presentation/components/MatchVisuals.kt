package com.charlesmuchogo.livestream.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.charlesmuchogo.livestream.ui.theme.Floodlight
import com.charlesmuchogo.livestream.ui.theme.displayFamily
import com.charlesmuchogo.livestream.utils.initials

@Composable
fun TeamCrest(name: String, logo: String, size: Dp = 34.dp, modifier: Modifier = Modifier) {
    var logoFailed by remember(logo) { mutableStateOf(logo.isBlank()) }
    val tone = Floodlight.CrestTones[(name.hashCode() and Int.MAX_VALUE) % Floodlight.CrestTones.size]
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(tone),
        contentAlignment = Alignment.Center,
    ) {
        if (logoFailed) {
            Text(
                text = initials(name),
                fontFamily = displayFamily(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.38f).sp,
                color = Floodlight.Text,
            )
        } else {
            AsyncImage(
                model = logo,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onError = { logoFailed = true },
                modifier = Modifier.fillMaxSize().padding(size * 0.14f),
            )
        }
    }
}

@Composable
fun PulsingDot(color: Color = Floodlight.Live, size: Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val ring by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "ring",
    )
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                drawCircle(color.copy(alpha = 0.5f * (1f - ring)), radius = this.size.minDimension / 2 * (1f + ring * 1.2f))
                drawCircle(color)
            },
    )
}

@Composable
fun LivePill(text: String = "LIVE", modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PulsingDot(size = 7.dp)
        Text(text, style = MaterialTheme.typography.labelSmall, color = Floodlight.LiveText)
    }
}

@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = Color(0xFFC9CFDB),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Floodlight.SurfaceRaised)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun SaveButton(saved: Boolean, label: String, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (saved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (saved) "Remove $label from Saved" else "Save $label",
            tint = if (saved) Floodlight.Live else Floodlight.TextMuted,
        )
    }
}
