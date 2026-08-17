package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmergencyCountdownDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.EmergencyRedGlow
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToContacts: () -> Unit,
    onNavigateToSafePlaces: () -> Unit,
    onNavigateToEmergencyNumbers: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSos by viewModel.activeSos.collectAsState()
    val powerClickCount by viewModel.powerClickCount.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val familyPairing by viewModel.familyPairing.collectAsState()
    val migrationState by viewModel.migrationState.collectAsState()

    // Countdown Dialog
    if (activeSos.isCountingDown) {
        EmergencyCountdownDialog(
            secondsRemaining = activeSos.countdownSecondsRemaining,
            onCancel = { viewModel.cancelSosCountdown() },
            onTriggerNow = { viewModel.triggerSosImmediate() }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Brand Header in Geometric Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(NeonLime, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = PureBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NAGPUR SURAKSHA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.8).sp,
                    color = Color.White
                )
            }
            Text(
                text = "Emergency SOS & Live Guardian Network",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )

            userProfile?.let { profile ->
                Surface(
                    color = Color(0xFF222222),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Citizen: ${profile.name} • ${profile.bloodGroup}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeonLime,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Power Button 3x Indicator Card (Rounded 28dp card)
        Surface(
            color = DarkCardSurface,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDiagnostics() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF262626), RoundedCornerShape(999.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Hardware trigger",
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Power Button × 3 Trigger",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp,
                            color = Color.White
                        )
                        Text(
                            text = "Click power button 3 times anywhere to trigger",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Quick test button for 3x power click simulation
                Button(
                    onClick = { viewModel.simulatePowerButtonClick() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("simulate_power_button")
                ) {
                    Text(
                        text = if (powerClickCount > 0) "Click $powerClickCount/3" else "Test 3x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Guardian Pairing Status Card (28dp radius)
        Surface(
            color = DarkCardSurface,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (familyPairing.isChildPairedWithParent) NeonLime.copy(alpha = 0.4f) else DarkCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPairing() }
                .testTag("banner_paired_guardian")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (familyPairing.isChildPairedWithParent) NeonLime.copy(alpha = 0.15f) else Color(0xFF262626),
                                RoundedCornerShape(999.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (familyPairing.isChildPairedWithParent) Icons.Default.Shield else Icons.Default.Security,
                            contentDescription = "Guardian Status",
                            tint = if (familyPairing.isChildPairedWithParent) NeonLime else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (familyPairing.isChildPairedWithParent)
                                "Paired: ${familyPairing.pairedParentName ?: "Guardian"}"
                            else
                                "Family Link Inactive",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp,
                            color = Color.White
                        )
                        Text(
                            text = if (familyPairing.isChildPairedWithParent)
                                "Live SOS & GPS broadcast active"
                            else
                                "Tap to link 6-character guardian code",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    color = if (familyPairing.isChildPairedWithParent) NeonLime else Color(0xFF2E2E2E),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = if (familyPairing.isChildPairedWithParent) "LINKED" else "PAIR CODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (familyPairing.isChildPairedWithParent) PureBlack else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Giant SOS Button (Central Hero Element with Neon Lime Glow Accent)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .scale(pulseScale)
                .testTag("sos_button_container")
        ) {
            // Outer Halo Ring
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                EmergencyRed.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            // Outer Stroke Ring
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .border(4.dp, NeonLime.copy(alpha = 0.5f), CircleShape)
            )

            // Inner SOS Push Button
            Surface(
                onClick = { viewModel.triggerSosCountdown("BIG_BUTTON") },
                shape = CircleShape,
                color = EmergencyRed,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .size(175.dp)
                    .testTag("main_sos_button")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SOS",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-1.0).sp,
                        color = Color.White
                    )
                    Surface(
                        color = PureBlack.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "HOLD TO TRIGGER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "HOLD 3s FOR INSTANT BROADCAST",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3 Simple Primary Cards with rounded 28dp radius and neon hover styling
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Emergency Contacts",
                subtitle = "Active guardians configured for SMS & WhatsApp alert",
                icon = Icons.Default.Contacts,
                accentColor = NeonLime,
                testTag = "btn_nav_contacts",
                onClick = onNavigateToContacts
            )

            ActionCard(
                title = "Safe Places Near You",
                subtitle = "Nagpur Police Stations, Hospitals, 24/7 Shelters",
                icon = Icons.Default.LocationCity,
                accentColor = NeonLime,
                testTag = "btn_nav_safe_places",
                onClick = onNavigateToSafePlaces
            )

            ActionCard(
                title = "Emergency Helplines",
                subtitle = "112 Police, 108 Ambulance, 101 Fire, 1091 Women",
                icon = Icons.Default.PhoneInTalk,
                accentColor = EmergencyRed,
                testTag = "btn_nav_emergency_numbers",
                onClick = onNavigateToEmergencyNumbers
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Firebase Cloud Firestore Sync Card (28dp radius)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firebase_sync_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(NeonLime.copy(alpha = 0.15f), RoundedCornerShape(999.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (migrationState.status == com.example.data.repository.MigrationStatus.SUCCESS) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = "Firebase Sync",
                                tint = NeonLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Firebase Cloud Sync",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp,
                                color = Color.White
                            )
                            Text(
                                text = if (migrationState.totalRecordsMigrated > 0)
                                    "${migrationState.totalRecordsMigrated} records backed up in Cloud"
                                else
                                    "Ready to sync Room to Cloud",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Surface(
                        color = when (migrationState.status) {
                            com.example.data.repository.MigrationStatus.SUCCESS -> NeonLime.copy(alpha = 0.15f)
                            com.example.data.repository.MigrationStatus.MIGRATING -> Color(0xFF332200)
                            com.example.data.repository.MigrationStatus.ERROR -> Color(0xFF330000)
                            com.example.data.repository.MigrationStatus.IDLE -> Color(0xFF262626)
                        },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = when (migrationState.status) {
                                com.example.data.repository.MigrationStatus.SUCCESS -> "● Synced"
                                com.example.data.repository.MigrationStatus.MIGRATING -> "⏳ Syncing..."
                                com.example.data.repository.MigrationStatus.ERROR -> "⚠️ Retry"
                                com.example.data.repository.MigrationStatus.IDLE -> "○ Ready"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (migrationState.status) {
                                com.example.data.repository.MigrationStatus.SUCCESS -> NeonLime
                                com.example.data.repository.MigrationStatus.MIGRATING -> Color(0xFFFFB800)
                                com.example.data.repository.MigrationStatus.ERROR -> EmergencyRedGlow
                                com.example.data.repository.MigrationStatus.IDLE -> TextMuted
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats breakdown pills (pill radius 999dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SyncPill("Contacts", "${migrationState.contactsCount}", Modifier.weight(1f))
                    SyncPill("Places", "${if (migrationState.safePlacesCount > 0) migrationState.safePlacesCount else 12}", Modifier.weight(1f))
                    SyncPill("Incidents", "${migrationState.incidentsCount}", Modifier.weight(1f))
                    SyncPill("Profiles", "${if (migrationState.userProfilesCount > 0) migrationState.userProfilesCount else 1}", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button to trigger migration (fully rounded)
                Button(
                    onClick = { viewModel.migrateDataToFirebase() },
                    enabled = migrationState.status != com.example.data.repository.MigrationStatus.MIGRATING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_migrate_firebase")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = PureBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (migrationState.status == com.example.data.repository.MigrationStatus.MIGRATING)
                            "Migrating to Firestore..."
                        else
                            "Sync All Data with Cloud Firestore",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
fun SyncPill(
    label: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF262626),
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonLime
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = DarkCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (accentColor == NeonLime) NeonLime.copy(alpha = 0.15f) else accentColor.copy(alpha = 0.15f),
                            RoundedCornerShape(999.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF262626), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "›",
                    fontSize = 18.sp,
                    color = NeonLime,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
