package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldDarkPrimary,
  onPrimary = EmeraldDarkOnPrimary,
  primaryContainer = EmeraldDarkPrimaryContainer,
  onPrimaryContainer = EmeraldDarkOnPrimaryContainer,
  secondary = MintDarkSecondary,
  onSecondary = MintDarkOnSecondary,
  secondaryContainer = MintDarkSecondaryContainer,
  onSecondaryContainer = MintDarkOnSecondaryContainer,
  tertiary = AmberDarkTertiary,
  onTertiary = AmberDarkOnTertiary,
  tertiaryContainer = AmberDarkTertiaryContainer,
  onTertiaryContainer = AmberDarkOnTertiaryContainer,
  error = CoralError,
  onError = CoralOnError,
  errorContainer = CoralErrorContainer,
  onErrorContainer = CoralOnErrorContainer,
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkSurface,
  onSurface = DarkOnBackground,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkMutedSlate,
  outline = DarkOutline,
  outlineVariant = DarkOutlineVariant,
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = EmeraldOnPrimary,
  primaryContainer = EmeraldPrimaryContainer,
  onPrimaryContainer = EmeraldOnPrimaryContainer,
  secondary = MintSecondary,
  onSecondary = MintOnSecondary,
  secondaryContainer = MintSecondaryContainer,
  onSecondaryContainer = MintOnSecondaryContainer,
  tertiary = AmberTertiary,
  onTertiary = AmberOnTertiary,
  tertiaryContainer = AmberTertiaryContainer,
  onTertiaryContainer = AmberOnTertiaryContainer,
  error = CoralError,
  onError = CoralOnError,
  errorContainer = CoralErrorContainer,
  onErrorContainer = CoralOnErrorContainer,
  background = AlabasterBackground,
  onBackground = CharcoalOnBackground,
  surface = PureSurface,
  onSurface = CharcoalOnBackground,
  surfaceVariant = SurfaceWarmVariant,
  onSurfaceVariant = MutedSlate,
  outline = SubtleOutline,
  outlineVariant = SubtleOutlineVariant,
)

@Composable
fun MeuBolsoTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content,
  )
}
