package com.elishaazaria.sayboard.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Green500,
    primaryVariant = Green700,
    secondary = Orange700,
    secondaryVariant = Orange900,

    background = Color(0xFF1F1F1F),
    surface = Color(0xFF212121),
)

private val LightColorPalette = lightColors(
    primary = Green500,
    primaryVariant = Green700,
    secondary = Orange700,
    secondaryVariant = Orange900,

    background = Color(0xFFFFFFFF),
    surface = Color(0xFFE7E7E7),

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme)
        DarkColorPalette
    else LightColorPalette

    MaterialTheme(
        colors = colors,
        shapes = Shapes,
        content = content,
    )
}