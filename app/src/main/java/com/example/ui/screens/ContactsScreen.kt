package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.EmergencyContactEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.NeonLime
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SecondaryLime
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ContactsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.emergencyContacts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, rel, phone, wa, priority ->
                viewModel.addContact(name, rel, phone, wa, priority)
                showAddDialog = false
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Emergency Contacts",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = Color.White
            )
            Text(
                text = "Contacts will receive WhatsApp alerts with live tracking links in priority order when SOS is triggered.",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            )

            if (contacts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = DarkCardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Contacts Added Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap the + button below to add your emergency contacts manually (Parents, Guardians, Friends).",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            onDelete = { viewModel.deleteContact(contact.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = NeonLime,
            contentColor = PureBlack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_contact")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = PureBlack)
        }
    }
}

@Composable
fun ContactCard(
    contact: EmergencyContactEntity,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = DarkCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth().testTag("contact_item_${contact.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        .background(NeonLime.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P${contact.priority}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = contact.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "${contact.relationship} • ${contact.phone}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    if (contact.whatsappNumber.isNotEmpty()) {
                        Text(
                            text = "WhatsApp: ${contact.whatsappNumber}",
                            fontSize = 11.sp,
                            color = NeonLime
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var priorityText by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = {
        focusManager.clearFocus()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Emergency Contact",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g. Mother, Father, Friend)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { priorityText = it },
                    label = { Text("Priority Order (1, 2, 3...)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        onDismiss()
                    }) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val prio = priorityText.toIntOrNull() ?: 1
                            val waNum = if (whatsapp.isEmpty()) phone else whatsapp
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onSave(name, relationship, phone, waNum, prio)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = PureBlack),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Save Contact", fontWeight = FontWeight.SemiBold, color = PureBlack)
                    }
                }
            }
        }
    }
}
