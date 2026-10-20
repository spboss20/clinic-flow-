package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun BookingFlowScreen(
    doctor: Doctor,
    clinic: Clinic?,
    timeSlots: List<TimeSlot>,
    familyMembers: List<FamilyMember>,
    language: Language,
    textScale: TextScale,
    onConfirmBooking: (doctorId: String, clinicId: String, date: String, time: String, patientName: String, patientPhone: String, patientAge: Int) -> Result<Appointment>,
    onTrackQueueClick: (token: String, doctorId: String) -> Unit,
    onAskAiClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val tomorrowDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() + 86400000L))
    }

    var selectedDate by remember { mutableStateOf(todayDate) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedPatientType by remember { mutableStateOf("SELF") } // "SELF" or family member id
    var patientName by remember { mutableStateOf("Ramesh Sharma") }
    var patientAge by remember { mutableStateOf("67") }
    var patientPhone by remember { mutableStateOf("+91 98450 99887") }

    var bookingSuccessAppointment by remember { mutableStateOf<Appointment?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Filter available slots for doctor and selected date
    val availableSlots = remember(timeSlots, doctor.id, selectedDate) {
        timeSlots.filter { it.doctorId == doctor.id && it.date == selectedDate }
    }

    if (bookingSuccessAppointment != null) {
        val app = bookingSuccessAppointment!!
        // Confirmation Screen (Step 9)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ClinicBackground)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(StatusSuccessContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = StatusSuccess,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = Strings.get("booking_success", language),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy,
                    fontSize = (20 * textScale.scale).sp
                )
            )

            // Digital Clinic Pass / Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("confirmed_booking_pass")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ClinicTeal,
                                    fontSize = (32 * textScale.scale).sp
                                )
                            )
                        }
                        StatusBadge(status = app.status, language = language, textScale = textScale)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ClinicDivider)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${if (language == Language.HINDI) "बुकिंग संदर्भ" else "Booking Ref"}: ${app.bookingRef}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${if (language == Language.HINDI) "मरीज" else "Patient"}: ${app.patientName} (${app.patientAge}y)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicNavy,
                            fontSize = (15 * textScale.scale).sp
                        )
                    )

                    Text(
                        text = "${if (language == Language.HINDI) "डॉक्टर" else "Doctor"}: ${app.doctorName} (${app.doctorSpeciality})",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ClinicNavy)
                    )

                    Text(
                        text = "${if (language == Language.HINDI) "स्थान" else "Clinic"}: ${app.clinicName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ClinicSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (language == Language.HINDI) "तारीख" else "Date", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                            Text(text = app.date, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = if (language == Language.HINDI) "समय" else "Slot Time", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                            Text(text = app.time, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = Strings.get("pass_subtitle", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = (12 * textScale.scale).sp
                        )
                    )
                }
            }

            // CTAs: Track My Queue Now
            Button(
                onClick = {
                    onTrackQueueClick(app.tokenNumber, app.doctorId)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .testTag("track_queue_after_booking_button")
            ) {
                Icon(Icons.Default.HourglassTop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("track_queue", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textScale.scale).sp
                )
            }

            CallClinicButton(
                phoneNumber = app.clinicPhone,
                language = language,
                textScale = textScale,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    // Interactive Multi-step Booking Form
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Guidance Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
            border = BorderStroke(1.dp, ClinicTeal.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAskAiClick() }
                .testTag("booking_ask_ai_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = ClinicTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == Language.HINDI) "स्लॉट या प्रक्रिया समझने में मदद चाहिए?" else "Need help choosing a slot or doctor?",
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (13 * textScale.scale).sp
                    )
                    Text(
                        text = if (language == Language.HINDI) "क्लिनिकफ्लो एआई से पूछें (English/हिंदी)" else "Ask ClinicFlow AI for real-time guidance",
                        color = ClinicNavy.copy(alpha = 0.75f),
                        fontSize = (11 * textScale.scale).sp
                    )
                }
                TextButton(
                    onClick = onAskAiClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ask AI",
                        fontWeight = FontWeight.Bold,
                        color = ClinicTeal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Doctor Summary Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ClinicTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = ClinicTealDark)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ClinicNavy)
                    )
                    Text(
                        text = "${doctor.speciality} • ${doctor.clinicName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }
                Text(
                    text = "₹${doctor.consultationFee}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ClinicTeal)
                )
            }
        }

        // Step 1: Date Selection
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == Language.HINDI) "1. तारीख चुनें" else "1. Select Appointment Date",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (16 * textScale.scale).sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val dateOptions = listOf(
                        todayDate to (if (language == Language.HINDI) "आज (Today)" else "Today"),
                        tomorrowDate to (if (language == Language.HINDI) "कल (Tomorrow)" else "Tomorrow")
                    )

                    dateOptions.forEach { (dateVal, label) ->
                        val isSelected = selectedDate == dateVal
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ClinicTeal else ClinicSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) ClinicTeal else ClinicBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDate = dateVal
                                    selectedTime = null
                                }
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("select_date_$dateVal")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else ClinicNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (14 * textScale.scale).sp
                                )
                                Text(
                                    text = dateVal,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B),
                                    fontSize = (12 * textScale.scale).sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Slot Selection (Morning / Afternoon / Evening)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("select_slot", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (16 * textScale.scale).sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == Language.HINDI) "उपलब्ध स्लॉट में से एक चुनें (ग्रे रंग वाले स्लॉट पहले ही बुक हो चुके हैं):" else "Choose from available slots (grey slots are already taken):",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Render slots in a responsive wrap or grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chunked = availableSlots.chunked(3)
                    chunked.forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val isSelected = selectedTime == slot.time
                                val isBooked = slot.isBooked

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isBooked -> Color(0xFFE2E8F0)
                                        isSelected -> ClinicTeal
                                        else -> ClinicSurfaceVariant
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            isBooked -> Color(0xFFCBD5E1)
                                            isSelected -> ClinicTeal
                                            else -> ClinicBorder
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = !isBooked) {
                                            selectedTime = slot.time
                                            errorMessage = null
                                        }
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("slot_${slot.time.replace(" ", "_")}")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isBooked) "${slot.time}\n(Booked)" else slot.time,
                                            color = when {
                                                isBooked -> Color(0xFF94A3B8)
                                                isSelected -> Color.White
                                                else -> ClinicNavy
                                            },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = (12 * textScale.scale).sp
                                        )
                                    }
                                }
                            }
                            // Filler if row has less than 3
                            repeat(3 - rowSlots.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Patient Selection (Self or Family Member)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("select_patient", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (16 * textScale.scale).sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Option: Self
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPatientType = "SELF"
                            patientName = "Ramesh Sharma"
                            patientAge = "67"
                            patientPhone = "+91 98450 99887"
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPatientType == "SELF",
                        onClick = {
                            selectedPatientType = "SELF"
                            patientName = "Ramesh Sharma"
                            patientAge = "67"
                            patientPhone = "+91 98450 99887"
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${Strings.get("for_myself", language)} (Ramesh Sharma, 67y)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ClinicNavy,
                            fontSize = (14 * textScale.scale).sp
                        )
                    )
                }

                // Options: Family Members
                familyMembers.forEach { fm ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPatientType = fm.id
                                patientName = fm.name
                                patientAge = fm.age.toString()
                                patientPhone = fm.phone
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPatientType == fm.id,
                            onClick = {
                                selectedPatientType = fm.id
                                patientName = fm.name
                                patientAge = fm.age.toString()
                                patientPhone = fm.phone
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${fm.name} (${if (language == Language.HINDI) fm.relationHindi else fm.relation}, ${fm.age}y)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                    }
                }
            }
        }

        // Booking Summary & Cancellation Policy
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == Language.HINDI) "बुकिंग सारांश" else "Booking Summary & Fee",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• ${if (language == Language.HINDI) "डॉक्टर" else "Doctor"}: ${doctor.name}",
                    fontSize = (13 * textScale.scale).sp
                )
                Text(
                    text = "• ${if (language == Language.HINDI) "क्लिनिक" else "Clinic"}: ${doctor.clinicName}",
                    fontSize = (13 * textScale.scale).sp
                )
                Text(
                    text = "• ${if (language == Language.HINDI) "समय" else "Slot"}: $selectedDate, ${selectedTime ?: (if (language == Language.HINDI) "चयनित नहीं" else "Not chosen")}",
                    fontSize = (13 * textScale.scale).sp
                )
                Text(
                    text = "• ${if (language == Language.HINDI) "परामर्श शुल्क" else "Consultation Fee"}: ₹${doctor.consultationFee}",
                    fontWeight = FontWeight.Bold,
                    color = ClinicTealDark,
                    fontSize = (14 * textScale.scale).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == Language.HINDI) "रद्दीकरण नीति: तय समय से 2 घंटे पहले तक निःशुल्क रद्दीकरण।" else "Cancellation Policy: Free cancellation up to 2 hours prior to scheduled time.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569), fontSize = (11 * textScale.scale).sp)
                )
            }
        }

        // Error message if any
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = StatusError,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Confirm Button
        Button(
            onClick = {
                if (selectedTime == null) {
                    errorMessage = if (language == Language.HINDI) "कृपया आगे बढ़ने के लिए समय स्लॉट चुनें" else "Please select an available time slot first."
                    return@Button
                }
                isSubmitting = true
                val result = onConfirmBooking(
                    doctor.id,
                    doctor.clinicId,
                    selectedDate,
                    selectedTime!!,
                    patientName,
                    patientPhone,
                    patientAge.toIntOrNull() ?: 50
                )
                isSubmitting = false
                if (result.isSuccess) {
                    bookingSuccessAppointment = result.getOrThrow()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Booking failed. Please try another slot."
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .testTag("confirm_booking_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Strings.get("confirm_booking", language),
                fontWeight = FontWeight.Bold,
                fontSize = (16 * textScale.scale).sp
            )
        }
    }
}
