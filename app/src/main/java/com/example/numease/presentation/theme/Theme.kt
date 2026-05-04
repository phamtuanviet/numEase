package com.example.numease.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.numease.ui.theme.DarkOnPrimary
import com.example.numease.ui.theme.DarkOnPrimaryContainer
import com.example.numease.ui.theme.DarkOnSecondary
import com.example.numease.ui.theme.DarkOnSecondaryContainer
import com.example.numease.ui.theme.DarkOnTertiary
import com.example.numease.ui.theme.DarkPrimary
import com.example.numease.ui.theme.DarkPrimaryContainer
import com.example.numease.ui.theme.DarkSecondary
import com.example.numease.ui.theme.DarkSecondaryContainer
import com.example.numease.ui.theme.DarkTertiary
import com.example.numease.ui.theme.LightPrimary
import com.example.numease.ui.theme.DarkTertiaryContainer
import com.example.numease.ui.theme.DarkOnTertiaryContainer
import com.example.numease.ui.theme.DarkBackground
import com.example.numease.ui.theme.DarkOnBackground
import com.example.numease.ui.theme.DarkSurface
import com.example.numease.ui.theme.DarkOnSurface

import com.example.numease.ui.theme.LightOnPrimary
import com.example.numease.ui.theme.LightPrimaryContainer
import com.example.numease.ui.theme.LightOnPrimaryContainer
import com.example.numease.ui.theme.LightSecondary
import com.example.numease.ui.theme.LightOnSecondary
import com.example.numease.ui.theme.LightSecondaryContainer
import com.example.numease.ui.theme.LightOnSecondaryContainer
import com.example.numease.ui.theme.LightTertiary
import com.example.numease.ui.theme.LightOnTertiary
import com.example.numease.ui.theme.LightTertiaryContainer
import com.example.numease.ui.theme.LightOnTertiaryContainer
import com.example.numease.ui.theme.LightBackground
import com.example.numease.ui.theme.LightOnBackground
import com.example.numease.ui.theme.LightSurface
import com.example.numease.ui.theme.LightOnSurface



private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)





private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,

    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)



@Composable
fun NumEaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}