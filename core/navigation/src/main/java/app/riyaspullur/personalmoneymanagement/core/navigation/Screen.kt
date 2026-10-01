package app.riyaspullur.personalmoneymanagement.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object AuthGraph : Screen
    
    @Serializable
    data object Login : Screen
    
    @Serializable
    data object Register : Screen
    
    @Serializable
    data object MainGraph : Screen
    
    @Serializable
    data object Dashboard : Screen
    
    @Serializable
    data object Transactions : Screen
    
    @Serializable
    data object Reports : Screen
    
    @Serializable
    data object Accounts : Screen

    @Serializable
    data object AddTransaction : Screen

    @Serializable
    data object AddAccount : Screen

    @Serializable
    data object Lock : Screen

    @Serializable
    data object RecycleBin : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object Transfer : Screen

    @Serializable
    data object HelpSupport : Screen

    @Serializable
    data class PdfViewer(val filePath: String) : Screen
}
