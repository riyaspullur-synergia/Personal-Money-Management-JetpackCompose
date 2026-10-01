package app.riyaspullur.personalmoneymanagement.ui.theme

import androidx.compose.ui.graphics.Color

// ---- M3 role palette, hand-derived from brand seed #00875A (emerald) ----

// Light
val PrimaryLight = Color(0xFF00875A)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFA6F2CB)
val OnPrimaryContainerLight = Color(0xFF002114)

val SecondaryLight = Color(0xFF4C6358)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCEE9DA)
val OnSecondaryContainerLight = Color(0xFF092017)

val TertiaryLight = Color(0xFF3D6373)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFC0E8FB)
val OnTertiaryContainerLight = Color(0xFF001F29)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF6FBF3)
val OnBackgroundLight = Color(0xFF171D19)
val SurfaceLight = Color(0xFFF6FBF3)
val OnSurfaceLight = Color(0xFF171D19)
val SurfaceVariantLight = Color(0xFFDCE5DD)
val OnSurfaceVariantLight = Color(0xFF414942)
val OutlineLight = Color(0xFF717971)
val OutlineVariantLight = Color(0xFFC1C9C0)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF0F5ED)
val SurfaceContainerLight = Color(0xFFEAF0E7)
val SurfaceContainerHighLight = Color(0xFFE4EAE1)
val SurfaceContainerHighestLight = Color(0xFFDEE4DC)

val InverseSurfaceLight = Color(0xFF2B322D)
val InverseOnSurfaceLight = Color(0xFFECF2E8)
val InversePrimaryLight = Color(0xFF8DDBB4)
val ScrimLight = Color(0xFF000000)

// Dark
val PrimaryDark = Color(0xFF6CDBAA)
val OnPrimaryDark = Color(0xFF00391F)
val PrimaryContainerDark = Color(0xFF00522F)
val OnPrimaryContainerDark = Color(0xFF8DF9CA)

val SecondaryDark = Color(0xFFB3CCBE)
val OnSecondaryDark = Color(0xFF1E352A)
val SecondaryContainerDark = Color(0xFF354B40)
val OnSecondaryContainerDark = Color(0xFFCEE9DA)

val TertiaryDark = Color(0xFFA4CCDE)
val OnTertiaryDark = Color(0xFF08333F)
val TertiaryContainerDark = Color(0xFF234B5A)
val OnTertiaryContainerDark = Color(0xFFC0E8FB)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF0F1512)
val OnBackgroundDark = Color(0xFFDEE4DC)
val SurfaceDark = Color(0xFF0F1512)
val OnSurfaceDark = Color(0xFFDEE4DC)
val SurfaceVariantDark = Color(0xFF414942)
val OnSurfaceVariantDark = Color(0xFFC1C9C0)
val OutlineDark = Color(0xFF8B938A)
val OutlineVariantDark = Color(0xFF414942)

val SurfaceContainerLowestDark = Color(0xFF0A0F0C)
val SurfaceContainerLowDark = Color(0xFF171D19)
val SurfaceContainerDark = Color(0xFF1B211D)
val SurfaceContainerHighDark = Color(0xFF252B27)
val SurfaceContainerHighestDark = Color(0xFF303632)

val InverseSurfaceDark = Color(0xFFDEE4DC)
val InverseOnSurfaceDark = Color(0xFF2B322D)
val InversePrimaryDark = Color(0xFF00875A)
val ScrimDark = Color(0xFF000000)

// ---- Semantic finance colors (separate from M3 roles) ----

val IncomeLight = Color(0xFF1E8E5A)
val ExpenseLight = Color(0xFFC4432E)
val TransferLight = TertiaryLight
val SavingsGoalLight = Color(0xFFC68A00)

val IncomeDark = Color(0xFF7FDB9E)
val ExpenseDark = Color(0xFFFFB59C)
val TransferDark = TertiaryDark
val SavingsGoalDark = Color(0xFFE4C36B)

// Extra accent hues used only to round out the categorical chart palette
val ChartVioletLight = Color(0xFF6B5CA5)
val ChartTealLight = Color(0xFF2E9E9E)
val ChartVioletDark = Color(0xFFB8A9E0)
val ChartTealDark = Color(0xFF7FD9D9)

val ChartPaletteLight = listOf(
    PrimaryLight,
    TertiaryLight,
    SavingsGoalLight,
    ExpenseLight,
    SecondaryLight,
    ChartVioletLight,
    ChartTealLight,
    OutlineLight
)

val ChartPaletteDark = listOf(
    PrimaryDark,
    TertiaryDark,
    SavingsGoalDark,
    ExpenseDark,
    SecondaryDark,
    ChartVioletDark,
    ChartTealDark,
    OutlineDark
)
