package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.SafeGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HardwareDiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val powerClickCount by viewModel.powerClickCount.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val context = LocalContext.current

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    } else true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Hardware & Service Diagnostics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111111)
        )
        Text(
            text = "Technical transparency regarding Android OS hardware trigger mechanisms and foreground tracking.",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // 3x Power Click Test Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFF3E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Power Button 3x Trigger",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                    }

                    Surface(
                        color = if (powerClickCount > 0) AlertAmber else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Clicks: $powerClickCount/3",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (powerClickCount > 0) Color.Black else SafeGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Android OS Security Architecture Note: On standard non-rooted Android devices, apps cannot intercept KEYCODE_POWER globally. Nagpur Suraksha uses a Foreground Service with an Intent.ACTION_SCREEN_ON / OFF broadcast receiver to detect 3 quick state toggles when the physical power key is pressed.",
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.simulatePowerButtonClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp).testTag("btn_diag_power_click")
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = "Simulate", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Click ($powerClickCount/3)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetPowerClickCount() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GPS Telemetry Sensor Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE3F2FD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = "GPS", tint = PoliceNavy, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GPS & Location Service",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                DiagRow("Latitude", String.format("%.6f", currentLocation.latitude))
                DiagRow("Longitude", String.format("%.6f", currentLocation.longitude))
                DiagRow("Estimated Accuracy", "±${currentLocation.accuracy.toInt()} meters")
                DiagRow("Current Speed", "${String.format("%.1f", currentLocation.speed)} km/h")
                DiagRow("Battery Level", "${currentLocation.battery}%")
                DiagRow("Resolved Nagpur Area", currentLocation.address)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // OS Permission & Battery Optimization
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SYSTEM PERMISSIONS & SERVICES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PoliceNavy,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                DiagStatusRow("ACCESS_FINE_LOCATION", true)
                DiagStatusRow("FOREGROUND_SERVICE_LOCATION", true)
                DiagStatusRow("POST_NOTIFICATIONS", true)
                DiagStatusRow("VIBRATE", true)
                DiagStatusRow("WAKE_LOCK (Emergency lock-screen wake)", true)
                DiagStatusRow("Battery Optimization Exemption", isIgnoringBatteryOptimizations)
            }
        }
    }
}

@Composable
fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF666666))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111111))
    }
}

@Composable
fun DiagStatusRow(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF333333), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        Surface(
            color = if (isOk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = if (isOk) "GRANTED" else "RESTRICTED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOk) SafeGreen else EmergencyRed,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
