package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppTeal
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MockWhatsAppScreen(
    viewModel: MainViewModel,
    onOpenLiveTracking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val whatsAppInbox by viewModel.whatsAppInbox.collectAsState()
    val activeSos by viewModel.activeSos.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFEAE2))
    ) {
        // WhatsApp Chat Header
        Surface(
            color = WhatsAppTeal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Nagpur Suraksha",
                        tint = WhatsAppTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nagpur Suraksha Alert Bot",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Official Emergency Broadcast • Verified",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // WhatsApp Chat Background & Messages
        if (whatsAppInbox.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No emergency notifications yet.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When an SOS is activated, automated WhatsApp alerts with live tracking links will appear here in real-time.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(whatsAppInbox) { message ->
                    WhatsAppMessageBubble(
                        message = message,
                        onOpenLink = { url ->
                            val token = url.substringAfterLast("/")
                            onOpenLiveTracking(token)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsAppMessageBubble(
    message: com.example.data.repository.WhatsAppMessageLog,
    onOpenLink: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCF8C6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("whatsapp_message_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header inside message
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "To: ${message.recipientName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF075E54)
                    )
                    Text(
                        text = message.formattedTime,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.messageText,
                    fontSize = 13.sp,
                    color = Color(0xFF111111),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Clickable Live Tracking Button
                Surface(
                    color = WhatsAppGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLink(message.trackingUrl) }
                        .testTag("track_live_location_whatsapp_link")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🔴 OPEN LIVE GPS TRACKING",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delivered",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "Delivered",
                        tint = Color(0xFF34B7F1),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
