package com.example.data

/**
 * Supported Application Languages.
 * When ARABIC is selected:
 * 1. CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) flips all UI layout horizontally (RTL).
 * 2. All titles, subtitles, metrics, buttons, tab labels, and dialogs display in fluent Modern Standard Arabic.
 */
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    ARABIC("ar", "Arabic", "العربية")
}
