package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Doctor
import com.example.data.model.Language
import com.example.data.model.QueueEntry
import com.example.data.model.QueueStatus
import com.example.data.model.TextScale
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun ReceptionistDashboardScreen(
    doctors: List<Doctor>,
    queueList: List<QueueEntry>,
    language: Language,
    textScale: TextScale,
    onRegisterWalkIn: (doctorId: String, clinicId: String, name: String, phone: String, age: Int) -> Unit,
    onCheckInPatient: (queueEntryId: String) -> Unit,
    onCallNextPatient: (doctorId: String) -> Unit,
    onMarkNoShow: (queueEntryId: String) -> Unit,
    onAskAiClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showWalkInDialog by remember { mutableStateOf(false) }

    var walkInName by remember { mutableStateOf("") }
    var walkInPhone by remember { mutableStateOf("") }
    var walkInAge by remember { mutableStateOf("45") }
    var selectedDoctorId by remember { mutableStateOf(doctors.firstOrNull()?.id ?: "") }

    val filteredQueue = remember(queueList, searchQuery) {
        if (searchQuery.isBlank()) queueList
        else queueList.filter {
            it.patientName.contains(searchQuery, ignoreCase = true) ||
                it.tokenNumber.contains(searchQuery, ignoreCase = true) ||
                it.bookingId.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Reception Desk Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicNavy),
                modifier = Modifier.fillMaxWidth().testTag("receptionist_header")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (language == Language.HINDI) "क्लिनिक रिसेप्शन डेस्क (Metro Care)" else "Clinic Reception Desk (Metro Care)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (18 * textScale.scale).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == Language.HINDI) "ऑनलाइन अपॉइंटमेंट चेक-इन और सीधे क्लिनिक आए (Walk-in) मरीजों का प्रबंधन" else "Unified Queue Management: Online Bookings & Walk-in Patients",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0CBE5))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showWalkInDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).testTag("open_walkin_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("walk_in_registration", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = (14 * textScale.scale).sp
                        )
                    }
                }
            }
        }

        // ClinicFlow AI Staff Copilot Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
                border = BorderStroke(1.dp, ClinicTeal.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receptionist_copilot_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ClinicTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Staff Copilot",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == Language.HINDI) "क्लिनिकफ्लो स्टाफ को-पायलट" else "ClinicFlow Staff Copilot",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                        Text(
                            text = if (language == Language.HINDI) "दैनिक शेड्यूल सारांश, खाली स्लॉट व कतार प्रबंधन सहायता" else "Schedule summary, open slots & queue operations helper",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ClinicTealDark,
                                fontSize = (12 * textScale.scale).sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAskAiClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp).testTag("open_staff_copilot_button")
                    ) {
                        Text(
                            text = if (language == Language.HINDI) "खोलें" else "Open",
                            fontSize = (12 * textScale.scale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Search Bar for Patient / Token
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search patient name, token (e.g. A006) or booking ref...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ClinicTeal) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClinicTeal,
                    unfocusedBorderColor = ClinicBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).testTag("reception_search_input")
            )
        }

        // Unified Queue Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("today_appointments", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (16 * textScale.scale).sp
                    )
                )
                Text(
                    text = "${filteredQueue.size} Total",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                )
            }
        }

        // Queue Items
        items(filteredQueue) { entry ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("reception_entry_${entry.tokenNumber}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.tokenNumber,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (entry.isWalkIn) ClinicBlue else ClinicTeal
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (entry.isWalkIn) ClinicBlueContainer else ClinicTealContainer
                            ) {
                                Text(
                                    text = if (entry.isWalkIn) "Walk-in" else "Online",
                                    color = if (entry.isWalkIn) ClinicBlue else ClinicTealDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        StatusBadge(status = entry.status, language = language, textScale = textScale)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${entry.patientName} (${entry.patientAge}y)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicNavy,
                            fontSize = (15 * textScale.scale).sp
                        )
                    )
                    Text(
                        text = "Doctor: ${entry.doctorName} • Slot: ${entry.slotTime}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                    )
                    Text(
                        text = "Phone: ${entry.patientPhone}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Receptionist Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (entry.status == QueueStatus.BOOKED) {
                            Button(
                                onClick = { onCheckInPatient(entry.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp).testTag("check_in_button_${entry.tokenNumber}")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.get("check_in", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (entry.status == QueueStatus.CHECKED_IN || entry.status == QueueStatus.WAITING) {
                            FilledTonalButton(
                                onClick = { onCallNextPatient(entry.doctorId) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp)
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = Strings.get("call_next", language), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (entry.status != QueueStatus.COMPLETED && entry.status != QueueStatus.CANCELLED && entry.status != QueueStatus.NO_SHOW) {
                            OutlinedButton(
                                onClick = { onMarkNoShow(entry.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                            ) {
                                Text(text = Strings.get("mark_no_show", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Register Walk-in Dialog
    if (showWalkInDialog) {
        AlertDialog(
            onDismissRequest = { showWalkInDialog = false },
            title = {
                Text(
                    text = Strings.get("walk_in_registration", language),
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == Language.HINDI) "क्लिनिक आए मरीज को तुरंत टोकन संख्या जारी करें:" else "Instantly assign token to a patient visiting the reception:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                    OutlinedTextField(
                        value = walkInName,
                        onValueChange = { walkInName = it },
                        label = { Text("Patient Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("walkin_name_input")
                    )
                    OutlinedTextField(
                        value = walkInPhone,
                        onValueChange = { walkInPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("walkin_phone_input")
                    )
                    OutlinedTextField(
                        value = walkInAge,
                        onValueChange = { walkInAge = it },
                        label = { Text("Age (years)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (walkInName.isNotBlank()) {
                            onRegisterWalkIn(
                                selectedDoctorId,
                                doctors.firstOrNull { it.id == selectedDoctorId }?.clinicId ?: "c1",
                                walkInName,
                                walkInPhone.ifBlank { "+91 98000 00000" },
                                walkInAge.toIntOrNull() ?: 45
                            )
                            walkInName = ""
                            walkInPhone = ""
                            showWalkInDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                    modifier = Modifier.testTag("submit_walkin_button")
                ) {
                    Text("Generate Token", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWalkInDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
