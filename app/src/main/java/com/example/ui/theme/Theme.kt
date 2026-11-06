package com.example.ui.theme

import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    zahraTheme: ZahraThemeMode = ZahraThemeMode.SWEET_SAKURA_PUFFY,
    fontStyle: ZahraFontStyle = ZahraFontStyle.FANTASY_ROUND,
    content: @Composable () -> Unit
) {
    val isDarkPalette = zahraTheme == ZahraThemeMode.OBSIDIAN_BLACK_GOLD
    val colorScheme = if (isDarkPalette) {
        darkColorScheme(
            primary = zahraTheme.primaryColor,
            onPrimary = Color(0xFF17130A),
            secondary = zahraTheme.secondaryColor,
            onSecondary = Color(0xFF17130A),
            tertiary = zahraTheme.accentGold,
            background = zahraTheme.backgroundColor,
            onBackground = Color(0xFFFFF8E7),
            surface = zahraTheme.cardBackground,
            onSurface = Color(0xFFFFF8E7),
            surfaceVariant = Color(0xFF292316),
            onSurfaceVariant = Color(0xFFE8DDBF),
            outline = zahraTheme.accentGold.copy(alpha = 0.55f)
        )
    } else {
        lightColorScheme(
            primary = zahraTheme.primaryColor,
            onPrimary = Color.White,
            secondary = zahraTheme.secondaryColor,
            onSecondary = Color(0xFF171717),
            tertiary = zahraTheme.accentGold,
            background = zahraTheme.backgroundColor,
            onBackground = Color(0xFF25212A),
            surface = zahraTheme.cardBackground,
            onSurface = Color(0xFF25212A),
            surfaceVariant = zahraTheme.backgroundColor,
            onSurfaceVariant = Color(0xFF514A55),
            outline = zahraTheme.buttonBevel.copy(alpha = 0.5f)
        )
    }

    val baseTypography = MaterialTypography()
    val selectedTypography = MaterialTypography(
        displayLarge = baseTypography.displayLarge.copy(fontFamily = fontStyle.fontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontFamily = fontStyle.fontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontFamily = fontStyle.fontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = fontStyle.fontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = fontStyle.fontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontFamily = fontStyle.fontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = fontStyle.fontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = fontStyle.fontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontFamily = fontStyle.fontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = fontStyle.fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = fontStyle.fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = fontStyle.fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = fontStyle.fontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = fontStyle.fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = fontStyle.fontFamily)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = selectedTypography,
        content = content
    )
}
