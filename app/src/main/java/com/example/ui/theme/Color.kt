package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Strict Gemini Dark Palette
val GeminiBackground = Color(0xFF0C0D0E)
val GeminiSurface = Color(0xFF16171A)
val GeminiSurfaceContainer = Color(0xFF1E1F22)
val GeminiSurfaceHigh = Color(0xFF282A2C)
val GeminiBorder = Color(0xFF24262B)
val GeminiBorderSubtle = Color(0xFF1E2024)

// OpenCodeMovil Official Logo Gradient & Accents (Azul - Esmeralda)
val OpenCodeBlue = Color(0xFF2979FF)
val OpenCodeCyan = Color(0xFF00E5FF)
val OpenCodeEmerald = Color(0xFF00E676)
val OpenCodeMint = Color(0xFF1DE9B6)
val OpenCodeGlow = Color(0xFF00E5FF)

val OpenCodeGradient = Brush.linearGradient(
    listOf(OpenCodeBlue, OpenCodeCyan, OpenCodeEmerald)
)

val OpenCodeButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF1E6AFF), Color(0xFF00D2B4), Color(0xFF00E676))
)

// Accent Colors
val GeminiSparkBlue = Color(0xFF7CA5FF)
val GeminiSparkPurple = Color(0xFFB388FF)
val GeminiSparkCyan = Color(0xFF4DD0E1)
val GeminiAccent = Color(0xFF8AB4F8)

// Text Colors
val GeminiTextPrimary = Color(0xFFE3E3E3)
val GeminiTextSecondary = Color(0xFF9AA0A6)
val GeminiTextMuted = Color(0xFF6E7176)

// Message Bubbles
val GeminiBubbleUser = Color(0xFF282A2C)
val GeminiBubbleAi = Color(0xFF16171A)

// Code block theme
val CodeBackground = Color(0xFF111215)
val CodeHeaderBackground = Color(0xFF1A1C20)
val CodeKeyword = Color(0xFFFF7B72)
val CodeString = Color(0xFFA5D6FF)
val CodeComment = Color(0xFF8B949E)
val CodeFunction = Color(0xFFD2A8FF)

// Status
val StatusGreen = Color(0xFF4CAF50)
val StatusAmber = Color(0xFFFFB74D)
val StatusRed = Color(0xFFEF5350)
