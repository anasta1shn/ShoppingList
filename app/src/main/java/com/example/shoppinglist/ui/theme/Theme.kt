
package com.example.shoppinglist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9BC7A0),
    secondary = Color(0xFFB7CBB2),
    tertiary = Color(0xFFE6BFA8),
    background = Color(0xFF171C18),
    surface = Color(0xFF222923),
    onPrimary = Color(0xFF18351F),
    onSecondary = Color(0xFF263B29),
    onTertiary = Color(0xFF402B20),
    onBackground = Color(0xFFE7EDE5),
    onSurface = Color(0xFFE7EDE5)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF42664D),
    secondary = Color(0xFF789477),
    tertiary = Color(0xFFC58F70),
    background = Color(0xFFF7F6F0),
    surface = Color(0xFFFFFEFA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF28352B),
    onSurface = Color(0xFF28352B)
)

@Composable
fun ShoppingListTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
