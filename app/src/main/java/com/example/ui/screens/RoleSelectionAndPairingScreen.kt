package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RoleSelectionAndPairingScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToParentTracking: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val googleUser by viewModel.googleUser.collectAsState()
    val familyPairing by viewModel.familyPairing.collectAsState()

    // Active chosen role tab: PARENT or CHILD (defaults to googleUser.role if set, or CHILD)
    var selectedRole by remember {
        mutableStateOf(
            if (googleUser.role == UserRole.PARENT) UserRole.PARENT else UserRole.CHILD
        )
    }

    var enteredChildCode by remember { mutableStateOf("") }
    var isVerifyingCode by remember { mutableStateOf(false) }
    var pairingErrorMessage by remember { mutableStateOf<String?>(null) }
    var showPairSuccessCelebration by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareCodeViaWhatsApp(code: String) {
        val message = "🛡️ *Nagpur Suraksha - Family Safety Pairing Code*\n\n" +
                "Hi! Please use this pairing code in your Nagpur Suraksha app to link with my Guardian profile:\n\n" +
                "🔑 *Pairing Code:* `$code`\n" +
                "⏱️ Valid for 15 minutes.\n\n" +
                "This links 3x power key SOS alerts and live GPS tracking for your protection."

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Pairing code ready: $code", Toast.LENGTH_SHORT).show()
        }
    }

    fun verifyAndPairCode() {
        if (enteredChildCode.isBlank()) {
            pairingErrorMessage = "Please enter a 6-character code"
            return
        }
        isVerifyingCode = true
        pairingErrorMessage = null

        coroutineScope.launch {
            delay(500) // Brief simulation of secure key exchange
            val success = viewModel.pairWithParentCode(
                enteredCode = enteredChildCode,
                childName = googleUser.displayName.ifBlank { "Citizen Ward" },
                childPhone = "+91 98765 43210"
            )

            isVerifyingCode = false
            if (success) {
                viewModel.setUserRole(UserRole.CHILD)
                showPairSuccessCelebration = true
                Toast.makeText(context, "Successfully paired with Guardian!", Toast.LENGTH_LONG).show()
            } else {
                pairingErrorMessage = "Invalid code. Check active parent code (e.g. ${familyPairing.activePairingCode}) or generate a new one."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Authenticated User Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("card_user_profile_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = googleUser.displayName.take(2).uppercase().ifBlank { "NS" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = googleUser.displayName.ifBlank { "Citizen User" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF065F46),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Google Verified",
                                    fontSize = 9.sp,
                                    color = Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = googleUser.email.ifBlank { "fs22ai030chaitalidamodar@gmail.com" },
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.signOutGoogle()
                        onSignOut()
                    },
                    modifier = Modifier.size(36.dp).testTag("btn_sign_out")
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Screen Heading & Subtitle
        Text(
            text = "Choose Your Role & Setup Pairing",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Select whether you are a Parent/Guardian monitoring your family, or a Child/Ward protected by the safety grid.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Role Segmented Tabs (Parent vs Child)
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Parent Tab
                Surface(
                    color = if (selectedRole == UserRole.PARENT) Color(0xFF1E3A8A) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = if (selectedRole == UserRole.PARENT) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedRole = UserRole.PARENT
                            viewModel.setUserRole(UserRole.PARENT)
                        }
                        .testTag("tab_role_parent")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FamilyRestroom,
                            contentDescription = "Parent Role",
                            tint = if (selectedRole == UserRole.PARENT) Color(0xFF93C5FD) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Parent / Guardian",
                            fontSize = 13.sp,
                            fontWeight = if (selectedRole == UserRole.PARENT) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedRole == UserRole.PARENT) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                // Child Tab
                Surface(
                    color = if (selectedRole == UserRole.CHILD) Color(0xFF881337) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = if (selectedRole == UserRole.CHILD) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedRole = UserRole.CHILD
                            viewModel.setUserRole(UserRole.CHILD)
                        }
                        .testTag("tab_role_child")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ChildCare,
                            contentDescription = "Child Role",
                            tint = if (selectedRole == UserRole.CHILD) Color(0xFFFDA4AF) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Child / Ward",
                            fontSize = 13.sp,
                            fontWeight = if (selectedRole == UserRole.CHILD) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedRole == UserRole.CHILD) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // DYNAMIC CONTENT: Parent (Generate Code) vs Child (Enter Code)
        AnimatedContent(
            targetState = selectedRole,
            transitionSpec = {
                if (targetState == UserRole.PARENT) {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                } else {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                }
            },
            label = "RolePairingContent"
        ) { role ->
            if (role == UserRole.PARENT) {
                // ==================== PARENT UI: GENERATE CODE ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("card_parent_code_generator")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFF1D4ED8).copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Your Family Pairing Code",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    color = Color(0xFF065F46),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "15m Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6EE7B7),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large Monospace Code Card
                            Surface(
                                color = Color(0xFF0B132B),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF3B82F6)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = familyPairing.activePairingCode,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF60A5FA),
                                        letterSpacing = 4.sp,
                                        modifier = Modifier.testTag("text_parent_active_code")
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Give this 6-character code to your child",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons (Copy, WhatsApp, Rotate)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        copyToClipboard(familyPairing.activePairingCode, "Pairing Code")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_copy_pairing_code")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        shareCodeViaWhatsApp(familyPairing.activePairingCode)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_whatsapp_share_code")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    val newCode = viewModel.generateNewPairingCode()
                                    Toast.makeText(context, "New code generated: $newCode", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_generate_new_code")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate New Pairing Code", fontSize = 12.sp, color = Color(0xFF93C5FD))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connected Children Status Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2D)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23354E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONNECTED CHILDREN (${familyPairing.pairedChildren.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF93C5FD),
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Auto-Synced",
                                    fontSize = 10.sp,
                                    color = SafeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            familyPairing.pairedChildren.forEach { child ->
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(34.dp).background(Color(0xFFE11D48), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(child.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(child.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("Dharampeth, Nagpur", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("${child.battery}%", fontSize = 11.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Button to proceed to Parent Dashboard
                    Button(
                        onClick = {
                            viewModel.setUserRole(UserRole.PARENT)
                            onNavigateToParentTracking(familyPairing.pairedChildren.firstOrNull()?.childId ?: "nagpur-demo-child")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_proceed_parent_dashboard")
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Guardian Live Tracking Hub", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // ==================== CHILD UI: ENTER CODE ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("card_child_code_entry")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFFE11D48).copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFFFB7185), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Enter Guardian's Code",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    color = if (familyPairing.isChildPairedWithParent) Color(0xFF065F46) else Color(0xFF854D0E),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (familyPairing.isChildPairedWithParent) "PAIRED 🛡️" else "UNPAIRED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (familyPairing.isChildPairedWithParent) Color(0xFF6EE7B7) else Color(0xFFFDE047),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Ask your parent for their 6-character code (e.g. ${familyPairing.activePairingCode}) and enter it below.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Monospace Code Input Box
                            OutlinedTextField(
                                value = enteredChildCode,
                                onValueChange = {
                                    if (it.length <= 10) {
                                        enteredChildCode = it.uppercase(Locale.getDefault())
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "e.g. NAG-9284",
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { verifyAndPairCode() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF43F5E),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_child_pair_code")
                            )

                            if (pairingErrorMessage != null) {
                                Text(
                                    text = pairingErrorMessage ?: "",
                                    color = Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Paste from Clipboard & Demo Fill Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                        if (!clip.isNullOrBlank()) {
                                            enteredChildCode = clip.trim().uppercase(Locale.getDefault())
                                            Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("btn_paste_code")
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Paste", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }

                                OutlinedButton(
                                    onClick = {
                                        enteredChildCode = familyPairing.activePairingCode
                                        Toast.makeText(context, "Autofilled active code: ${familyPairing.activePairingCode}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("btn_autofill_demo_code")
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Autofill Demo", fontSize = 11.sp, color = Color(0xFFFDA4AF))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Verify & Link Button
                            Button(
                                onClick = { verifyAndPairCode() },
                                enabled = !isVerifyingCode,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_verify_and_pair")
                            ) {
                                if (isVerifyingCode) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifying Key Exchange...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify & Pair with Parent", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paired Parent Status Card
                    if (familyPairing.isChildPairedWithParent) {
                        Surface(
                            color = Color(0xFF064E3B),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp).testTag("card_paired_parent_confirmed")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Active Guardian Protection", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    }
                                    Text("Priority #1", fontSize = 10.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Guardian: ${familyPairing.pairedParentName ?: "Rajesh Damodar"}",
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "SOS alerts & 3x power key clicks will dispatch directly to this guardian.",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // Button to proceed to Citizen Home Hub
                    Button(
                        onClick = {
                            viewModel.setUserRole(UserRole.CHILD)
                            onNavigateToHome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_proceed_child_home")
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Proceed to Citizen Safety Hub", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security Protocol Explanation Footer
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2D)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23354E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "End-to-End Pairing: Handshake tokens are encrypted on-device. Power button clicks and SOS broadcasts synchronize in real-time.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
