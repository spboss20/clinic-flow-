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
import com.example.data.model.Language
import com.example.data.model.QueueEntry
import com.example.data.model.QueueStatus
import com.example.data.model.TextScale
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun DoctorDashboardScreen(
    doctorName: String,
    speciality: String,
    clinicName: String,
    queueList: List<QueueEntry>,
    delayMinutes: Int,
    isQueuePaused: Boolean,
    language: Language,
    textScale: TextScale,
    onCallNextPatient: () -> Unit,
    onStartConsultation: (queueEntryId: String) -> Unit,
    onCompleteConsultation: (queueEntryId: String) -> Unit,
    onMarkNoShow: (queueEntryId: String) -> Unit,
    onAnnounceDelay: (minutes: Int, reason: String) -> Unit,
    onTogglePauseQueue: () -> Unit,
    onAskAiClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDelayDialog by remember { mutableStateOf(false) }
    var delayMinutesInput by remember { mutableStateOf("15") }
    var delayReasonInput by remember { mutableStateOf("Emergency patient case") }

    val activeConsultation = remember(queueList) {
        queueList.find { it.status == QueueStatus.IN_CONSULTATION }
    }
    val nextInLine = remember(queueList) {
        queueList.firstOrNull { it.status == QueueStatus.WAITING || it.status == QueueStatus.CHECKED_IN }
    }

    val completedCount = queueList.count { it.status == QueueStatus.COMPLETED }
    val waitingCount = queueList.count { it.status == QueueStatus.WAITING || it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.BOOKED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Doctor Header & Status
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicNavy),
                modifier = Modifier.fillMaxWidth().testTag("doctor_dashboard_header")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = doctorName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = (18 * textScale.scale).sp
                                )
                            )
                            Text(
                                text = "$speciality • $clinicName",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0CBE5))
                            )
                        }

                        if (isQueuePaused) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusWarningContainer
                            ) {
                                Text(
                                    text = Strings.get("queue_paused", language),
                                    color = StatusWarning,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = (11 * textScale.scale).sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF1E3A5F))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Today", color = Color(0xFFB0CBE5), fontSize = 11.sp)
                            Text(text = queueList.size.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "In Waiting", color = Color(0xFFB0CBE5), fontSize = 11.sp)
                            Text(text = waitingCount.toString(), color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Completed", color = Color(0xFFB0CBE5), fontSize = 11.sp)
                            Text(text = completedCount.toString(), color = Color(0xFF4DD0E1), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Active Consultation / Next Patient Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("active_consultation_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (activeConsultation != null) {
                            if (language == Language.HINDI) "वर्तमान में परामर्श जारी (In Consultation)" else "Current Consultation in Progress"
                        } else {
                            if (language == Language.HINDI) "परामर्श कक्ष (Consultation Room Ready)" else "Consultation Room Ready"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicNavy,
                            fontSize = (16 * textScale.scale).sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = ClinicDivider)
                    Spacer(modifier = Modifier.height(10.dp))

                    val displayPatient = activeConsultation ?: nextInLine
                    if (displayPatient != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = displayPatient.patientName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ClinicNavy,
                                        fontSize = (16 * textScale.scale).sp
                                    )
                                )
                                Text(
                                    text = "Age: ${displayPatient.patientAge}y • ${displayPatient.slotTime}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = displayPatient.tokenNumber,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClinicTeal
                                    )
                                )
                                StatusBadge(status = displayPatient.status, language = language, textScale = textScale)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls: Call Next, Start, Complete, No Show
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (activeConsultation == null) {
                                Button(
                                    onClick = { onStartConsultation(displayPatient.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("start_consultation_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Strings.get("start_consultation", language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (13 * textScale.scale).sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onCompleteConsultation(activeConsultation.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("complete_consultation_button")
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Strings.get("complete_consultation", language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (13 * textScale.scale).sp
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { onMarkNoShow(displayPatient.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("mark_no_show_button")
                            ) {
                                Text(
                                    text = Strings.get("mark_no_show", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (12 * textScale.scale).sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (language == Language.HINDI) "कतार में अभी कोई मरीज प्रतीक्षारत नहीं है。" else "No patients waiting in queue right now.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            }
        }

        // Operational Actions: Call Next, Announce Delay, Pause
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallNextPatient,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicBlue),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("doctor_call_next_button")
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.get("call_next", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = (13 * textScale.scale).sp
                    )
                }

                FilledTonalButton(
                    onClick = { showDelayDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("announce_delay_button")
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.get("announce_delay", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = (13 * textScale.scale).sp
                    )
                }

                OutlinedButton(
                    onClick = onTogglePauseQueue,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("pause_queue_button")
                ) {
                    Icon(
                        imageVector = if (isQueuePaused) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                        contentDescription = "Pause or Resume"
                    )
                }
            }
        }

        // ClinicFlow AI Doctor Copilot Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, ClinicTeal.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doctor_copilot_card")
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
                            contentDescription = "AI Copilot",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == Language.HINDI) "क्लिनिकफ्लो डॉक्टर को-पायलट" else "ClinicFlow Doctor Copilot",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                        Text(
                            text = if (language == Language.HINDI) "शेड्यूल सारांश, रिक्त स्लॉट व देरी की पूर्व-सूचना का मसौदा" else "Summarise schedule, find open slots & draft delay notice",
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
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp).testTag("open_doctor_copilot_button")
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

        // Today's Unified Queue List
        item {
            Text(
                text = Strings.get("today_appointments", language),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy,
                    fontSize = (16 * textScale.scale).sp
                )
            )
        }

        items(queueList) { entry ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().testTag("queue_entry_${entry.tokenNumber}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.tokenNumber,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ClinicTeal
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = entry.patientName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ClinicNavy
                                )
                            )
                            Text(
                                text = "${entry.slotTime} • ${if (entry.isWalkIn) "Walk-in" else "Online"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                            )
                        }
                    }

                    StatusBadge(status = entry.status, language = language, textScale = textScale)
                }
            }
        }
    }

    // Announce Delay Dialog
    if (showDelayDialog) {
        AlertDialog(
            onDismissRequest = { showDelayDialog = false },
            title = {
                Text(
                    text = Strings.get("announce_delay", language),
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == Language.HINDI) "प्रतीक्षारत सभी मरीजों को देरी की सूचना और अनुमानित समय भेजा जाएगा।" else "Broadcasts adjusted wait times to all currently waiting patients.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                    OutlinedTextField(
                        value = delayMinutesInput,
                        onValueChange = { delayMinutesInput = it },
                        label = { Text(Strings.get("delay_minutes", language)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = delayReasonInput,
                        onValueChange = { delayReasonInput = it },
                        label = { Text("Reason / विवरण") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = delayMinutesInput.toIntOrNull() ?: 15
                        onAnnounceDelay(mins, delayReasonInput)
                        showDelayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal)
                ) {
                    Text("Broadcast / भेजें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelayDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
