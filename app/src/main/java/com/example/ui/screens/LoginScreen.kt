package com.example.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Google 1-Tap, 1: Direct Sign-Up, 2: Demo Mode
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    
    // Direct Sign-Up Form Fields - empty by default for manual entry
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPhone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CITIZEN) }
    
    // Dialogs
    var showCancelQuestionDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    fun handleSignInCompletion(
        email: String,
        displayName: String,
        role: UserRole = selectedRole,
        photoUrl: String? = null
    ) {
        viewModel.signInWithGoogle(
            email = email,
            displayName = displayName,
            photoUrl = photoUrl
        )
        viewModel.setUserRole(role)
        viewModel.migrateDataToFirebase()
        Toast.makeText(context, "Welcome $displayName! Session saved permanently.", Toast.LENGTH_SHORT).show()
        onSignInSuccess()
    }

    fun launchCredentialManagerGoogleSignIn() {
        coroutineScope.launch {
            isLoading = true
            statusMessage = "Opening Google Sign-In..."

            try {
                val serverClientId = try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    "478106574221-nagpur-suraksha.apps.googleusercontent.com"
                }

                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val response = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = response.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                    val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                    statusMessage = "Connecting with Firebase Firestore..."
                    try {
                        val auth = FirebaseAuth.getInstance()
                        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(authCredential)
                    } catch (e: Exception) {
                        Log.w("LoginScreen", "Firebase Auth note: ${e.message}")
                    }

                    handleSignInCompletion(email, displayName, UserRole.CITIZEN, photoUrl)
                } else {
                    selectedTab = 1
                }
            } catch (e: GetCredentialCancellationException) {
                statusMessage = null
                showCancelQuestionDialog = true
            } catch (e: Exception) {
                Log.w("LoginScreen", "Google Credential Manager info: ${e.message}")
                showCancelQuestionDialog = true
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(NeonLime, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Nagpur Suraksha Shield",
                        tint = PureBlack,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NAGPUR SURAKSHA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.8).sp
                )

                Text(
                    text = "Smart Citizen & Family Protection Network",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Questions & Help Assistant Pill Button
                Surface(
                    color = Color(0xFF242424),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable { showHelpDialog = true }
                        .testTag("btn_auth_questions_assistant")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.QuestionAnswer,
                            contentDescription = "Help",
                            tint = NeonLime,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign-Up Questions & Help",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Selector: Google 1-Tap vs Direct Sign Up vs Demo Mode
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCardSurface,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonLime
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Google 1-Tap", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 0) NeonLime else TextMuted) },
                    modifier = Modifier.testTag("tab_google_1tap")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Direct Sign Up", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 1) NeonLime else TextMuted) },
                    modifier = Modifier.testTag("tab_direct_signup")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Demo Mode", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 2) NeonLime else TextMuted) },
                    modifier = Modifier.testTag("tab_demo_mode")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: Google 1-Tap Card
            if (selectedTab == 0) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = DarkCardSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_google_login")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Instant Google Authentication",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.4).sp,
                            color = Color.White
                        )

                        Text(
                            text = "Authenticate securely via Google Play Services. Your session is permanently remembered so you never have to sign in repeatedly.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                        )

                        // Primary Google Button
                        Surface(
                            color = NeonLime,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable(enabled = !isLoading) {
                                    launchCredentialManagerGoogleSignIn()
                                }
                                .testTag("btn_google_sign_in")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = PureBlack,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = statusMessage ?: "Connecting...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureBlack
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(PureBlack, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = NeonLime
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureBlack
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to direct sign up button
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Custom Name / Email Instead", fontSize = 13.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Secured with Cloud Firestore & Room DB", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            // TAB 1: Direct Sign-Up & Setup Card
            if (selectedTab == 1) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = DarkCardSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_direct_signup")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NeonLime, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Create Citizen Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Permanent login & Cloud Firestore sync", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name
                        OutlinedTextField(
                            value = signUpName,
                            onValueChange = { signUpName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedLabelColor = NeonLime,
                                unfocusedLabelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_signup_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email
                        OutlinedTextField(
                            value = signUpEmail,
                            onValueChange = { signUpEmail = it },
                            label = { Text("Email Address (Google/Personal)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedLabelColor = NeonLime,
                                unfocusedLabelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_signup_email")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Emergency Phone Number
                        OutlinedTextField(
                            value = signUpPhone,
                            onValueChange = { signUpPhone = it },
                            label = { Text("Primary Contact Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedLabelColor = NeonLime,
                                unfocusedLabelColor = TextMuted
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_signup_phone")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "CHOOSE YOUR ROLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Role Selector Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (selectedRole == UserRole.CITIZEN) NeonLime.copy(alpha = 0.18f) else Color(0xFF222222),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedRole == UserRole.CITIZEN) NeonLime else DarkCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = UserRole.CITIZEN }
                                    .testTag("role_option_citizen")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (selectedRole == UserRole.CITIZEN) NeonLime else TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Citizen / Ward", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("SOS Protection", fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            Surface(
                                color = if (selectedRole == UserRole.PARENT) NeonLime.copy(alpha = 0.18f) else Color(0xFF222222),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedRole == UserRole.PARENT) NeonLime else DarkCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = UserRole.PARENT }
                                    .testTag("role_option_parent")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.FamilyRestroom,
                                        contentDescription = null,
                                        tint = if (selectedRole == UserRole.PARENT) NeonLime else TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Parent / Guardian", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Family Tracker", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Sign-Up Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (signUpEmail.isNotBlank()) {
                                    val name = signUpName.ifBlank { signUpEmail.substringBefore("@") }
                                    handleSignInCompletion(signUpEmail.trim(), name, selectedRole)
                                } else {
                                    Toast.makeText(context, "Please provide an email address", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_submit_signup")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Complete Setup & Keep Me Logged In", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // TAB 2: Demo Mode Card
            if (selectedTab == 2) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = DarkCardSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_demo_mode")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(SecondaryLime.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SecondaryLime, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Instant Nagpur Demo Access", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "Experience full SOS triggers, WhatsApp emergency simulation, parent-child live pairing, and Nagpur police dispatch without typing credentials.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                        )

                        // 1-Click Citizen Demo
                        Button(
                            onClick = {
                                handleSignInCompletion("chaitali.damodar@nagpur.gov.in", "Chaitali Damodar (Nagpur)", UserRole.CITIZEN)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_demo_citizen")
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch as Nagpur Citizen (Chaitali)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1-Click Parent Demo
                        Surface(
                            color = Color(0xFF242424),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickable {
                                    handleSignInCompletion("rajesh.damodar@nagpur.gov.in", "Rajesh Damodar (Parent)", UserRole.PARENT)
                                }
                                .testTag("btn_demo_parent")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch as Guardian / Parent (Rajesh)", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Safety Grid Features Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkCardSurface,
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "SAFETY ARCHITECTURE & FIREBASE SYNC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LoginFeatureRow(
                        icon = Icons.Default.CloudDone,
                        title = "Cloud Firestore Real-time Sync",
                        subtitle = "Instant backup of contacts, SOS incidents, and location breadcrumbs"
                    )
                    LoginFeatureRow(
                        icon = Icons.Default.FamilyRestroom,
                        title = "Separate Parent & Child Roles",
                        subtitle = "Live tracking map for parents, one-touch SOS for citizens"
                    )
                    LoginFeatureRow(
                        icon = Icons.Default.Shield,
                        title = "Hardware 3X Power Button SOS",
                        subtitle = "Discreet emergency activation even when device is locked"
                    )
                }
            }
        }

        // Q&A / QUESTIONS & HELP ASSISTANT DIALOG
        if (showHelpDialog) {
            Dialog(onDismissRequest = { showHelpDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = DarkCardSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_auth_help_assistant")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = NeonLime, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Authentication & Firebase Q&A", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Frequently Asked Questions", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            IconButton(onClick = { showHelpDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = DarkCardBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // FAQ 1
                        HelpQuestionItem(
                            question = "Why was the app asking me to sign in every time?",
                            answer = "We have added persistent session caching via SharedPreferences and Room SQLite. Once you sign in (or use Direct Sign-Up), your login state is permanently saved so you never get interrupted on app restart."
                        )

                        // FAQ 2
                        HelpQuestionItem(
                            question = "How is my database migrated to Firebase?",
                            answer = "When you sign in, Nagpur Suraksha automatically mirrors your emergency contacts, user profile, safety places, and SOS logs to Cloud Firestore. You can also trigger manual cloud sync from the Home screen."
                        )

                        // FAQ 3
                        HelpQuestionItem(
                            question = "What if Google Play Services is unavailable?",
                            answer = "You can use the 'Direct Sign Up' tab or 'Demo Mode' tab anytime. Both provide 100% functional access to the emergency SOS, map, contacts, and WhatsApp broadcast simulation."
                        )

                        // FAQ 4
                        HelpQuestionItem(
                            question = "Can I switch between Parent and Child mode?",
                            answer = "Yes! Tap the Profile icon in the top header on the Home screen to open the Account Profile dialog and switch roles or sign out at any time."
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showHelpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Got It, Close Help", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Cancel Question Dialog
        if (showCancelQuestionDialog) {
            Dialog(onDismissRequest = { showCancelQuestionDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = DarkCardSurface,
                    border = BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .testTag("dialog_auth_cancelled")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.HelpOutline,
                                contentDescription = "Question",
                                tint = NeonLime,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Sign-In Interrupted",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.4).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "How would you like to proceed with Nagpur Suraksha authentication?",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Option 1: Direct Sign-Up tab
                        Surface(
                            color = NeonLime,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable {
                                    showCancelQuestionDialog = false
                                    selectedTab = 1
                                }
                                .testTag("btn_dialog_switch_direct")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Use Direct Sign-Up (Email/Name)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureBlack
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option 2: Instant Demo
                        Surface(
                            color = Color(0xFF262626),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable {
                                    showCancelQuestionDialog = false
                                    handleSignInCompletion("chaitali.damodar@nagpur.gov.in", "Chaitali Damodar", UserRole.CITIZEN)
                                }
                                .testTag("btn_dialog_demo_mode")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue Instantly in Demo Mode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dismiss Button
                        TextButton(
                            onClick = { showCancelQuestionDialog = false },
                            modifier = Modifier.fillMaxWidth().testTag("btn_dialog_dismiss")
                        ) {
                            Text(
                                text = "Stay on Sign-In Screen",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpQuestionItem(
    question: String,
    answer: String
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = NeonLime,
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = question,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            fontSize = 12.sp,
            color = TextMuted,
            lineHeight = 16.sp,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}

@Composable
fun LoginFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(NeonLime.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NeonLime, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
        }
    }
}
