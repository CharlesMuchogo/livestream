package com.charlesmuchogo.livestream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.charlesmuchogo.livestream.resources.Res
import com.charlesmuchogo.livestream.resources.barlow_condensed_bold
import com.charlesmuchogo.livestream.resources.barlow_condensed_extrabold
import com.charlesmuchogo.livestream.resources.barlow_condensed_semibold
import com.charlesmuchogo.livestream.resources.manrope
import org.jetbrains.compose.resources.Font

object Floodlight {
    val Ink = Color(0xFF0B0D12)
    val Surface = Color(0xFF151922)
    val SurfaceRaised = Color(0xFF1E2330)
    val Outline = Color(0xFF262C3A)
    val Divider = Color(0xFF1C212C)
    val Text = Color(0xFFF2F4F8)
    val TextMuted = Color(0xFFA3ABBD)
    val TextFaint = Color(0xFF6F788C)
    val Volt = Color(0xFFC6F432)
    val Live = Color(0xFFFF5C5C)
    val LiveText = Color(0xFFFF7A7A)
    val Verified = Color(0xFF5CB8FF)
    val Good = Color(0xFF9BE59B)
    val Warn = Color(0xFFFFB547)
    val CrestTones = listOf(Color(0xFF2A3142), Color(0xFF33293F), Color(0xFF203535), Color(0xFF3A2E24), Color(0xFF24304A))
}

@Composable
fun displayFamily() = FontFamily(
    Font(Res.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(Res.font.barlow_condensed_bold, FontWeight.Bold),
    Font(Res.font.barlow_condensed_extrabold, FontWeight.ExtraBold),
)

@Composable
fun bodyFamily() = FontFamily(
    listOf(400, 500, 600, 700, 800).map { weight ->
        Font(
            Res.font.manrope,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
        )
    },
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val display = displayFamily()
    val body = bodyFamily()
    val typography = Typography(
        displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = 0.02.em),
        titleLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = 0.02.em),
        titleMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, lineHeight = 18.sp),
        labelMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = body, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.08.em),
    )
    val colors = darkColorScheme(
        primary = Floodlight.Volt,
        onPrimary = Floodlight.Ink,
        secondary = Floodlight.Verified,
        background = Floodlight.Ink,
        onBackground = Floodlight.Text,
        surface = Floodlight.Ink,
        onSurface = Floodlight.Text,
        surfaceVariant = Floodlight.Surface,
        onSurfaceVariant = Floodlight.TextMuted,
        surfaceContainer = Floodlight.Surface,
        surfaceContainerHigh = Floodlight.SurfaceRaised,
        outline = Floodlight.Outline,
        outlineVariant = Floodlight.Divider,
        error = Floodlight.Live,
        inverseSurface = Floodlight.Text,
        inverseOnSurface = Floodlight.Ink,
        inversePrimary = Floodlight.Ink,
    )
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
