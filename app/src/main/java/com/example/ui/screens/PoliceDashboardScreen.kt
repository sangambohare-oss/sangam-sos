package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.LocationBreadcrumbEntity
import com.example.ui.components.TacticalMapCanvas
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PoliceDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToParentTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allIncidents by viewModel.allIncidents.collectAsState()
    val activeIncidents by viewModel.activeIncidents.collectAsState()
    val safePlaces by viewModel.safePlaces.collectAsState()
    val selectedIncident by viewModel.selectedIncident.collectAsState()

    var isOfficerLoggedIn by remember { mutableStateOf(true) }
    var officerBadgeId by remember { mutableStateOf("MH-31-PC-4091") }
    var officerStation by remember { mutableStateOf("Sadar Control HQ, Nagpur") }
    var statusFilter by remember { mutableStateOf("ALL") }
    val context = LocalContext.current

    if (!isOfficerLoggedIn) {
        PoliceLoginView(
            onLoginSuccess = { badge, station ->
                officerBadgeId = badge
                officerStation = station
                isOfficerLoggedIn = true
            }
        )
        return
    }

    val filteredIncidents = remember(allIncidents, statusFilter) {
        when (statusFilter) {
            "ACTIVE" -> allIncidents.filter { it.status == "ACTIVE" || it.status == "ACKNOWLEDGED" || it.status == "RESPONDING" }
            "RESOLVED" -> allIncidents.filter { it.status == "RESOLVED" }
            else -> allIncidents
        }
    }

    if (selectedIncident != null) {
        IncidentDetailModal(
            incident = selectedIncident!!,
            viewModel = viewModel,
            onDismiss = { viewModel.selectIncident(null) },
            onOpenParentTracking = { token ->
                viewModel.selectIncident(null)
                onNavigateToParentTracking(token)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Control Center Header with Officer Info & Logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NeonLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalPolice,
                        contentDescription = "Police",
                        tint = PureBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "NAGPUR SURAKSHA",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                    Text(
                        text = "POLICE & RESCUE CONTROL CENTER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = NeonLime.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "🟢 LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = { isOfficerLoggedIn = false }) {
                    Icon(Icons.Default.Logout, contentDescription = "Officer Logout", tint = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Authenticated Officer Banner
        Surface(
            color = DarkCardSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👮 Officer: $officerBadgeId",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = officerStation,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 KPI Stat Cards for Real-Time Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiStatCard(
                label = "ACTIVE SOS",
                value = String.format("%02d", activeIncidents.count { it.status == "ACTIVE" || it.status == "RESPONDING" || it.status == "ACKNOWLEDGED" }),
                color = EmergencyRed,
                modifier = Modifier.weight(1f)
            )
            KpiStatCard(
                label = "RESOLVED",
                value = String.format("%02d", allIncidents.count { it.status == "RESOLVED" }),
                color = NeonLime,
                modifier = Modifier.weight(1f)
            )
            KpiStatCard(
                label = "TOTAL SOS",
                value = String.format("%02d", allIncidents.size),
                color = SecondaryLime,
                modifier = Modifier.weight(1f)
            )
            KpiStatCard(
                label = "CITIZENS",
                value = "823",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tactical Nagpur Map
        Text(
            text = "NAGPUR ACTIVE TACTICAL MAP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonLime,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        val latestActive = activeIncidents.firstOrNull() ?: allIncidents.firstOrNull()
        Surface(
            color = DarkCardSurface,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            TacticalMapCanvas(
                userLat = latestActive?.currentLatitude ?: 21.1458,
                userLng = latestActive?.currentLongitude ?: 79.0882,
                accuracy = latestActive?.accuracy ?: 8f,
                isSosActive = activeIncidents.isNotEmpty(),
                incidents = allIncidents,
                safePlaces = safePlaces,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp)),
                isDarkTheme = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = statusFilter == "ALL",
                onClick = { statusFilter = "ALL" },
                label = { Text("All (${allIncidents.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonLime,
                    selectedLabelColor = PureBlack,
                    containerColor = DarkCardSurface,
                    labelColor = TextMuted
                )
            )
            FilterChip(
                selected = statusFilter == "ACTIVE",
                onClick = { statusFilter = "ACTIVE" },
                label = { Text("Active (${activeIncidents.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmergencyRed,
                    selectedLabelColor = Color.White,
                    containerColor = DarkCardSurface,
                    labelColor = TextMuted
                )
            )
            FilterChip(
                selected = statusFilter == "RESOLVED",
                onClick = { statusFilter = "RESOLVED" },
                label = { Text("Resolved (${allIncidents.count { it.status == "RESOLVED" }})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonLime,
                    selectedLabelColor = PureBlack,
                    containerColor = DarkCardSurface,
                    labelColor = TextMuted
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Incident Cards List
        if (filteredIncidents.isEmpty()) {
            Surface(
                color = DarkCardSurface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "No emergency incidents in this view.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        } else {
            filteredIncidents.forEach { incident ->
                PoliceIncidentCard(
                    incident = incident,
                    onViewLiveLocation = { onNavigateToParentTracking(incident.trackingToken) },
                    onViewDetails = { viewModel.selectIncident(incident) },
                    onContactUser = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${incident.userPhone}"))
                        context.startActivity(callIntent)
                    },
                    onAcknowledge = { viewModel.acknowledgeIncident(incident.incidentCode) },
                    onRespond = { viewModel.respondIncident(incident.incidentCode, "PCR Van 07 dispatched from Sadar") },
                    onResolve = { viewModel.resolveIncident(incident.incidentCode) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PoliceLoginView(
    onLoginSuccess: (String, String) -> Unit
) {
    var badgeId by remember { mutableStateOf("MH-31-PC-4091") }
    var station by remember { mutableStateOf("Sadar Police HQ, Nagpur") }
    var pin by remember { mutableStateOf("1122") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(NeonLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LocalPolice,
                contentDescription = "Police Emblem",
                tint = PureBlack,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "POLICE & RESCUE DISPATCH",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp,
            color = Color.White
        )
        Text(
            text = "Nagpur City Police Control Center Portal",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "OFFICER AUTHENTICATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = badgeId,
                    onValueChange = { badgeId = it },
                    label = { Text("Officer Badge / ID") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = NeonLime) },
                    modifier = Modifier.fillMaxWidth().testTag("input_police_badge"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = station,
                    onValueChange = { station = it },
                    label = { Text("Police Station / Jurisdiction") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = NeonLime) },
                    modifier = Modifier.fillMaxWidth().testTag("input_police_station"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Security PIN (Default: 1122)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonLime) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().testTag("input_police_pin"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = errorMessage!!, color = EmergencyRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (badgeId.isNotBlank() && station.isNotBlank()) {
                            onLoginSuccess(badgeId, station)
                        } else {
                            errorMessage = "Please enter officer badge and station."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_police_login")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PureBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Access Control Dashboard", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = PureBlack)
                }
            }
        }
    }
}

@Composable
fun KpiStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCardSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun PoliceIncidentCard(
    incident: IncidentEntity,
    onViewLiveLocation: () -> Unit,
    onViewDetails: () -> Unit,
    onContactUser: () -> Unit,
    onAcknowledge: () -> Unit,
    onRespond: () -> Unit,
    onResolve: () -> Unit
) {
    val statusColor = when (incident.status) {
        "ACTIVE" -> EmergencyRed
        "RESPONDING" -> AlertAmber
        "ACKNOWLEDGED" -> Color(0xFFFDD835)
        else -> NeonLime
    }

    val timeFormatted = remember(incident.activatedAt) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(incident.activatedAt))
    }

    Surface(
        color = DarkCardSurface,
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (incident.status == "ACTIVE") EmergencyRed else DarkCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("incident_card_${incident.incidentCode}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with SOS Code & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🚨 SOS #${incident.incidentCode}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = incident.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User details
            Text(
                text = "Citizen: ${incident.userName} • ${incident.userPhone}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            // Location
            Text(
                text = "📍 ${incident.addressName}",
                fontSize = 12.sp,
                color = NeonLime
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time, Battery, Accuracy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time: $timeFormatted",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "🔋 ${incident.battery}%",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "Acc: ±${incident.accuracy.toInt()}m",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User-requested explicit actions: View Live Location, View Details, Contact User
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onViewLiveLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.weight(1.2f).height(38.dp).testTag("btn_view_live_loc_${incident.incidentCode}")
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PureBlack, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live Location", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PureBlack)
                }

                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.weight(0.9f).height(38.dp).testTag("btn_view_details_${incident.incidentCode}")
                ) {
                    Text("Details", fontSize = 10.sp, color = Color.White)
                }

                Button(
                    onClick = onContactUser,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626), contentColor = Color.White),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.weight(0.9f).height(38.dp).testTag("btn_contact_user_${incident.incidentCode}")
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = NeonLime, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Police Dispatch Lifecycle Status Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (incident.status == "ACTIVE") {
                    Button(
                        onClick = onAcknowledge,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text("Acknowledge Incident", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                } else if (incident.status == "ACKNOWLEDGED") {
                    Button(
                        onClick = onRespond,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertAmber),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch PCR Van 07", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                } else if (incident.status == "RESPONDING") {
                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = PureBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Incident Resolved", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PureBlack)
                    }
                }
            }
        }
    }
}

@Composable
fun IncidentDetailModal(
    incident: IncidentEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onOpenParentTracking: (String) -> Unit
) {
    val breadcrumbs by viewModel.getBreadcrumbs(incident.incidentCode).collectAsState(initial = emptyList())
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INCIDENT #${incident.incidentCode}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = TextMuted, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Citizen: ${incident.userName}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Phone: ${incident.userPhone}", color = NeonLime, fontSize = 13.sp)
                Text("Location: ${incident.addressName}", color = TextMuted, fontSize = 13.sp)
                Text("GPS: ${incident.currentLatitude}, ${incident.currentLongitude}", color = TextMuted, fontSize = 11.sp)
                Text("Battery: ${incident.battery}% • Speed: ${incident.speed} km/h • Acc: ±${incident.accuracy.toInt()}m", color = TextMuted, fontSize = 11.sp)

                if (incident.responderNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Responder Log: ${incident.responderNotes}", color = AlertAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mini Map of breadcrumbs
                TacticalMapCanvas(
                    userLat = incident.currentLatitude,
                    userLng = incident.currentLongitude,
                    accuracy = incident.accuracy,
                    isSosActive = incident.status != "RESOLVED",
                    breadcrumbs = breadcrumbs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    isDarkTheme = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Location Breadcrumb Trail
                Text(
                    text = "LOCATION BREADCRUMBS (${breadcrumbs.size} Points)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                breadcrumbs.takeLast(4).reversed().forEach { bc ->
                    val t = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(bc.timestamp))
                    Text(
                        text = "• $t — ${bc.address} (${bc.battery}%)",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${incident.userPhone}"))
                            context.startActivity(callIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626)),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Contact User", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onOpenParentTracking(incident.trackingToken) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Live Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PureBlack)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Lifecycle Actions
                if (incident.status == "ACTIVE") {
                    Button(
                        onClick = {
                            viewModel.acknowledgeIncident(incident.incidentCode)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Acknowledge Incident", fontWeight = FontWeight.SemiBold)
                    }
                } else if (incident.status == "ACKNOWLEDGED") {
                    Button(
                        onClick = {
                            viewModel.respondIncident(incident.incidentCode, "PCR Van 07 dispatched")
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertAmber),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dispatch Response Unit", fontWeight = FontWeight.SemiBold)
                    }
                } else if (incident.status == "RESPONDING") {
                    Button(
                        onClick = {
                            viewModel.resolveIncident(incident.incidentCode)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Incident Resolved", fontWeight = FontWeight.SemiBold, color = PureBlack)
                    }
                }
            }
        }
    }
}
