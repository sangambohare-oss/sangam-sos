package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChildPairingScreen(
    viewModel: MainViewModel,
    onPairingComplete: () -> Unit,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyPairing by viewModel.familyPairing.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var enteredCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessCelebration by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SecondaryLime, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChildCare,
                        contentDescription = "Child Mode",
                        tint = PureBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CHILD PROTECTION",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.4).sp,
                        color = Color.White
                    )
                    Text(
                        text = "${googleUser.displayName.ifBlank { "Child" }}",
                        fontSize = 12.sp,
                        color = SecondaryLime
                    )
                }
            }

            Surface(
                color = Color(0xFF262626),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.clickable { onSwitchRole() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Role", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // If Already Paired, show Guardian Status Card (28dp radius)
        if (familyPairing.isChildPairedWithParent && familyPairing.pairedParentName != null) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = DarkCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("card_child_paired_guardian")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = "Paired",
                                    tint = NeonLime,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Connected to Guardian",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Priority 1 Emergency Contact",
                                    fontSize = 11.sp,
                                    color = NeonLime,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            color = NeonLime.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "● ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkCardBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "PARENT / GUARDIAN", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(text = familyPairing.pairedParentName ?: "Guardian", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PHONE / WHATSAPP", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(text = familyPairing.pairedParentPhone ?: "Not specified", fontSize = 13.sp, color = NeonLime, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color(0xFF222222),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "When you press power button 3x, live GPS coordinates are beamed to this guardian.",
                                fontSize = 11.sp,
                                color = Color(0xFFCCCCCC),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPairingComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(1.2f).height(44.dp).testTag("btn_goto_sos_home")
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Go to SOS Home", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.unpairChild()
                                Toast.makeText(context, "Unlinked from parent.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(0.8f).height(44.dp).testTag("btn_unlink_guardian")
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlink", fontSize = 12.sp, color = EmergencyRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // SECTION 2: ENTER CODE & PAIR FORM (28dp radius)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_child_enter_code")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = "Pair Code",
                        tint = NeonLime,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (familyPairing.isChildPairedWithParent) "Link Another Guardian Code" else "Enter Parent Pairing Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    color = Color.White
                )

                Text(
                    text = "Enter the 6-character code shown on parent's Nagpur Suraksha app (e.g. ${familyPairing.activePairingCode})",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                )

                // Code Input Field
                OutlinedTextField(
                    value = enteredCode,
                    onValueChange = {
                        enteredCode = it.uppercase(Locale.getDefault())
                        errorMessage = null
                    },
                    label = { Text("6-Digit Pairing Code (e.g. NAG-9284)") },
                    placeholder = { Text(familyPairing.activePairingCode) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedLabelColor = NeonLime,
                        unfocusedLabelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pairing_code"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                )

                // Error Message if any
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = EmergencyRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Helper Buttons: Paste & Auto-Fill Demo Code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clip.isNullOrBlank()) {
                                enteredCode = clip.trim().uppercase(Locale.getDefault())
                            } else {
                                Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp, color = TextMuted)
                    }

                    Button(
                        onClick = {
                            enteredCode = familyPairing.activePairingCode
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626)),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1.3f).height(40.dp).testTag("btn_autofill_active_code")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = NeonLime, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-fill Active Code", fontSize = 11.sp, color = NeonLime)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Verify & Pair Button (Neon Lime Pill)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (enteredCode.isBlank()) {
                            errorMessage = "Please enter the pairing code from parent."
                            return@Button
                        }
                        isVerifying = true
                        errorMessage = null
                        coroutineScope.launch {
                            delay(500)
                            val success = viewModel.pairWithParentCode(
                                enteredCode = enteredCode,
                                childName = googleUser.displayName.ifBlank { "Chaitali Damodar" },
                                childPhone = "+91 98765 43210"
                            )
                            isVerifying = false
                            if (success) {
                                showSuccessCelebration = true
                                Toast.makeText(context, "Pairing Successful! Parent is now linked.", Toast.LENGTH_LONG).show()
                            } else {
                                errorMessage = "Invalid code. Please check the code on parent's phone."
                            }
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
                        .testTag("btn_verify_pairing_code")
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PureBlack, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validating Code...", color = PureBlack, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.Link, contentDescription = null, tint = PureBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify & Pair with Parent", color = PureBlack, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success dialog or celebration
        AnimatedVisibility(visible = showSuccessCelebration) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E2F1E),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonLime, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device Successfully Linked!", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "Guardian (Rajesh Damodar) linked. You are now protected with real-time GPS beaconing.",
                        color = Color(0xFFE5E5E5),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                    )
                    Button(
                        onClick = {
                            showSuccessCelebration = false
                            onPairingComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Continue to Nagpur Suraksha Home", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step by Step Guide
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "HOW PAIRING WORKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                PairingStepItem(number = "1", title = "Parent creates code", desc = "Parent selects 'Parent Mode' and generates 6-digit code.")
                PairingStepItem(number = "2", title = "Child enters code", desc = "Child types the code above to establish handshake.")
                PairingStepItem(number = "3", title = "3x Power Button Ready", desc = "Press power button 3 times in danger to alert Parent & Police.")
            }
        }
    }
}

@Composable
fun PairingStepItem(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = Color(0xFF262626),
            shape = CircleShape,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = number, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonLime)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = desc, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
        }
    }
}
