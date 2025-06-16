package com.example.myfirstapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val UniversalDarkColors = darkColorScheme(
    primary = Color(0xFF7E7E7E),
    onPrimary = Color(0xFFE0E0E0),
    primaryContainer = Color(0xFF555555),
    onPrimaryContainer = Color(0xFFF0F0F0),

    secondary = Color(0xFF6B8F71),
    onSecondary = Color(0xFFD3E6D3),
    secondaryContainer = Color(0xFF445A44),
    onSecondaryContainer = Color(0xFFDEEEE0),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE1E1E1),

    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFE1E1E1),

    error = Color(0xFF565656),
    onError = Color(0xFFFFDAD6),
    errorContainer = Color(0xFF790000),
    onErrorContainer = Color(0xFFFFDAD6),

    surfaceVariant = Color(0xFF2B2B2B),
    onSurfaceVariant = Color(0xFFA0A0A0),

    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF414141),

    inverseSurface = Color(0xFFE1E1E1),
    inverseOnSurface = Color(0xFF121212),
    inversePrimary = Color(0xFF7E7E7E),

    scrim = Color(0xFF000000), // блюр

    surfaceDim = Color(0xFF2E2E2E),
    surfaceBright = Color(0xFF4E4E4E),

    surfaceContainerLowest = Color(0xFF121212),
    surfaceContainerLow = Color(0xFF1B1B1B), // меню
    surfaceContainer = Color(0xFF282828), // шапка
    surfaceContainerHigh = Color(0xFF3B3B3B),
    surfaceContainerHighest = Color(0xFF505050),
)


val BlueColors = lightColorScheme(
    primary = Color(0xCC3B69B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3FF),
    onPrimaryContainer = Color(0xFF001E48),
    secondary = Color(0xFF5B7CCC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E2FF),
    onSecondaryContainer = Color(0xFF001B44),
    background = Color(0xFFF5F7FD),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
    )

val RedColors = lightColorScheme(
    primary = Color(0xCCB14348),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF8D8D9),
    onPrimaryContainer = Color(0xFF410006),
    secondary = Color(0xFF9F5050),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF9DADD),
    onSecondaryContainer = Color(0xFF410005),
    background = Color(0xFFFDF7F7),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)

val GreenColors = lightColorScheme(
    primary = Color(0xCC3A7A41),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EAD6),
    onPrimaryContainer = Color(0xFF0B210B),
    secondary = Color(0xFF588A51),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEAD4),
    onSecondaryContainer = Color(0xFF0D270C),
    background = Color(0xFFF5FBF6),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)

val YellowColors = lightColorScheme(
    primary = Color(0xCCAA8F1A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6EDBB),
    onPrimaryContainer = Color(0xFF2E2700),
    secondary = Color(0xFFB59F40),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFF6EFB9),
    onSecondaryContainer = Color(0xFF2F2A00),
    background = Color(0xFFFEFCE8),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)

val OrangeColors = lightColorScheme(
    primary = Color(0xCCB4661B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDDDB3),
    onPrimaryContainer = Color(0xFF3F2600),
    secondary = Color(0xFFB1722F),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFDDFAF),
    onSecondaryContainer = Color(0xFF3E2500),
    background = Color(0xFFFEF9F1),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)

val PurpleColors = lightColorScheme(
    primary = Color(0xCC64427A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7D7F0),
    onPrimaryContainer = Color(0xFF23143C),
    secondary = Color(0xFF73548B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8D7F0),
    onSecondaryContainer = Color(0xFF271846),
    background = Color(0xFFF9F7FB),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)

val TealColors = lightColorScheme(
    primary = Color(0xCC1E7F7F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8DAD9),
    onPrimaryContainer = Color(0xFF00201F),
    secondary = Color(0xFF207070),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9DDD6),
    onSecondaryContainer = Color(0xFF00201F),
    background = Color(0xFFF3F9F9),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)

val BrownColors = lightColorScheme(
    primary = Color(0xCC5A4639),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9CFC9),
    onPrimaryContainer = Color(0xFF20160E),
    secondary = Color(0xFF6C5447),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4C7BA),
    onSecondaryContainer = Color(0xFF25180E),
    background = Color(0xFFFAF7F3),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xCC919191),
    onError = Color(0xFFFFFFFF),
)


@Composable
fun MyFirstAppTheme(
    colorScheme: ColorScheme = RedColors,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}