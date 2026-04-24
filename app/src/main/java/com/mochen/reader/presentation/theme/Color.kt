package com.mochen.reader.presentation.theme

import androidx.compose.ui.graphics.Color

// Light Theme
val PrimaryLight = Color(0xFF6750A4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFEADDFF)
val OnPrimaryContainerLight = Color(0xFF21005D)

val SecondaryLight = Color(0xFF625B71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)

val TertiaryLight = Color(0xFF7D5260)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD8E4)
val OnTertiaryContainerLight = Color(0xFF31111D)

val ErrorLight = Color(0xFFB3261E)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFF9DEDC)
val OnErrorContainerLight = Color(0xFF410E0B)

val BackgroundLight = Color(0xFFFFFBFE)
val OnBackgroundLight = Color(0xFF1C1B1F)
val SurfaceLight = Color(0xFFFFFBFE)
val OnSurfaceLight = Color(0xFF1C1B1F)
val SurfaceVariantLight = Color(0xFFE7E0EC)
val OnSurfaceVariantLight = Color(0xFF49454F)

val OutlineLight = Color(0xFF79747E)
val OutlineVariantLight = Color(0xFFCAC4D0)

// Dark Theme
val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)

val BackgroundDark = Color(0xFF1C1B1F)
val OnBackgroundDark = Color(0xFFE6E1E5)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)

val OutlineDark = Color(0xFF938F99)
val OutlineVariantDark = Color(0xFF49454F)

// Reader Theme Colors
object ReaderColors {
    // Background colors
    val WhiteBg = Color(0xFFFFFFFF)
    val CreamBg = Color(0xFFF5F0E6)
    val GreenBg = Color(0xFFE8F5E9)
    val GrayBg = Color(0xFFE0E0E0)
    val BlackBg = Color(0xFF000000)

    // Text colors
    val WhiteText = Color(0xFF212121)
    val CreamText = Color(0xFF3E2723)
    val GreenText = Color(0xFF1B5E20)
    val GrayText = Color(0xFF424242)
    val BlackText = Color(0xFFE0E0E0)

    // Highlight colors
    val HighlightYellow = Color(0x80FFEB3B)
    val HighlightGreen = Color(0x804CAF50)
    val HighlightBlue = Color(0x802196F3)
    val HighlightPink = Color(0x80E91E63)

    fun getBackgroundColor(theme: ReaderTheme): Color {
        return when (theme) {
            ReaderTheme.WHITE -> WhiteBg
            ReaderTheme.CREAM -> CreamBg
            ReaderTheme.GREEN -> GreenBg
            ReaderTheme.GRAY -> GrayBg
            ReaderTheme.BLACK -> BlackBg
            ReaderTheme.CUSTOM -> WhiteBg // Will be replaced with custom color
        }
    }

    fun getTextColor(theme: ReaderTheme): Color {
        return when (theme) {
            ReaderTheme.WHITE -> WhiteText
            ReaderTheme.CREAM -> CreamText
            ReaderTheme.GREEN -> GreenText
            ReaderTheme.GRAY -> GrayText
            ReaderTheme.BLACK -> BlackText
            ReaderTheme.CUSTOM -> WhiteText // Will be replaced with custom color
        }
    }
}

enum class ReaderTheme(val displayName: String) {
    WHITE("白色"),
    CREAM("米黄"),
    GREEN("护眼绿"),
    GRAY("灰色"),
    BLACK("纯黑"),
    CUSTOM("自定义")
}
