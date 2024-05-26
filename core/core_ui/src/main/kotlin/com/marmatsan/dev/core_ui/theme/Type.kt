package com.marmatsan.dev.core_ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.marmatsan.core_ui.R

// Google Fonts Online Provider
/*val fontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Poppins"),
        fontProvider = GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    )
)*/

val fontFamily = FontFamily(
    Font(R.font.poppins_black, FontWeight.Black, FontStyle.Normal),
    Font(R.font.poppins_blackitalic, FontWeight.Black, FontStyle.Italic),

    Font(R.font.poppins_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.poppins_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),

    Font(R.font.poppins_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.poppins_bolditalic, FontWeight.Bold, FontStyle.Italic),

    Font(R.font.poppins_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.poppins_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),

    Font(R.font.poppins_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.poppins_mediumitalic, FontWeight.Medium, FontStyle.Italic),

    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),

    Font(R.font.poppins_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.poppins_lightitalic, FontWeight.Light, FontStyle.Italic),

    Font(R.font.poppins_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.poppins_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),

    Font(R.font.poppins_thin, FontWeight.Thin, FontStyle.Normal),
    Font(R.font.poppins_thinitalic, FontWeight.Thin, FontStyle.Italic)
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = (-0.25).sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.sp,
        lineHeight = 40.sp
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.sp,
        lineHeight = 32.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = fontFamily,
        letterSpacing = 0.15.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = fontFamily,
        letterSpacing = 0.1.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.5.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.25.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = fontFamily,
        letterSpacing = 0.4.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = fontFamily,
        letterSpacing = 0.1.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = fontFamily,
        letterSpacing = 0.5.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = fontFamily,
        letterSpacing = 0.5.sp,
        lineHeight = 16.sp
    )
)
