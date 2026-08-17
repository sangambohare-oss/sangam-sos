package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RoleSelectionScreen(
    viewModel: MainViewModel,
    onRoleSelected: (UserRole) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val googleUser by viewModel.googleUser.collectAsState()
    val context = LocalContext.current
    var selectedRole by remember {
        mutableStateOf<UserRole?>(
            if (googleUser.role != UserRole.UNASSIGNED) googleUser.role else null
        )
    }

    val isRoleAlreadyFixed = googleUser.role != UserRole.UNASSIGNED

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Authenticated User Banner (28dp radius)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth().testTag("card_logged_in_user")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        Text(
                            text = googleUser.displayName.take(2).uppercase().ifBlank { "GA" },
                            color = PureBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = googleUser.displayName.ifBlank { "Google User" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF262626),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "Google Account",
                                    fontSize = 9.sp,
                                    color = NeonLime,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = googleUser.email.ifBlank { "user@gmail.com" },
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.signOutGoogle()
                        onSignOut()
                    },
                    modifier = Modifier.testTag("btn_logout_role_screen")
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isRoleAlreadyFixed) "ACCOUNT ROLE ASSIGNED" else "SELECT YOUR ROLE",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            letterSpacing = (-0.8).sp
        )

        Text(
            text = if (isRoleAlreadyFixed)
                "This Google Account is registered as ${googleUser.role.name} in Firestore."
            else
                "Choose whether this device acts as Parent (Guardian) or Child (Protected Ward).",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Role Option 1: PARENT / GUARDIAN (28dp radius)
        val isParentChosen = selectedRole == UserRole.PARENT

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (isParentChosen) Color(0xFF222222) else DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isParentChosen) 2.dp else 1.dp,
                color = if (isParentChosen) NeonLime else DarkCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isRoleAlreadyFixed) { selectedRole = UserRole.PARENT }
                .testTag("card_role_parent")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isParentChosen) NeonLime else Color(0xFF262626), RoundedCornerShape(999.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FamilyRestroom,
                                contentDescription = "Parent Role",
                                tint = if (isParentChosen) PureBlack else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Parent / Guardian",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp,
                                color = Color.White
                            )
                            Text(
                                text = "Family Guardian Mode",
                                fontSize = 12.sp,
                                color = if (isParentChosen) NeonLime else TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isParentChosen) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(NeonLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = PureBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "• Generates a 6-character Pairing Code in Firestore for child devices\n" +
                            "• Live GPS location tracking & Nagpur tactical map\n" +
                            "• Real-time automatic sync of child's emergency contacts & safe places\n" +
                            "• Instant SOS notifications and safe check-in pings",
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF191919),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "🛡️ Designed for parents, guardians, and family protectors",
                        fontSize = 11.sp,
                        color = NeonLime,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Option 2: CHILD / WARD (28dp radius)
        val isChildChosen = selectedRole == UserRole.CHILD

        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (isChildChosen) Color(0xFF222222) else DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isChildChosen) 2.dp else 1.dp,
                color = if (isChildChosen) SecondaryLime else DarkCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isRoleAlreadyFixed) { selectedRole = UserRole.CHILD }
                .testTag("card_role_child")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isChildChosen) SecondaryLime else Color(0xFF262626), RoundedCornerShape(999.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChildCare,
                                contentDescription = "Child Role",
                                tint = if (isChildChosen) PureBlack else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Child / Ward",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp,
                                color = Color.White
                            )
                            Text(
                                text = "Protected Ward Mode",
                                fontSize = 12.sp,
                                color = if (isChildChosen) SecondaryLime else TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isChildChosen) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(SecondaryLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = PureBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "• Enters the 6-character Pairing Code provided by the parent\n" +
                            "• One-touch Big Red SOS & 3x Power Button emergency trigger\n" +
                            "• Broadcasts live GPS coordinates directly to paired parent device\n" +
                            "• Automatic WhatsApp alert simulation to emergency contacts",
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF191919),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "📱 Designed for students, children, and protected family members",
                        fontSize = 11.sp,
                        color = SecondaryLime,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Selection Button (Neon Lime Pill)
        Button(
            onClick = {
                selectedRole?.let { role ->
                    viewModel.setUserRole(role)
                    Toast.makeText(context, "Role confirmed as ${role.name}", Toast.LENGTH_SHORT).show()
                    onRoleSelected(role)
                }
            },
            enabled = selectedRole != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = PureBlack,
                disabledContainerColor = Color(0xFF2E2E2E),
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_confirm_role")
        ) {
            Text(
                text = if (isRoleAlreadyFixed) "Continue to ${googleUser.role.name} Dashboard" else "Confirm & Save Role to Firestore",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedRole != null) PureBlack else TextMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
