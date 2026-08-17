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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncidentStatus
import com.example.ui.components.SafeCancelConfirmationDialog
import com.example.ui.components.TacticalMapCanvas
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
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
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SosActiveScreen(
    viewModel: MainViewModel,
    onNavigateToParentTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSos by viewModel.activeSos.collectAsState()
    val safePlaces by viewModel.safePlaces.collectAsState()
    val breadcrumbs by viewModel.getBreadcrumbs(activeSos.incidentCode ?: "").collectAsState(initial = emptyList())
    var showCancelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showCancelDialog) {
        SafeCancelConfirmationDialog(
            onConfirmCancel = {
                showCancelDialog = false
                viewModel.cancelActiveSos("User confirmed safe from Active SOS view")
            },
            onKeepActive = {
                showCancelDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Emergency Banner (28dp radius)
        Surface(
            color = EmergencyRed,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_sos_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🚨 SOS ACTIVE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.4).sp,
                            color = Color.White
                        )
                        Text(
                            text = "Incident #${activeSos.incidentCode ?: "NS-LIVE"}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Battery Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = if (activeSos.location.battery < 20) Icons.Default.BatteryAlert else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (activeSos.location.battery < 20) Color(0xFFFF5252) else NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activeSos.location.battery}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Low Battery Banner Warning
        AnimatedVisibility(visible = activeSos.location.battery < 15) {
            Surface(
                color = Color(0xFF3B1E2B),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Low battery", tint = NeonLime)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "⚠️ LOW BATTERY (${activeSos.location.battery}%): Location updates optimized. Emergency contacts alerted.",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Coordination Status Grid
        Surface(
            color = DarkCardSurface,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "EMERGENCY DISPATCH STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                SosStatusRow(
                    icon = Icons.Default.ShareLocation,
                    label = "Live GPS Location",
                    value = "Broadcasting every 4s",
                    statusColor = NeonLime
                )
                Spacer(modifier = Modifier.height(10.dp))
                SosStatusRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Parents / Contacts",
                    value = "WhatsApp Alert Sent",
                    statusColor = NeonLime
                )
                Spacer(modifier = Modifier.height(10.dp))

                val policeStatusText = when (activeSos.status) {
                    IncidentStatus.ACTIVE -> "Alerted — Awaiting Officer"
                    IncidentStatus.ACKNOWLEDGED -> "Acknowledged by Control"
                    IncidentStatus.RESPONDING -> "🚔 PCR Van Responding"
                    IncidentStatus.RESOLVED -> "Resolved"
                    IncidentStatus.CANCELLED -> "Cancelled"
                }
                val policeStatusColor = when (activeSos.status) {
                    IncidentStatus.ACTIVE -> AlertAmber
                    IncidentStatus.ACKNOWLEDGED -> Color(0xFFFDD835)
                    IncidentStatus.RESPONDING -> NeonLime
                    else -> Color.White
                }
                SosStatusRow(
                    icon = Icons.Default.LocalPolice,
                    label = "Nagpur Police",
                    value = policeStatusText,
                    statusColor = policeStatusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Tactical Map View
        Surface(
            color = DarkCardSurface,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Pin",
                            tint = NeonLime,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeSos.location.address,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    activeSos.trackingToken?.let { token ->
                        Surface(
                            color = Color(0xFF262626),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.clickable { onNavigateToParentTracking(token) }
                        ) {
                            Text(
                                text = "Parent View ↗",
                                fontSize = 11.sp,
                                color = NeonLime,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TacticalMapCanvas(
                    userLat = activeSos.location.latitude,
                    userLng = activeSos.location.longitude,
                    accuracy = activeSos.location.accuracy,
                    speed = activeSos.location.speed,
                    isSosActive = true,
                    breadcrumbs = breadcrumbs,
                    safePlaces = safePlaces,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    isDarkTheme = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Speed: ${String.format("%.1f", activeSos.location.speed)} km/h",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Acc: ±${activeSos.location.accuracy.toInt()}m",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Breadcrumbs: ${breadcrumbs.size}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Big "I'M SAFE — CANCEL SOS" Button
        Button(
            onClick = { showCancelDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("im_safe_cancel_button")
        ) {
            Icon(Icons.Default.Shield, contentDescription = "Safe", tint = PureBlack)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "I'M SAFE — CANCEL SOS",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                color = PureBlack
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Direct Helpline Calling Buttons
        Text(
            text = "ONE-TAP DIRECT CALLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonLime,
            letterSpacing = 0.8.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HelplineButton(
                title = "Police 112",
                phone = "112",
                color = Color(0xFF262626),
                modifier = Modifier.weight(1f)
            )
            HelplineButton(
                title = "Ambulance 108",
                phone = "108",
                color = Color(0xFF262626),
                modifier = Modifier.weight(1f)
            )
            HelplineButton(
                title = "Women 1091",
                phone = "1091",
                color = Color(0xFF262626),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nearest Nagpur Safe Places
        Text(
            text = "NEAREST SAFE PLACES (NAGPUR)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonLime,
            letterSpacing = 0.8.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(10.dp))

        val sortedPlaces = safePlaces.take(3)
        sortedPlaces.forEach { place ->
            val dist = viewModel.calculateDistance(place.latitude, place.longitude)
            Surface(
                color = DarkCardSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "${place.area} • ${String.format("%.1f", dist)} km away",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = {
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                            context.startActivity(callIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = PureBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Call", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PureBlack)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SosStatusRow(
    icon: ImageVector,
    label: String,
    value: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.White
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
}

@Composable
fun HelplineButton(
    title: String,
    phone: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        color = color,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(intent)
            }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}
