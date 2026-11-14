package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Eye-Friendly Soft Stone & Nordic Mist Light Mode Palette (Zero Harsh White Glare)
val NeumorphBgLight = Color(0xFFDCE3EC)         // Calm muted slate-mist base canvas
val NeumorphSurfaceLight = Color(0xFFE7ECF3)    // Soft satin ceramic card surface
val NeumorphCardTopLight = Color(0xFFECF0F6)    // Subtle top gradient sheen on cards
val NeumorphCardBottomLight = Color(0xFFE0E7F0) // Subtle bottom gradient depth on cards
val NeumorphInsetLight = Color(0xFFD2DBE6)      // Recessed grooves, pills, and tracks
val NeumorphShadowLight = Color(0x457589A2)     // Refined bottom-right diffusion shadow
val NeumorphHighlightLight = Color(0x7AF2F5FA)  // Soft silk top-left catchlight (never stark white)
val NeumorphBorderLight = Color(0x99BECAD8)     // Delicate stone border
val OnAccentLight = Color(0xFFF2F6FA)           // Soft porcelain text on solid accent buttons

// Dark Mode Base & Surfaces
val NeumorphBgDark = Color(0xFF0F172A)          // Deep slate-charcoal base
val NeumorphSurfaceDark = Color(0xFF1E293B)     // Slate glass card surface
val NeumorphShadowDark = Color(0x66000000)      // Dark depth shadow
val NeumorphHighlightDark = Color(0x1F94A3B8)   // Muted top highlight
val NeumorphBorderDark = Color(0x33334155)      // Slate dark border

// High-Contrast Eye-Comfort Typography Colors
val TextCharcoal = Color(0xFF1A2433)            // Deep ink slate for light mode (never harsh black)
val TextSlate = Color(0xFF4C5E75)               // Rich balanced slate (~5.5:1 contrast on #E7ECF3)
val TextMuted = Color(0xFF6B7E96)               // Soft muted caption text for light mode
val TextWhite = Color(0xFFF2F6FA)               // Soft ice-white text for dark mode
val TextMutedDark = Color(0xFF94A3B8)           // Secondary text for dark mode

// Accent Color Palette
val TactileGreen = Color(0xFF00C853)            // Positive / Income / Storing Green
val TactileCoralRed = Color(0xFFFF3D00)         // Expense / Warning / Heating Red
val TactileRoyalBlue = Color(0xFF2962FF)        // Action / Primary Blue
val TactileCyan = Color(0xFF00B0FF)             // Cooling / Cryo Cyan
val TactileViolet = Color(0xFF7C4DFF)           // Deep Sleep / Cosmic Violet
val TactileAmber = Color(0xFFFFAB00)            // Shield / Alert Amber

// Glass Overlays
val GlassWhite80 = Color(0xD9E8EDF4)
val GlassWhite40 = Color(0x73E8EDF4)
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
