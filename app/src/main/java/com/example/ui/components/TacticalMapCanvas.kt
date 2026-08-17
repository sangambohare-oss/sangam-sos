package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.IncidentEntity
import com.example.data.local.entity.LocationBreadcrumbEntity
import com.example.data.local.entity.SafePlaceEntity
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedGlow
import com.example.ui.theme.PoliceNavy
import com.example.ui.theme.SafeGreen

data class MapLandmark(
    val name: String,
    val lat: Double,
    val lng: Double,
    val type: String
)

@Composable
fun TacticalMapCanvas(
    userLat: Double,
    userLng: Double,
    accuracy: Float = 8f,
    speed: Float = 0f,
    isSosActive: Boolean = false,
    breadcrumbs: List<LocationBreadcrumbEntity> = emptyList(),
    safePlaces: List<SafePlaceEntity> = emptyList(),
    incidents: List<IncidentEntity> = emptyList(),
    selectedIncidentCode: String? = null,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Center of Nagpur for projection
    val centerLat = 21.1458
    val centerLng = 79.0882
    val latSpan = 0.12 // ~13km north-south
    val lngSpan = 0.12 // ~13km east-west

    // Static key Nagpur landmarks
    val landmarks = remember {
        listOf(
            MapLandmark("Sitabuldi Fort", 21.1460, 79.0870, "HERITAGE"),
            MapLandmark("Dharampeth", 21.1415, 79.0620, "ZONE"),
            MapLandmark("Civil Lines", 21.1510, 79.0730, "GOV"),
            MapLandmark("Sadar Bazaar", 21.1610, 79.0805, "MARKET"),
            MapLandmark("Ambazari Lake", 21.1250, 79.0480, "LAKE"),
            MapLandmark("GMC Medical", 21.1350, 79.0980, "HOSPITAL"),
            MapLandmark("Mankapur", 21.1890, 79.0840, "ZONE"),
            MapLandmark("Wardha Rd / Khamla", 21.1180, 79.0620, "ZONE")
        )
    }

    Box(
        modifier = modifier
            .background(if (isDarkTheme) Color(0xFF10141D) else Color(0xFFF0F4F8))
            .border(1.dp, if (isDarkTheme) Color(0xFF2A3447) else Color(0xFFD0DBE5), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            fun project(lat: Double, lng: Double): Offset {
                val x = ((lng - (centerLng - lngSpan / 2)) / lngSpan * width).toFloat()
                // Invert Y because latitude goes upwards (north)
                val y = (((centerLat + latSpan / 2) - lat) / latSpan * height).toFloat()
                return Offset(x, y)
            }

            // 1. Draw Grid Lines (Tactical Grid)
            val gridColor = if (isDarkTheme) Color(0x1F4A6572) else Color(0x221E88E5)
            val gridStep = 40f
            var gx = 0f
            while (gx < width) {
                drawLine(gridColor, Offset(gx, 0f), Offset(gx, height), strokeWidth = 1f)
                gx += gridStep
            }
            var gy = 0f
            while (gy < height) {
                drawLine(gridColor, Offset(0f, gy), Offset(width, gy), strokeWidth = 1f)
                gy += gridStep
            }

            // 2. Draw Simulated Major Nagpur Roads (Ring Road, Wardha Rd, Amravati Rd, Central Ave)
            val roadColor = if (isDarkTheme) Color(0x33607D8B) else Color(0x4490A4AE)
            val roadStroke = Stroke(width = 3f)

            // Wardha Road (South to North)
            val pWardhaS = project(21.0900, 79.0650)
            val pSitabuldi = project(21.1448, 79.0835)
            val pSadar = project(21.1650, 79.0810)
            drawLine(roadColor, pWardhaS, pSitabuldi, strokeWidth = 5f)
            drawLine(roadColor, pSitabuldi, pSadar, strokeWidth = 5f)

            // Amravati Road (West to Center)
            val pWadi = project(21.1450, 79.0100)
            drawLine(roadColor, pWadi, pSitabuldi, strokeWidth = 5f)

            // Central Avenue (Center to East)
            val pEastNagpur = project(21.1480, 79.1400)
            drawLine(roadColor, pSitabuldi, pEastNagpur, strokeWidth = 5f)

            // Ring Road Arc
            val ringPoints = listOf(
                project(21.1200, 79.0400),
                project(21.1100, 79.0800),
                project(21.1300, 79.1200),
                project(21.1700, 79.1100),
                project(21.1800, 79.0600),
                project(21.1500, 79.0300)
            )
            val ringPath = Path().apply {
                moveTo(ringPoints[0].x, ringPoints[0].y)
                for (i in 1 until ringPoints.size) {
                    lineTo(ringPoints[i].x, ringPoints[i].y)
                }
                close()
            }
            drawPath(
                ringPath,
                roadColor,
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )

            // 3. Draw Water Bodies (Ambazari Lake, Futala Lake)
            val waterColor = if (isDarkTheme) Color(0x330288D1) else Color(0x4429B6F6)
            val pAmbazari = project(21.1250, 79.0480)
            drawCircle(waterColor, radius = 26f, center = pAmbazari)
            val pFutala = project(21.1520, 79.0450)
            drawCircle(waterColor, radius = 18f, center = pFutala)

            // 4. Draw Safe Places (Police Stations, Hospitals, Fire Stations)
            safePlaces.forEach { place ->
                val p = project(place.latitude, place.longitude)
                val color = when (place.category) {
                    "POLICE" -> PoliceNavy
                    "HOSPITAL" -> SafeGreen
                    "FIRE" -> AlertAmber
                    else -> Color(0xFF8E24AA)
                }
                // Marker background
                drawCircle(color, radius = 8f, center = p)
                drawCircle(Color.White, radius = 4f, center = p)
            }

            // 5. Draw Breadcrumbs path if present
            if (breadcrumbs.size > 1) {
                val breadcrumbPath = Path()
                val first = project(breadcrumbs.first().latitude, breadcrumbs.first().longitude)
                breadcrumbPath.moveTo(first.x, first.y)

                for (i in 1 until breadcrumbs.size) {
                    val pt = project(breadcrumbs[i].latitude, breadcrumbs[i].longitude)
                    breadcrumbPath.lineTo(pt.x, pt.y)
                }
                drawPath(
                    breadcrumbPath,
                    color = EmergencyRedGlow,
                    style = Stroke(width = 4f)
                )

                // Draw dots on trail
                breadcrumbs.forEach { bc ->
                    val pt = project(bc.latitude, bc.longitude)
                    drawCircle(EmergencyRedDark, radius = 4f, center = pt)
                }
            }

            // 6. Draw other Police Incidents (if viewed on Police Dashboard)
            incidents.forEach { incident ->
                if (incident.status != "RESOLVED" && incident.incidentCode != selectedIncidentCode) {
                    val p = project(incident.currentLatitude, incident.currentLongitude)
                    val incColor = when (incident.status) {
                        "ACTIVE" -> EmergencyRed
                        "RESPONDING" -> AlertAmber
                        "ACKNOWLEDGED" -> Color(0xFFFDD835)
                        else -> SafeGreen
                    }
                    drawCircle(incColor.copy(alpha = 0.3f), radius = 18f, center = p)
                    drawCircle(incColor, radius = 9f, center = p)
                    drawCircle(Color.White, radius = 4f, center = p)
                }
            }

            // 7. Draw Active User Location with pulsing beacon
            val userPos = project(userLat, userLng)

            if (isSosActive) {
                // Outer Pulse Ring
                drawCircle(
                    color = EmergencyRed.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = userPos
                )
                // Middle Glow
                drawCircle(
                    color = EmergencyRed.copy(alpha = 0.4f),
                    radius = 24f,
                    center = userPos
                )
                // Core Pin
                drawCircle(
                    color = EmergencyRed,
                    radius = 12f,
                    center = userPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = userPos
                )
            } else {
                // Standard Blue user dot
                drawCircle(
                    color = Color(0x332196F3),
                    radius = 20f,
                    center = userPos
                )
                drawCircle(
                    color = Color(0xFF1976D2),
                    radius = 10f,
                    center = userPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = userPos
                )
            }
        }

        // Overlay status pill in top corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .background(
                    if (isDarkTheme) Color(0xDD1A2130) else Color(0xDDFFFFFF),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isSosActive) "🚨 LIVE EMERGENCY TRACKING — NAGPUR" else "📍 NAGPUR TACTICAL MAP",
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (isSosActive) EmergencyRed else if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF1565C0)
            )
        }

        // Bottom stats overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(
                    if (isDarkTheme) Color(0xDD1A2130) else Color(0xDDFFFFFF),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "GPS: ${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)} | Acc: ±${accuracy.toInt()}m",
                fontSize = 10.sp,
                color = if (isDarkTheme) Color.LightGray else Color.DarkGray
            )
        }
    }
}
private val EmergencyRedDark = Color(0xFFB71C1C)
