package com.example.ui.screens

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
import com.example.data.model.*
import com.example.ui.components.CallClinicButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyAppointmentsScreen(
    appointments: List<Appointment>,
    language: Language,
    textScale: TextScale,
    onTrackQueue: (token: String, doctorId: String) -> Unit,
    onCancelAppointment: (appointmentId: String) -> Unit,
    onFindDoctorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = Strings.get("my_appointments", language),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy,
                    fontSize = (20 * textScale.scale).sp
                )
            )
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = ClinicTeal, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Strings.get("no_upcoming_appointment", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ClinicNavy)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onFindDoctorClick,
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                        ) {
                            Text(Strings.get("book_appointment", language), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(appointments) { app ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("appointment_card_${app.bookingRef}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Strings.get("your_token", language),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                                )
                                Text(
                                    text = app.tokenNumber,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClinicTeal
                                    )
                                )
                            }
                            StatusBadge(status = app.status, language = language, textScale = textScale)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = ClinicDivider)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = app.doctorName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (16 * textScale.scale).sp
                            )
                        )
                        Text(
                            text = "${app.doctorSpeciality} • ${app.clinicName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                        )
                        Text(
                            text = "${if (language == Language.HINDI) "तारीख व समय" else "Date & Time"}: ${app.date} at ${app.time}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ClinicTealDark
                            )
                        )
                        Text(
                            text = "Patient: ${app.patientName} (${app.patientAge}y) • Ref: ${app.bookingRef}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (app.status != QueueStatus.COMPLETED && app.status != QueueStatus.CANCELLED) {
                                Button(
                                    onClick = { onTrackQueue(app.tokenNumber, app.doctorId) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                                    modifier = Modifier.weight(1.2f).defaultMinSize(minHeight = 44.dp)
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = Strings.get("track_queue", language), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { appointmentToCancel = app },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                ) {
                                    Text(text = Strings.get("cancel_appointment", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (appointmentToCancel != null) {
        val target = appointmentToCancel!!
        AlertDialog(
            onDismissRequest = { appointmentToCancel = null },
            title = { Text(Strings.get("cancel_appointment", language), fontWeight = FontWeight.Bold, color = ClinicNavy) },
            text = {
                Text(
                    text = "${Strings.get("cancel_confirm", language)}\n\n${target.doctorName} (${target.date} at ${target.time}). Token: ${target.tokenNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelAppointment(target.id)
                        appointmentToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text(Strings.get("yes_cancel", language), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToCancel = null }) {
                    Text(Strings.get("dismiss", language))
                }
            }
        )
    }
}

@Composable
fun FamilyMembersScreen(
    members: List<FamilyMember>,
    language: Language,
    textScale: TextScale,
    onAddMember: (name: String, relation: String, age: Int, gender: String, phone: String) -> Unit,
    onRemoveMember: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var relationInput by remember { mutableStateOf("Mother") }
    var ageInput by remember { mutableStateOf("68") }
    var genderInput by remember { mutableStateOf("Female") }
    var phoneInput by remember { mutableStateOf("+91 98450 99880") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("family_members", language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (20 * textScale.scale).sp
                    )
                )

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                    modifier = Modifier.testTag("add_family_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (language == Language.HINDI) "सदस्य जोड़ें" else "Add Member", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = if (language == Language.HINDI) "अपने परिवार के बुजुर्गों और बच्चों के लिए आसानी से अपॉइंटमेंट बुक करें।" else "Manage profiles for parents and family to book doctor visits easily on their behalf.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )
        }

        items(members) { fm ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
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
                                .clip(CircleShape)
                                .background(ClinicTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = ClinicTealDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = fm.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ClinicNavy)
                            )
                            Text(
                                text = "${if (language == Language.HINDI) fm.relationHindi else fm.relation} • ${fm.age} yrs • ${fm.gender}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                            )
                            Text(
                                text = fm.phone,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                            )
                        }
                    }

                    IconButton(onClick = { onRemoveMember(fm.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusError)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (language == Language.HINDI) "परिवार के नए सदस्य को जोड़ें" else "Add Family Member", fontWeight = FontWeight.Bold, color = ClinicNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("family_name_input")
                    )
                    OutlinedTextField(
                        value = relationInput,
                        onValueChange = { relationInput = it },
                        label = { Text("Relationship (e.g. Mother, Father, Son)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Age (years)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onAddMember(
                                nameInput,
                                relationInput,
                                ageInput.toIntOrNull() ?: 50,
                                genderInput,
                                phoneInput
                            )
                            nameInput = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                ) {
                    Text("Save / सहेजें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    language: Language,
    textScale: TextScale,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = Strings.get("notifications", language),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy,
                    fontSize = (20 * textScale.scale).sp
                )
            )
        }

        if (notifications.isEmpty()) {
            item {
                Text(
                    text = if (language == Language.HINDI) "कोई नई सूचना नहीं है।" else "No notifications.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                )
            }
        } else {
            items(notifications) { notif ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("notif_item_${notif.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    when (notif.type) {
                                        "DELAY" -> StatusWarningContainer
                                        "BOOKING" -> StatusSuccessContainer
                                        else -> ClinicBlueContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (notif.type) {
                                    "DELAY" -> Icons.Default.AccessTimeFilled
                                    "BOOKING" -> Icons.Default.EventAvailable
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = when (notif.type) {
                                    "DELAY" -> StatusWarning
                                    "BOOKING" -> StatusSuccess
                                    else -> ClinicBlue
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == Language.HINDI) notif.titleHindi else notif.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ClinicNavy,
                                        fontSize = (14 * textScale.scale).sp
                                    )
                                )
                                Text(
                                    text = dateFormat.format(Date(notif.timestamp)),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == Language.HINDI) notif.messageHindi else notif.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF475569),
                                    fontSize = (12 * textScale.scale).sp,
                                    lineHeight = (16 * textScale.scale).sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
