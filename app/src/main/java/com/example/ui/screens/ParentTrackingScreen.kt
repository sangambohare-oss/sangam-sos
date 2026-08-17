package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TacticalMapCanvas
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.SafeGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentTrackingScreen(
    token: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSos by viewModel.activeSos.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val breadcrumbs by viewModel.getBreadcrumbs(activeSos.incidentCode ?: "").collectAsState(initial = emptyList())
    val safePlaces by viewModel.safePlaces.collectAsState()
    val context = LocalContext.current

    val userName = userProfile?.name?.ifBlank { null } ?: "Child User"
    val userPhone = userProfile?.phone?.ifBlank { null } ?: "Not specified"

    val isSosActive = activeSos.isTriggered

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Secure Parent Portal Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FamilyRestroom,
                                contentDescription = "Family",
                                tint = SafeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NAGPUR SURAKSHA",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF111111)
                            )
                            Text(
                                text = "Parent Emergency Live Tracker",
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFE8F0FE), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Secure", tint = Color(0xFF1967D2), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SECURE LINK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1967D2))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // User Status Alert Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSosActive) Color(0xFFB71C1C) else Color(0xFF1B5E20)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("parent_tracking_status_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSosActive) "🔴 SOS IS ACTIVE" else "🟢 USER IS SAFE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Battery: ${activeSos.location.battery}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Citizen: $userName",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "📍 ${activeSos.location.address}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Dynamic Map
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LIVE NAGPUR MAP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PoliceNavy
                    )
                    val timeNow = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(activeSos.location.timestamp))
                    Text(
                        text = "Updated: $timeNow",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TacticalMapCanvas(
                    userLat = activeSos.location.latitude,
                    userLng = activeSos.location.longitude,
                    accuracy = activeSos.location.accuracy,
                    speed = activeSos.location.speed,
                    isSosActive = isSosActive,
                    breadcrumbs = breadcrumbs,
                    safePlaces = safePlaces,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    isDarkTheme = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Telemetry Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TelemetryItem(
                        icon = Icons.Default.Speed,
                        label = "Speed",
                        value = "${String.format("%.1f", activeSos.location.speed)} km/h"
                    )
                    TelemetryItem(
                        icon = Icons.Default.DirectionsWalk,
                        label = "Movement",
                        value = if (activeSos.location.speed > 3f) "Moving" else "Stationary"
                    )
                    TelemetryItem(
                        icon = Icons.Default.LocationOn,
                        label = "Accuracy",
                        value = "±${activeSos.location.accuracy.toInt()}m"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct Action Buttons for Parent
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$userPhone"))
                    context.startActivity(callIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call $userName", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                    context.startActivity(callIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PoliceNavy),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Default.LocalPolice, contentDescription = "Police", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Police (112)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Text("← Back to Main App", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TelemetryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = PoliceNavy, modifier = Modifier.size(20.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111111))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}
