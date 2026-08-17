package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserRole
import com.example.service.PowerButtonService
import com.example.ui.screens.ChildPairingScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DemoModeScreen
import com.example.ui.screens.EmergencyNumbersScreen
import com.example.ui.screens.GoogleSignInScreen
import com.example.ui.screens.HardwareDiagnosticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MockWhatsAppScreen
import com.example.ui.screens.ParentPairingScreen
import com.example.ui.screens.ParentTrackingScreen
import com.example.ui.screens.PoliceDashboardScreen
import com.example.ui.screens.RoleSelectionAndPairingScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SafePlacesScreen
import com.example.ui.screens.SosActiveScreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NagpurSurakshaTheme
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

enum class AppScreen {
    GOOGLE_SIGN_IN,
    ROLE_SELECTION,
    PARENT_PAIRING,
    CHILD_PAIRING,
    HOME,
    SOS_ACTIVE,
    POLICE_DASHBOARD,
    PARENT_TRACKING,
    WHATSAPP_INBOX,
    CONTACTS,
    SAFE_PLACES,
    EMERGENCY_NUMBERS,
    DEMO_MODE,
    DIAGNOSTICS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as NagpurSurakshaApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            NagpurSurakshaTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == PowerButtonService.ACTION_TRIGGER_SOS_HARDWARE) {
            viewModel.triggerSosCountdown("HARDWARE_3X_POWER")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val activeSos by viewModel.activeSos.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val familyPairing by viewModel.familyPairing.collectAsState()

    var currentScreen by remember {
        mutableStateOf(
            if (!googleUser.isSignedIn) AppScreen.GOOGLE_SIGN_IN
            else if (googleUser.role == UserRole.UNASSIGNED) AppScreen.ROLE_SELECTION
            else if (googleUser.role == UserRole.PARENT) AppScreen.PARENT_PAIRING
            else AppScreen.HOME
        )
    }

    var parentTrackingToken by remember { mutableStateOf("ns-token-demo") }
    var showAccountDialog by remember { mutableStateOf(false) }

    // Intercept back presses when not on root screens to prevent ImeBackDispatcher desync
    val isRootScreen = currentScreen == AppScreen.HOME || 
        currentScreen == AppScreen.PARENT_PAIRING || 
        currentScreen == AppScreen.GOOGLE_SIGN_IN
    BackHandler(enabled = !isRootScreen) {
        if (showAccountDialog) {
            showAccountDialog = false
        } else {
            currentScreen = if (activeSos.isTriggered) {
                AppScreen.SOS_ACTIVE
            } else if (googleUser.role == UserRole.PARENT) {
                AppScreen.PARENT_PAIRING
            } else {
                AppScreen.HOME
            }
        }
    }

    // Auto-navigate to SOS_ACTIVE screen if triggered
    LaunchedEffect(activeSos.isTriggered) {
        if (activeSos.isTriggered && (currentScreen == AppScreen.HOME || currentScreen == AppScreen.CHILD_PAIRING)) {
            currentScreen = AppScreen.SOS_ACTIVE
        } else if (!activeSos.isTriggered && currentScreen == AppScreen.SOS_ACTIVE) {
            currentScreen = if (googleUser.role == UserRole.PARENT) AppScreen.PARENT_PAIRING else AppScreen.HOME
        }
    }

    // Google Sign-in Full-Screen View
    if (!googleUser.isSignedIn || currentScreen == AppScreen.GOOGLE_SIGN_IN) {
        LoginScreen(
            viewModel = viewModel,
            onSignInSuccess = {
                currentScreen = AppScreen.ROLE_SELECTION
            }
        )
        return
    }

    // Role Selection Full-Screen View (Parent vs Child)
    if (googleUser.role == UserRole.UNASSIGNED || currentScreen == AppScreen.ROLE_SELECTION) {
        RoleSelectionScreen(
            viewModel = viewModel,
            onRoleSelected = { selectedRole ->
                currentScreen = if (selectedRole == UserRole.PARENT) {
                    AppScreen.PARENT_PAIRING
                } else {
                    AppScreen.CHILD_PAIRING
                }
            },
            onSignOut = {
                currentScreen = AppScreen.GOOGLE_SIGN_IN
            }
        )
        return
    }

    // Account & Family Profile Dialog
    if (showAccountDialog) {
        Dialog(onDismissRequest = { showAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = DarkCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("dialog_account_profile")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (googleUser.role == UserRole.PARENT) NeonLime else SecondaryLime,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = googleUser.displayName.take(2).uppercase().ifBlank { "GD" },
                                color = PureBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = googleUser.displayName.ifBlank { "Google User" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NeonLime.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = "Google",
                                        fontSize = 9.sp,
                                        color = NeonLime,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = googleUser.email.ifBlank { "user@gmail.com" },
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Surface(
                                color = Color(0xFF222222),
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = if (googleUser.role == UserRole.PARENT) "🛡️ PARENT (Guardian)" else "📱 CHILD (Ward)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (googleUser.role == UserRole.PARENT) NeonLime else SecondaryLime,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkCardBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "FAMILY PAIRING MANAGEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (googleUser.role == UserRole.PARENT) {
                        Surface(
                            color = Color(0xFF222222),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Active Pairing Code", fontSize = 11.sp, color = TextMuted)
                                    Text(familyPairing.activePairingCode, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonLime)
                                }
                                Button(
                                    onClick = {
                                        showAccountDialog = false
                                        currentScreen = AppScreen.PARENT_PAIRING
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Manage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF222222),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (familyPairing.isChildPairedWithParent) "Paired Guardian" else "Pairing Status",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = if (familyPairing.isChildPairedWithParent) familyPairing.pairedParentName ?: "Guardian" else "Not Linked",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (familyPairing.isChildPairedWithParent) NeonLime else EmergencyRed
                                    )
                                }
                                Button(
                                    onClick = {
                                        showAccountDialog = false
                                        currentScreen = AppScreen.CHILD_PAIRING
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(if (familyPairing.isChildPairedWithParent) "View Link" else "Enter Code", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Role Switcher Button
                    OutlinedButton(
                        onClick = {
                            showAccountDialog = false
                            currentScreen = AppScreen.ROLE_SELECTION
                        },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_switch_role_dialog")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Role (Parent ↔ Child)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sign Out Button
                    Button(
                        onClick = {
                            viewModel.signOutGoogle()
                            showAccountDialog = false
                            currentScreen = AppScreen.GOOGLE_SIGN_IN
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626)),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_signout_google")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out of Google", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }

Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = when (currentScreen) {
                            AppScreen.HOME -> "NAGPUR SURAKSHA"
                            AppScreen.SOS_ACTIVE -> "🚨 SOS ACTIVE"
                            AppScreen.PARENT_PAIRING -> "PARENT COMMAND HUB"
                            AppScreen.CHILD_PAIRING -> "CHILD PAIRING LINK"
                            AppScreen.POLICE_DASHBOARD -> "POLICE DASHBOARD"
                            AppScreen.PARENT_TRACKING -> "PARENT LIVE TRACKING"
                            AppScreen.WHATSAPP_INBOX -> "MOCK WHATSAPP"
                            AppScreen.CONTACTS -> "EMERGENCY CONTACTS"
                            AppScreen.SAFE_PLACES -> "SAFE PLACES"
                            AppScreen.EMERGENCY_NUMBERS -> "EMERGENCY NUMBERS"
                            AppScreen.DEMO_MODE -> "HACKATHON DEMO"
                            AppScreen.DIAGNOSTICS -> "HARDWARE DIAGNOSTICS"
                            else -> "NAGPUR SURAKSHA"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.4).sp,
                        color = Color.White
                    )
                    if (activeSos.isTriggered) {
                        Text(
                            text = "🔴 Emergency Active (#${activeSos.incidentCode ?: "LIVE"})",
                            fontSize = 11.sp,
                            color = EmergencyRed
                        )
                    } else {
                        Text(
                            text = if (googleUser.role == UserRole.PARENT) "Mode: Parent Guardian 🛡️" else "Mode: Child Protected 📱",
                            fontSize = 10.sp,
                            color = NeonLime
                        )
                    }
                }
            },
            navigationIcon = {
                if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.PARENT_PAIRING && currentScreen != AppScreen.SOS_ACTIVE) {
                    IconButton(onClick = {
                        currentScreen = if (activeSos.isTriggered) {
                            AppScreen.SOS_ACTIVE
                        } else if (googleUser.role == UserRole.PARENT) {
                            AppScreen.PARENT_PAIRING
                        } else {
                            AppScreen.HOME
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            },
            actions = {
                // Profile & Role Chip (Opens modal to switch roles or pair)
                Surface(
                    color = Color(0xFF262626),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .clickable { showAccountDialog = true }
                        .padding(end = 6.dp)
                        .testTag("btn_top_profile")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (googleUser.role == UserRole.PARENT) NeonLime else SecondaryLime,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = googleUser.displayName.take(1).uppercase().ifBlank { "G" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (googleUser.role == UserRole.PARENT) "Parent" else "Child",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Quick Demo Icon
                IconButton(
                    onClick = { currentScreen = AppScreen.DEMO_MODE },
                    modifier = Modifier.testTag("btn_top_demo")
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Demo",
                        tint = NeonLime
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (activeSos.isTriggered) EmergencyRed else DarkBackground
            )
        )
    },
    bottomBar = {
        NavigationBar(
            containerColor = DarkBackground,
            tonalElevation = 8.dp
        ) {
            // Main Citizen SOS or Parent Hub
            NavigationBarItem(
                selected = (googleUser.role == UserRole.PARENT && currentScreen == AppScreen.PARENT_PAIRING) ||
                        (googleUser.role == UserRole.CHILD && (currentScreen == AppScreen.HOME || currentScreen == AppScreen.SOS_ACTIVE)),
                onClick = {
                    currentScreen = if (activeSos.isTriggered) {
                        AppScreen.SOS_ACTIVE
                    } else if (googleUser.role == UserRole.PARENT) {
                        AppScreen.PARENT_PAIRING
                    } else {
                        AppScreen.HOME
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (activeSos.isTriggered) Icons.Default.Emergency
                        else if (googleUser.role == UserRole.PARENT) Icons.Default.FamilyRestroom
                        else Icons.Default.Home,
                        contentDescription = "Main Hub"
                    )
                },
                label = {
                    Text(
                        text = if (activeSos.isTriggered) "SOS Active"
                        else if (googleUser.role == UserRole.PARENT) "Guardian"
                        else "Citizen SOS",
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureBlack,
                    selectedTextColor = NeonLime,
                    indicatorColor = if (activeSos.isTriggered) EmergencyRed else NeonLime,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )

            // Family Link / Pair Hub (Parent Code Generator or Child Code Input)
            NavigationBarItem(
                selected = currentScreen == AppScreen.PARENT_PAIRING || currentScreen == AppScreen.CHILD_PAIRING,
                onClick = {
                    currentScreen = if (googleUser.role == UserRole.PARENT) AppScreen.PARENT_PAIRING else AppScreen.CHILD_PAIRING
                },
                icon = {
                    Icon(
                        imageVector = if (googleUser.role == UserRole.PARENT) Icons.Default.Key else Icons.Default.Link,
                        contentDescription = "Pair"
                    )
                },
                label = {
                    Text(
                        text = if (googleUser.role == UserRole.PARENT) "Pair Code" else "Pair Link",
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureBlack,
                    selectedTextColor = NeonLime,
                    indicatorColor = NeonLime,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )

            // Police Dashboard
            NavigationBarItem(
                selected = currentScreen == AppScreen.POLICE_DASHBOARD,
                onClick = { currentScreen = AppScreen.POLICE_DASHBOARD },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalPolice,
                        contentDescription = "Police"
                    )
                },
                label = { Text("Police", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureBlack,
                    selectedTextColor = NeonLime,
                    indicatorColor = NeonLime,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )

            // WhatsApp Inbox
            NavigationBarItem(
                selected = currentScreen == AppScreen.WHATSAPP_INBOX,
                onClick = { currentScreen = AppScreen.WHATSAPP_INBOX },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp"
                    )
                },
                label = { Text("WhatsApp", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureBlack,
                    selectedTextColor = NeonLime,
                    indicatorColor = NeonLime,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )

            // Demo
            NavigationBarItem(
                selected = currentScreen == AppScreen.DEMO_MODE,
                onClick = { currentScreen = AppScreen.DEMO_MODE },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Demo"
                    )
                },
                label = { Text("Demo", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PureBlack,
                    selectedTextColor = NeonLime,
                    indicatorColor = NeonLime,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.GOOGLE_SIGN_IN -> LoginScreen(
                    viewModel = viewModel,
                    onSignInSuccess = { currentScreen = AppScreen.ROLE_SELECTION }
                )

                AppScreen.ROLE_SELECTION -> RoleSelectionAndPairingScreen(
                    viewModel = viewModel,
                    onNavigateToHome = { currentScreen = AppScreen.HOME },
                    onNavigateToParentTracking = { token ->
                        parentTrackingToken = token
                        currentScreen = AppScreen.PARENT_TRACKING
                    },
                    onSignOut = { currentScreen = AppScreen.GOOGLE_SIGN_IN }
                )

                AppScreen.PARENT_PAIRING -> ParentPairingScreen(
                    viewModel = viewModel,
                    onNavigateToLiveTracking = { token ->
                        parentTrackingToken = token
                        currentScreen = AppScreen.PARENT_TRACKING
                    },
                    onSwitchRole = { currentScreen = AppScreen.ROLE_SELECTION }
                )

                AppScreen.CHILD_PAIRING -> ChildPairingScreen(
                    viewModel = viewModel,
                    onPairingComplete = { currentScreen = AppScreen.HOME },
                    onSwitchRole = { currentScreen = AppScreen.ROLE_SELECTION }
                )

                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToContacts = { currentScreen = AppScreen.CONTACTS },
                    onNavigateToSafePlaces = { currentScreen = AppScreen.SAFE_PLACES },
                    onNavigateToEmergencyNumbers = { currentScreen = AppScreen.EMERGENCY_NUMBERS },
                    onNavigateToDiagnostics = { currentScreen = AppScreen.DIAGNOSTICS },
                    onNavigateToPairing = { currentScreen = AppScreen.CHILD_PAIRING }
                )

                AppScreen.SOS_ACTIVE -> SosActiveScreen(
                    viewModel = viewModel,
                    onNavigateToParentTracking = { token ->
                        parentTrackingToken = token
                        currentScreen = AppScreen.PARENT_TRACKING
                    }
                )

                AppScreen.POLICE_DASHBOARD -> PoliceDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToParentTracking = { token ->
                        parentTrackingToken = token
                        currentScreen = AppScreen.PARENT_TRACKING
                    }
                )

                AppScreen.PARENT_TRACKING -> ParentTrackingScreen(
                    token = parentTrackingToken,
                    viewModel = viewModel,
                    onBack = {
                        currentScreen = if (activeSos.isTriggered) {
                            AppScreen.SOS_ACTIVE
                        } else if (googleUser.role == UserRole.PARENT) {
                            AppScreen.PARENT_PAIRING
                        } else {
                            AppScreen.HOME
                        }
                    }
                )

                AppScreen.WHATSAPP_INBOX -> MockWhatsAppScreen(
                    viewModel = viewModel,
                    onOpenLiveTracking = { token ->
                        parentTrackingToken = token
                        currentScreen = AppScreen.PARENT_TRACKING
                    }
                )

                AppScreen.CONTACTS -> ContactsScreen(viewModel = viewModel)

                AppScreen.SAFE_PLACES -> SafePlacesScreen(viewModel = viewModel)

                AppScreen.EMERGENCY_NUMBERS -> EmergencyNumbersScreen()

                AppScreen.DEMO_MODE -> DemoModeScreen(
                    viewModel = viewModel,
                    onNavigateToCitizenHome = { currentScreen = AppScreen.HOME },
                    onNavigateToPolice = { currentScreen = AppScreen.POLICE_DASHBOARD },
                    onNavigateToWhatsApp = { currentScreen = AppScreen.WHATSAPP_INBOX },
                    onNavigateToParentTracking = { token ->
                        parentTrackingToken = token
                        currentScreen = AppScreen.PARENT_TRACKING
                    },
                    onNavigateToParentPairing = { currentScreen = AppScreen.PARENT_PAIRING },
                    onNavigateToChildPairing = { currentScreen = AppScreen.CHILD_PAIRING },
                    onNavigateToRoleSelection = { currentScreen = AppScreen.ROLE_SELECTION }
                )

                AppScreen.DIAGNOSTICS -> HardwareDiagnosticsScreen(viewModel = viewModel)
            }
        }
    }
}
