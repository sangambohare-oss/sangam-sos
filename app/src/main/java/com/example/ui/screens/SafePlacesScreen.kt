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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SafePlaceEntity
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.SafeGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SafePlacesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val safePlaces by viewModel.safePlaces.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }
    val context = LocalContext.current

    val filteredPlaces = remember(safePlaces, selectedCategory) {
        if (selectedCategory == "ALL") safePlaces else safePlaces.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Nagpur Safe Places",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111111)
        )
        Text(
            text = "Verified emergency assistance centers across Nagpur sorted by live distance.",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Filter Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == "ALL",
                onClick = { selectedCategory = "ALL" },
                label = { Text("All", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedCategory == "POLICE",
                onClick = { selectedCategory = "POLICE" },
                label = { Text("Police", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedCategory == "HOSPITAL",
                onClick = { selectedCategory = "HOSPITAL" },
                label = { Text("Hospitals", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedCategory == "WOMEN_HELP",
                onClick = { selectedCategory = "WOMEN_HELP" },
                label = { Text("Women Cell", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredPlaces) { place ->
                val dist = viewModel.calculateDistance(place.latitude, place.longitude)
                SafePlaceCard(
                    place = place,
                    distanceKm = dist,
                    onCall = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${place.phone}"))
                        context.startActivity(callIntent)
                    },
                    onDirections = {
                        val mapIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:${place.latitude},${place.longitude}?q=${Uri.encode(place.name)}")
                        )
                        context.startActivity(mapIntent)
                    }
                )
            }
        }
    }
}

@Composable
fun SafePlaceCard(
    place: SafePlaceEntity,
    distanceKm: Double,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    val (icon, color) = when (place.category) {
        "POLICE" -> Pair(Icons.Default.LocalPolice, PoliceNavy)
        "HOSPITAL" -> Pair(Icons.Default.LocalHospital, SafeGreen)
        "FIRE" -> Pair(Icons.Default.LocalFireDepartment, AlertAmber)
        else -> Pair(Icons.Default.Woman, Color(0xFF8E24AA))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("safe_place_${place.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = place.category, tint = color, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = place.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                        Text(
                            text = "${place.area} • ${String.format("%.1f", distanceKm)} km away",
                            fontSize = 11.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF1F3F4),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = place.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = place.address,
                fontSize = 11.sp,
                color = Color(0xFF777777)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call ${place.phone}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDirections,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = "Directions", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Directions", fontSize = 11.sp)
                }
            }
        }
    }
}
