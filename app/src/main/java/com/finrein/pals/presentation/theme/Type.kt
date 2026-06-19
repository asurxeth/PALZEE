package com.finrein.pals.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.finrein.pals.R

val RobotoFontFamily = FontFamily(
    Font(R.font.roboto_medium_numbers, FontWeight.Medium)
)

val OwnglyphFontFamily = FontFamily(
    Font(R.font.ownglyph, FontWeight.Normal)
)

val DelaGothicOneFontFamily = FontFamily(
    Font(R.font.dela_gothic_one_regular, FontWeight.Normal)
)

val UnpackFontFamily = FontFamily(
    Font(R.font.unpack, FontWeight.Normal)
)

val GoogleSansFontFamily = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal)
)

val BricolageVariableFontFamily = FontFamily(
    Font(R.font.bricolage_grotesque_variable, FontWeight.Normal),
    Font(R.font.bricolage_grotesque_variable, FontWeight.Medium),
    Font(R.font.bricolage_grotesque_variable, FontWeight.Bold),
    Font(R.font.bricolage_grotesque_variable, FontWeight.ExtraBold)
)

val JetBrainsMonoFontFamily = FontFamily.Monospace

// Set of Material 3 typography styles to use
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = BricolageVariableFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = (-1.8).sp
    ),
    displayMedium = TextStyle(
        fontFamily = BricolageVariableFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.0).sp
    ),
    titleLarge = TextStyle(
        fontFamily = OwnglyphFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

