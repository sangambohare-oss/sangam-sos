package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.SafeGreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DemoStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val actionText: String
)

@Composable
fun DemoModeScreen(
    viewModel: MainViewModel,
    onNavigateToCitizenHome: () -> Unit,
    onNavigateToPolice: () -> Unit,
    onNavigateToWhatsApp: () -> Unit,
    onNavigateToParentTracking: (String) -> Unit,
    onNavigateToParentPairing: () -> Unit = {},
    onNavigateToChildPairing: () -> Unit = {},
    onNavigateToRoleSelection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeSos by viewModel.activeSos.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val familyPairing by viewModel.familyPairing.collectAsState()
    val scope = rememberCoroutineScope()
    var isRunningAutomatedDemo by remember { mutableStateOf(false) }
    var automatedStepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf(
        DemoStep(1, "Google Sign-In & Role Select", "Authenticate with Google and select Parent or Child role", Icons.Default.Shield, "Role Selection"),
        DemoStep(2, "Parent Generates Pairing Code", "Parent generates unique code (e.g. ${familyPairing.activePairingCode}) to pair child", Icons.Default.FamilyRestroom, "Parent Pairing Hub"),
        DemoStep(3, "Child Enters Pairing Code", "Child inputs parent's 6-digit code to establish verified link", Icons.Default.CheckCircle, "Child Code Entry"),
        DemoStep(4, "Trigger SOS Countdown", "Simulate citizen tapping SOS or pressing power button 3x", Icons.Default.Timer, "Trigger Countdown"),
        DemoStep(5, "Activate Emergency State", "SOS activates, GPS locked, foreground tracking begins", Icons.Default.Shield, "Activate SOS"),
        DemoStep(6, "Dispatch WhatsApp Alerts", "Formatted emergency alert message sent to parents", Icons.Default.NotificationsActive, "View Mock WhatsApp"),
        DemoStep(7, "Police Incident Response", "Incident shows up in Police Dashboard; Officer acknowledges", Icons.Default.LocalPolice, "Open Police Dashboard"),
        DemoStep(8, "Parent Real-time Tracking", "Open live tracking link with live breadcrumbs and movement", Icons.Default.FamilyRestroom, "Open Parent Portal")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Card(
            colors = CardDefaults.cardColors(containerColor = PoliceNavy),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Demo", tint = Color(0xFFFFD54F), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HACKATHON DEMO MODE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (activeSos.isTriggered) "SOS: ACTIVE" else "SOS: IDLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSos.isTriggered) EmergencyRed else SafeGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Execute the complete Nagpur Suraksha end-to-end lifecycle manually step-by-step or with 1-click automation.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Automated Demo Button
                Button(
                    onClick = {
                        isRunningAutomatedDemo = true
                        scope.launch {
                            automatedStepIndex = 1
                            viewModel.triggerSosCountdown("DEMO_AUTO")
                            delay(3200L)
                            automatedStepIndex = 2
                            delay(2000L)
                            automatedStepIndex = 3
                            delay(2000L)
                            automatedStepIndex = 4
                            activeSos.incidentCode?.let { viewModel.acknowledgeIncident(it) }
                            delay(2000L)
                            activeSos.incidentCode?.let { viewModel.respondIncident(it, "PCR Van 07 dispatched from Sadar") }
                            automatedStepIndex = 5
                            delay(2000L)
                            automatedStepIndex = 6
                            isRunningAutomatedDemo = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isRunningAutomatedDemo,
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_run_auto_demo")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunningAutomatedDemo) "Running Step $automatedStepIndex of 6..." else "⚡ RUN 1-CLICK AUTOMATED DEMO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isRunningAutomatedDemo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { automatedStepIndex / 6f },
                        color = Color(0xFFFFD54F),
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "STEP-BY-STEP WORKFLOW WALKTHROUGH",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PoliceNavy,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        steps.forEach { step ->
            val isCurrent = automatedStepIndex == step.stepNumber
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFFFFF8E1) else Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrent) AlertAmber else Color(0xFFE0E0E0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (isCurrent) AlertAmber else Color(0xFFE8EAF6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step.stepNumber}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.Black else PoliceNavy
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = step.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                            Text(
                                text = step.description,
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            when (step.stepNumber) {
                                1 -> onNavigateToRoleSelection()
                                2 -> onNavigateToParentPairing()
                                3 -> onNavigateToChildPairing()
                                4 -> viewModel.triggerSosCountdown("DEMO_STEP_4")
                                5 -> viewModel.triggerSosImmediate()
                                6 -> onNavigateToWhatsApp()
                                7 -> onNavigateToPolice()
                                8 -> onNavigateToParentTracking(activeSos.trackingToken ?: "demo-token")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(text = step.actionText, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
