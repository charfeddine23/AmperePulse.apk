package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Neumorphism Base & Surfaces
val NeumorphBgLight = Color(0xFFF2F6FA)       // Soft off-white / grey-blue base
val NeumorphSurfaceLight = Color(0xFFFFFFFF)  // Crisp white surface
val NeumorphShadowLight = Color(0x33A3B8CC)   // Soft bottom-right diffusion shadow
val NeumorphHighlightLight = Color(0xE6FFFFFF)// Top-left specular highlight
val NeumorphBorderLight = Color(0x66E2E8F0)   // Subtle tactile border

// Dark Mode Base & Surfaces
val NeumorphBgDark = Color(0xFF0F172A)        // Deep slate-charcoal base
val NeumorphSurfaceDark = Color(0xFF1E293B)   // Slate glass card surface
val NeumorphShadowDark = Color(0x66000000)    // Dark depth shadow
val NeumorphHighlightDark = Color(0x1F94A3B8) // Muted top highlight
val NeumorphBorderDark = Color(0x33334155)    // Slate dark border

// High-Contrast Typography Colors
val TextCharcoal = Color(0xFF1E293B)          // Dark charcoal text for light mode
val TextSlate = Color(0xFF64748B)             // Secondary slate text
val TextMuted = Color(0xFF94A3B8)             // Muted caption text
val TextWhite = Color(0xFFF8FAFC)             // Primary text for dark mode
val TextMutedDark = Color(0xFF94A3B8)         // Secondary text for dark mode

// Accent Color Palette
val TactileGreen = Color(0xFF00C853)          // Positive / Income / Storing Green (#00C853)
val TactileCoralRed = Color(0xFFFF3D00)       // Expense / Warning / Heating Red (#FF3D00)
val TactileRoyalBlue = Color(0xFF2962FF)      // Action / Primary Blue (#2962FF)
val TactileCyan = Color(0xFF00B0FF)           // Cooling / Cryo Cyan
val TactileViolet = Color(0xFF7C4DFF)         // Deep Sleep / Cosmic Violet
val TactileAmber = Color(0xFFFFAB00)          // Shield / Alert Amber

// Glass Overlays
val GlassWhite80 = Color(0xCCFFFFFF)
val GlassWhite40 = Color(0x66FFFFFF)
val GlassDark80 = Color(0xCC1E293B)
val GlassDark40 = Color(0x661E293B)

// Compatibility & Legacy Aliases
val NeonPhosphorusGreen = TactileGreen
val NeonCyan = TactileCyan
val CyberViolet = TactileViolet
val SolarAmber = TactileAmber
val ElectricMint = TactileGreen
val TextLuminous = TextWhite
val TextMutedGreen = TextSlate
val ObsidianOled = NeumorphBgDark
