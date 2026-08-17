package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

data class HelplineItem(
    val title: String,
    val number: String,
    val subtitle: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun EmergencyNumbersScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val helplines = listOf(
        HelplineItem("National Emergency & Police", "112", "All-India Emergency Response Support", EmergencyRed, Icons.Default.LocalPolice),
        HelplineItem("Ambulance / Medical Emergency", "108", "24x7 Free Ambulance Emergency Service", SafeGreen, Icons.Default.LocalHospital),
        HelplineItem("Fire Brigade", "101", "Nagpur Fire & Emergency Rescue", AlertAmber, Icons.Default.LocalFireDepartment),
        HelplineItem("Women Helpline (National)", "1091", "24x7 Safety & Distress Support", Color(0xFF8E24AA), Icons.Default.Woman),
        HelplineItem("Nagpur Police Bharosa Cell", "07122561212", "Special Cell for Women & Senior Citizens", PoliceNavy, Icons.Default.Security),
        HelplineItem("GMC Hospital Emergency Nagpur", "07122744100", "Government Medical College Trauma Center", Color(0xFF00897B), Icons.Default.LocalHospital),
        HelplineItem("National Cyber Crime Helpline", "1930", "Financial Fraud & Cyber Harassment", Color(0xFF3949AB), Icons.Default.PhoneInTalk),
        HelplineItem("Child Helpline", "1098", "Child in Distress / Protection", Color(0xFFFB8C00), Icons.Default.ChildCare)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Emergency Helplines",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111111)
        )
        Text(
            text = "One-tap direct dials to national and Nagpur municipal emergency response helplines.",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(helplines) { helpline ->
                HelplineCard(
                    item = helpline,
                    onCall = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${helpline.number}"))
                        context.startActivity(callIntent)
                    }
                )
            }
        }
    }
}

@Composable
fun HelplineCard(
    item: HelplineItem,
    onCall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("helpline_card_${item.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(item.color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = item.subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = item.color),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.number,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}
