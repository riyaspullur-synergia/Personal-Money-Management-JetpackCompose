package app.riyaspullur.personalmoneymanagement.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.riyaspullur.personalmoneymanagement.core.security.BiometricAuthenticator
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.util.AppLanguage
import app.riyaspullur.personalmoneymanagement.core.util.AppLocaleManager
import app.riyaspullur.personalmoneymanagement.feature.accounts.ui.AccountsScreen
import app.riyaspullur.personalmoneymanagement.feature.accounts.ui.AccountsUiState
import app.riyaspullur.personalmoneymanagement.feature.accounts.ui.AccountsViewModel
import app.riyaspullur.personalmoneymanagement.feature.accounts.ui.AddAccountScreen
import app.riyaspullur.personalmoneymanagement.feature.assistant.AiAssistantChatScreen
import app.riyaspullur.personalmoneymanagement.feature.assistant.AiAssistantFab
import app.riyaspullur.personalmoneymanagement.feature.assistant.AiAssistantViewModel
import app.riyaspullur.personalmoneymanagement.feature.auth.ui.AuthUiState
import app.riyaspullur.personalmoneymanagement.feature.auth.ui.AuthViewModel
import app.riyaspullur.personalmoneymanagement.feature.auth.ui.LockScreen
import app.riyaspullur.personalmoneymanagement.feature.auth.ui.LoginScreen
import app.riyaspullur.personalmoneymanagement.feature.auth.ui.RegisterScreen
import app.riyaspullur.personalmoneymanagement.feature.dashboard.ui.DashboardScreen
import app.riyaspullur.personalmoneymanagement.feature.dashboard.ui.DashboardUiState
import app.riyaspullur.personalmoneymanagement.feature.dashboard.ui.DashboardViewModel
import app.riyaspullur.personalmoneymanagement.feature.reports.ui.ReportsScreen
import app.riyaspullur.personalmoneymanagement.feature.reports.ui.ReportsViewModel
import app.riyaspullur.personalmoneymanagement.feature.settings.ui.RecycleBinScreen
import app.riyaspullur.personalmoneymanagement.feature.settings.ui.RecycleBinViewModel
import app.riyaspullur.personalmoneymanagement.feature.settings.ui.SettingsScreen
import app.riyaspullur.personalmoneymanagement.feature.settings.ui.SettingsViewModel
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.AddTransactionScreen
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.AddTransactionViewModel
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.TransactionsScreen
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.TransactionsViewModel
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.TransferScreen
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.TransferViewModel
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

@Composable
fun AppNavigation() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as FragmentActivity
    val biometricAuthenticator = remember { BiometricAuthenticator(context) }
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val isAppLockEnabled by settingsViewModel.isAppLockEnabled.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val lifecycleOwner = LocalLifecycleOwner.current

    val localizedContext = remember(language) { AppLocaleManager.localizedContext(context, language) }
    val layoutDirection = if (AppLanguage.fromCode(language).isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection
    ) {
    PersonalMoneyManagemntTheme(darkTheme = darkTheme) {
        // App Lock Logic
        androidx.compose.runtime.DisposableEffect(lifecycleOwner, isAppLockEnabled) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    if (isAppLockEnabled && authState is AuthUiState.Authenticated) {
                        navController.navigate(Screen.Lock)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val showBottomBar = currentDestination?.hierarchy?.any { 
            it.hasRoute<Screen.Dashboard>() || it.hasRoute<Screen.Reports>() || 
            it.hasRoute<Screen.Accounts>() || it.hasRoute<Screen.Settings>() || 
            it.hasRoute<Screen.Transactions>()
        } == true

        var showAssistant by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(navController, currentDestination)
                }
            },
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                composable<Screen.Splash> {
                    app.riyaspullur.personalmoneymanagement.core.ui.components.SplashScreen(
                        onSplashFinished = {
                            when (authState) {
                                is AuthUiState.Authenticated -> {
                                    navController.navigate(Screen.Dashboard) {
                                        popUpTo(Screen.Splash) { inclusive = true }
                                    }
                                }
                                is AuthUiState.RegistrationRequired -> {
                                    navController.navigate(Screen.Register) {
                                        popUpTo(Screen.Splash) { inclusive = true }
                                    }
                                }
                                else -> {
                                    navController.navigate(Screen.Login) {
                                        popUpTo(Screen.Splash) { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }
                composable<Screen.Login> {
                    val users by authViewModel.allUsers.collectAsState()
                    LoginScreen(
                        users = users,
                        onLoginClick = { u, p -> authViewModel.login(u, p) },
                        onRegisterClick = { navController.navigate(Screen.Register) }
                    )
                }
                composable<Screen.Register> {
                    RegisterScreen(
                        onRegisterClick = { u, p, d -> authViewModel.register(u, p, d) },
                        onLoginClick = { navController.navigate(Screen.Login) }
                    )
                }
                composable<Screen.Dashboard> {
                    val dashboardViewModel: DashboardViewModel = hiltViewModel()
                    val dashboardState by dashboardViewModel.uiState.collectAsState()
                    val successState = dashboardState as? DashboardUiState.Success
                    val recentTransactions = successState?.recentTransactions ?: emptyList()
                    val maxExpenseLimit by dashboardViewModel.maxExpenseLimit.collectAsState(initial = 0L)
                    val expenseLimitStartDay by dashboardViewModel.expenseLimitStartDay.collectAsState(initial = "1")

                    DashboardScreen(
                        uiState = dashboardState,
                        recentTransactions = recentTransactions,
                        event = dashboardViewModel.event,
                        maxExpenseLimit = maxExpenseLimit,
                        expenseLimitStartDay = expenseLimitStartDay,
                        onSetExpenseLimitClick = { limit, day -> dashboardViewModel.setExpenseLimit(limit, day) },
                        onAddExpenseClick = { navController.navigate(Screen.AddTransaction) },
                        onAddIncomeClick = { navController.navigate(Screen.AddTransaction) },
                        onTransferClick = { navController.navigate(Screen.Transfer) },
                        onViewAllClick = { navController.navigate(Screen.Transactions) },
                        onImportClick = { dashboardViewModel.importTransactions(it) }
                    )
                }
                composable<Screen.Transfer> {
                    val transferViewModel: TransferViewModel = hiltViewModel()
                    val accounts by transferViewModel.accounts.collectAsState()
                    TransferScreen(
                        accounts = accounts,
                        onTransferClick = { f, t, a, c, n -> transferViewModel.transfer(f, t, a, c, n) },
                        onBackClick = { navController.popBackStack() }
                    )
                    LaunchedEffect(Unit) {
                        transferViewModel.success.collect {
                            navController.popBackStack()
                        }
                    }
                }
                composable<Screen.Transactions> {
                    val transactionsViewModel: TransactionsViewModel = hiltViewModel()
                    val transactionsState by transactionsViewModel.uiState.collectAsState()
                    
                    TransactionsScreen(
                        uiState = transactionsState,
                        onBackClick = { navController.popBackStack() },
                        onDeleteClick = { transactionsViewModel.deleteTransaction(it) },
                        onDateRangeSelect = { start, end -> transactionsViewModel.setDateRange(start, end) }
                    )
                }
                composable<Screen.Reports> {
                    val reportsViewModel: ReportsViewModel = hiltViewModel()
                    val reportsState by reportsViewModel.uiState.collectAsState()
                    val accounts by reportsViewModel.accounts.collectAsState()
                    
                    ReportsScreen(
                        uiState = reportsState,
                        accounts = accounts,
                        onViewPdf = { reportsViewModel.viewPdf() },
                        onSharePdf = { reportsViewModel.sharePdf() },
                        onExportCsv = { reportsViewModel.exportCsv() },
                        onDownloadTemplate = { reportsViewModel.downloadTemplate() },
                        onSelectAccount = { reportsViewModel.selectAccount(it) },
                        onDateRangeSelect = { start, end -> reportsViewModel.setDateRange(start, end) }
                    )

                    LaunchedEffect(Unit) {
                        reportsViewModel.event.collect { event ->
                            when (event) {
                                is app.riyaspullur.personalmoneymanagement.feature.reports.ui.ReportsEvent.NavigateToPdfViewer -> {
                                    navController.navigate(Screen.PdfViewer(event.filePath))
                                }
                            }
                        }
                    }
                }
                composable<Screen.PdfViewer> { backStackEntry ->
                    val pdfViewer: Screen.PdfViewer = backStackEntry.toRoute()
                    val reportsViewModel: ReportsViewModel = hiltViewModel()
                    
                    app.riyaspullur.personalmoneymanagement.feature.reports.ui.PdfViewerScreen(
                        filePath = pdfViewer.filePath,
                        onBackClick = { navController.popBackStack() },
                        onShareClick = { reportsViewModel.shareFile(pdfViewer.filePath) }
                    )
                }
                composable<Screen.Accounts> {
                    val accountsViewModel: AccountsViewModel = hiltViewModel()
                    val accountsState by accountsViewModel.uiState.collectAsState()
                    AccountsScreen(
                        uiState = accountsState,
                        onAddAccountClick = { navController.navigate(Screen.AddAccount) },
                        onAddGroupClick = { name -> accountsViewModel.addGroup(name) }
                    )
                }
                composable<Screen.Settings> {
                    val backupViewModel: app.riyaspullur.personalmoneymanagement.feature.settings.ui.BackupViewModel = hiltViewModel()
                    var showBackup by rememberSaveable { mutableStateOf(false) }
                    if (showBackup) {
                        app.riyaspullur.personalmoneymanagement.feature.settings.ui.BackupDialog(backupViewModel) { showBackup = false }
                    }
                    SettingsScreen(
                        isAppLockEnabled = isAppLockEnabled,
                        themeMode = themeMode,
                        language = language,
                        onAppLockChange = { settingsViewModel.setAppLockEnabled(it) },
                        onThemeChange = { settingsViewModel.setThemeMode(it) },
                        onLanguageChange = { code ->
                            settingsViewModel.setLanguage(code)
                            AppLocaleManager.applyToSystem(activity, code)
                        },
                        onBackupRestoreClick = { showBackup = true },
                        onRecycleBinClick = { navController.navigate(Screen.RecycleBin) },
                        onProfileClick = { navController.navigate(Screen.Profile) },
                        onHelpSupportClick = { navController.navigate(Screen.HelpSupport) },
                        onLogoutClick = { 
                            authViewModel.logout()
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Screen.Profile> {
                    val profileViewModel: app.riyaspullur.personalmoneymanagement.feature.settings.ui.ProfileViewModel = hiltViewModel()
                    val profileState by profileViewModel.uiState.collectAsState()
                    
                    app.riyaspullur.personalmoneymanagement.feature.settings.ui.ProfileScreen(
                        uiState = profileState,
                        onUpdateClick = { profileViewModel.updateProfile(it) },
                        onChangePasswordClick = { old, new ->
                            profileViewModel.verifyAndChangePassword(old, new)
                        },
                        onBackClick = { navController.popBackStack() }
                    )

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val passwordChangedMessage = stringResource(R.string.profile_password_changed_successfully)
                    val profileErrorMessages = mapOf(
                        "User not found" to stringResource(R.string.profile_error_user_not_found),
                        "Incorrect current password" to stringResource(R.string.profile_error_incorrect_current_password),
                        "Failed to update profile" to stringResource(R.string.profile_error_failed_to_update),
                        "Failed to change password" to stringResource(R.string.profile_error_failed_to_change_password)
                    )
                    LaunchedEffect(Unit) {
                        profileViewModel.event.collect { event ->
                            when (event) {
                                is app.riyaspullur.personalmoneymanagement.feature.settings.ui.ProfileEvent.PasswordChanged -> {
                                    android.widget.Toast.makeText(context, passwordChangedMessage, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                is app.riyaspullur.personalmoneymanagement.feature.settings.ui.ProfileEvent.Error -> {
                                    val message = profileErrorMessages[event.message] ?: event.message
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                composable<Screen.HelpSupport> {
                    app.riyaspullur.personalmoneymanagement.feature.settings.ui.HelpSupportScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable<Screen.RecycleBin> {
                    val recycleBinViewModel: RecycleBinViewModel = hiltViewModel()
                    val deletedTransactions by recycleBinViewModel.deletedTransactions.collectAsState()
                    RecycleBinScreen(
                        deletedTransactions = deletedTransactions,
                        onRestoreClick = { recycleBinViewModel.restoreTransaction(it) },
                        onClearAllClick = { recycleBinViewModel.clearAll() },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable<Screen.Lock> {
                    val biometricPromptTitle = stringResource(R.string.lock_title)
                    val biometricPromptSubtitle = stringResource(R.string.lock_biometric_prompt_subtitle)
                    LockScreen(
                        onUnlockClick = { _ ->
                            navController.popBackStack()
                        },
                        onBiometricClick = {
                            biometricAuthenticator.authenticate(
                                activity = activity,
                                title = biometricPromptTitle,
                                subtitle = biometricPromptSubtitle,
                                onSuccess = { navController.popBackStack() },
                                onError = { /* Show error */ }
                            )
                        },
                        canUseBiometrics = biometricAuthenticator.canAuthenticate()
                    )
                }
                composable<Screen.AddAccount> {
                    val accountsViewModel: AccountsViewModel = hiltViewModel()
                    val accountsState by accountsViewModel.uiState.collectAsState()
                    val groups = (accountsState as? AccountsUiState.Success)?.groups ?: emptyList()
                    val userId = (authState as? AuthUiState.Authenticated)?.userId ?: 0L

                    AddAccountScreen(
                        groups = groups,
                        userId = userId,
                        onSaveClick = { account -> 
                            accountsViewModel.addAccount(account)
                            navController.popBackStack()
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable<Screen.AddTransaction> {
                    val addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
                    val addTransactionState by addTransactionViewModel.uiState.collectAsState()
                    val accounts by addTransactionViewModel.accounts.collectAsState()
                    val categories by addTransactionViewModel.categories.collectAsState()

                    AddTransactionScreen(
                        accounts = accounts,
                        categories = categories,
                        uiState = addTransactionState,
                        onAddClick = { amt, type, catId, accId, toAccId, desc, status, receipt, date -> 
                            addTransactionViewModel.addTransaction(amt, type, catId, accId, toAccId, desc, status, receipt, date) 
                        },
                        onAddCategoryClick = { name, type -> 
                            addTransactionViewModel.addCategory(name, type)
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                    
                    LaunchedEffect(Unit) {
                        addTransactionViewModel.event.collect { event ->
                            if (event is app.riyaspullur.personalmoneymanagement.feature.transactions.ui.AddTransactionEvent.Success) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }

            if (showBottomBar) {
                AiAssistantFab(
                    onClick = { showAssistant = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = innerPadding.calculateBottomPadding() + 16.dp, start = 16.dp)
                )
            }
        }
    }

        if (showAssistant) {
            val assistantViewModel: AiAssistantViewModel = hiltViewModel()
            val assistantState by assistantViewModel.uiState.collectAsState()
            
            AiAssistantChatScreen(
                state = assistantState,
                onSendMessage = { assistantViewModel.sendMessage(it) },
                onInitialize = { assistantViewModel.initializeWithKey(it) },
                onDismiss = { showAssistant = false }
            )
        }

        // Handle authentication state changes
        LaunchedEffect(authState) {
            if (currentDestination?.hasRoute<Screen.Splash>() == true) return@LaunchedEffect
            
            when (authState) {
                is AuthUiState.Authenticated -> {
                    if (currentDestination?.hasRoute<Screen.Login>() == true || currentDestination?.hasRoute<Screen.Register>() == true) {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo(Screen.Login) { inclusive = true }
                        }
                    }
                }
                is AuthUiState.RegistrationRequired -> {
                    navController.navigate(Screen.Register) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }
    }
}

@Composable
private fun AppBottomBar(
    navController: androidx.navigation.NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard)) },
            selected = currentDestination?.hierarchy?.any { 
                it.hasRoute<Screen.Dashboard>() || it.hasRoute<Screen.Transactions>() 
            } == true,
            colors = navItemColors,
            onClick = {
                navController.navigate(Screen.Dashboard) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
            label = { Text(stringResource(R.string.reports)) },
            selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Reports>() } == true,
            colors = navItemColors,
            onClick = {
                navController.navigate(Screen.Reports) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
            label = { Text(stringResource(R.string.accounts)) },
            selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Accounts>() } == true,
            colors = navItemColors,
            onClick = {
                navController.navigate(Screen.Accounts) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) },
            selected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Settings>() } == true,
            colors = navItemColors,
            onClick = {
                navController.navigate(Screen.Settings) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
