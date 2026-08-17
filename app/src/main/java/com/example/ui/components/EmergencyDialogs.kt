package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.SafeGreen

@Composable
fun EmergencyCountdownDialog(
    secondsRemaining: Int,
    onCancel: () -> Unit,
    onTriggerNow: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(secondsRemaining) {
        scale.snapTo(1.3f)
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Dialog(
        onDismissRequest = { /* Cannot dismiss without clicking Cancel button */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A0D)),
            border = androidx.compose.foundation.BorderStroke(2.dp, EmergencyRed),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("countdown_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = EmergencyRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SOS ACTIVATING",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = EmergencyRed,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sending GPS location to Nagpur Police & emergency contacts in:",
                    fontSize = 14.sp,
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Big Countdown Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(scale.value)
                        .background(EmergencyRed.copy(alpha = 0.2f), CircleShape)
                        .border(4.dp, EmergencyRed, CircleShape)
                ) {
                    Text(
                        text = "$secondsRemaining",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("cancel_countdown_button")
                ) {
                    Text(
                        text = "CANCEL (I AM SAFE)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRedDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onTriggerNow,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("trigger_immediately_button")
                ) {
                    Text(
                        text = "ACTIVATE IMMEDIATELY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SafeCancelConfirmationDialog(
    onConfirmCancel: () -> Unit,
    onKeepActive: () -> Unit
) {
    Dialog(
        onDismissRequest = onKeepActive,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF424242)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("safe_cancel_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Safe Shield",
                    tint = SafeGreen,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Are you sure you are safe?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Cancelling will notify Nagpur Police and your parents that you are now safe and stop live GPS sharing.",
                    fontSize = 14.sp,
                    color = Color(0xFFBDBDBD),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirmCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_safe_button")
                ) {
                    Text(
                        text = "YES, I AM SAFE (CANCEL SOS)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onKeepActive,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("keep_sos_active_button")
                ) {
                    Text(
                        text = "KEEP SOS ACTIVE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
