package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PairedChildInfo
import com.example.data.model.UserRole
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentPairingScreen(
    viewModel: MainViewModel,
    onNavigateToLiveTracking: (String) -> Unit,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val familyPairing by viewModel.familyPairing.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val activeSos by viewModel.activeSos.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showSimulateAddDialog by remember { mutableStateOf(false) }
    var newChildName by remember { mutableStateOf("") }
    var newChildPhone by remember { mutableStateOf("") }

    if (showSimulateAddDialog) {
        Dialog(onDismissRequest = {
            focusManager.clearFocus()
            showSimulateAddDialog = false
        }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = DarkCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Simulate Child Device Link",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                    Text(
                        text = "Quickly simulate a child entering pairing code ${familyPairing.activePairingCode} on their device.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = newChildName,
                        onValueChange = { newChildName = it },
                        label = { Text("Child / Ward Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedLabelColor = NeonLime,
                            unfocusedLabelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newChildPhone,
                        onValueChange = { newChildPhone = it },
                        label = { Text("Child Phone Number") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonLime,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedLabelColor = NeonLime,
                            unfocusedLabelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                showSimulateAddDialog = false
                            },
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Cancel", color = TextMuted)
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (newChildName.isNotBlank()) {
                                    viewModel.addPairedChild(newChildName, newChildPhone)
                                    showSimulateAddDialog = false
                                    Toast.makeText(context, "Child device linked successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Link Device", fontWeight = FontWeight.SemiBold, color = PureBlack)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Parent Header & Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(NeonLime, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FamilyRestroom,
                        contentDescription = "Parent",
                        tint = PureBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PARENT HUB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.4).sp,
                        color = Color.White
                    )
                    Text(
                        text = "Guardian: ${googleUser.displayName.ifBlank { "Parent" }}",
                        fontSize = 12.sp,
                        color = NeonLime
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

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: CODE GENERATOR FOR PAIRING CHILD (28dp radius)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_parent_pairing_code")
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
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHILD PAIRING CODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            letterSpacing = 0.8.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.generateNewPairingCode() },
                        modifier = Modifier.size(28.dp).testTag("btn_refresh_pairing_code")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Code Display (Pill Container)
                Surface(
                    color = Color(0xFF242424),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Pairing Code", familyPairing.activePairingCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied: ${familyPairing.activePairingCode}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = familyPairing.activePairingCode,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 4.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonLime,
                            modifier = Modifier.testTag("text_parent_active_code")
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap code to copy", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Give this code to your child to enter on their phone in Nagpur Suraksha under 'Child Mode'.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Nagpur Suraksha Family Pairing Code")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Your Nagpur Suraksha guardian pairing code is: ${familyPairing.activePairingCode}. Enter this in the app under Child Mode to link your device."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Pairing Code"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1.3f).height(44.dp).testTag("btn_share_pairing_code")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Code", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PureBlack)
                    }

                    OutlinedButton(
                        onClick = { showSimulateAddDialog = true },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("btn_simulate_child_link")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Quick Link", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: PAIRED CHILDREN LIST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAIRED WARDS (${familyPairing.pairedChildren.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonLime,
                letterSpacing = 0.8.sp
            )

            if (familyPairing.pairedChildren.isNotEmpty()) {
                Surface(
                    color = NeonLime.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "● Live Telemetry",
                        fontSize = 10.sp,
                        color = NeonLime,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (familyPairing.pairedChildren.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ChildCare,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Child Devices Linked Yet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Share pairing code ${familyPairing.activePairingCode} or tap '+ Quick Link' above.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                familyPairing.pairedChildren.forEach { child ->
                    PairedChildItemCard(
                        child = child,
                        onTrackLive = {
                            onNavigateToLiveTracking(child.pairingCode)
                        },
                        onDelete = {
                            viewModel.removePairedChild(child.childId)
                            Toast.makeText(context, "Unlinked ${child.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun PairedChildItemCard(
    child: PairedChildInfo,
    onTrackLive: () -> Unit,
    onDelete: () -> Unit
) {
    val isEmergency = child.safetyStatus == "SOS_TRIGGERED" || child.isSosActive
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val lastUpdate = timeFormat.format(Date(child.lastSeenTime))

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = DarkCardSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isEmergency) EmergencyRed else DarkCardBorder
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_paired_child_${child.childId}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isEmergency) EmergencyRed.copy(alpha = 0.2f) else NeonLime.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEmergency) Icons.Default.NotificationsActive else Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = if (isEmergency) EmergencyRed else NeonLime,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = child.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp,
                            color = Color.White
                        )
                        Text(
                            text = child.phone,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    color = if (isEmergency) EmergencyRed.copy(alpha = 0.2f) else NeonLime.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = if (isEmergency) "🚨 SOS ALERT" else "● ${child.safetyStatus}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEmergency) EmergencyRed else NeonLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DarkCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonLime, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = child.locationAddress,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
                Text(text = "Updated $lastUpdate", fontSize = 11.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTrackLive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEmergency) EmergencyRed else NeonLime,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.weight(1.5f).height(42.dp).testTag("btn_track_child_${child.childId}")
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEmergency) "View Live SOS Map" else "Live GPS Track", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Unlink", tint = TextMuted)
                }
            }
        }
    }
}
