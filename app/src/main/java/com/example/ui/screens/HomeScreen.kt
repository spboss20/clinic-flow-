package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.Language
import com.example.data.model.QueueStatus
import com.example.data.model.TextScale
import com.example.ui.components.CallClinicButton
import com.example.ui.components.EmergencyNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.Strings

data class SpecialityItem(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HomeScreen(
    language: Language,
    textScale: TextScale,
    upcomingAppointment: Appointment?,
    onFindDoctorClick: () -> Unit,
    onTrackQueueClick: (token: String, doctorId: String) -> Unit,
    onSpecialityClick: (String) -> Unit,
    onViewAllAppointmentsClick: () -> Unit,
    onBookAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val specialities = listOf(
        SpecialityItem("General Physician", "General Physician", "सामान्य चिकित्सक", Icons.Default.MedicalServices, ClinicTeal),
        SpecialityItem("Cardiologist", "Cardiologist", "हृदय रोग विशेषज्ञ", Icons.Default.Favorite, Color(0xFFE11D48)),
        SpecialityItem("Pediatrician", "Pediatrician", "शिशु रोग विशेषज्ञ", Icons.Default.ChildCare, Color(0xFFD97706)),
        SpecialityItem("Dentist", "Dentist", "दंत चिकित्सक", Icons.Default.SentimentSatisfiedAlt, Color(0xFF2563EB)),
        SpecialityItem("Orthopedic", "Orthopedic", "हड्डी रोग विशेषज्ञ", Icons.Default.AccessibilityNew, Color(0xFF7C3AED)),
        SpecialityItem("Gynecologist", "Gynecologist", "स्त्री रोग विशेषज्ञ", Icons.Default.PregnantWoman, Color(0xFFDB2777))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Banner at top
        item {
            EmergencyNoticeBanner(language = language, textScale = textScale)
        }

        // Welcome / Search Shortcut
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicNavy),
                modifier = Modifier.fillMaxWidth().testTag("hero_welcome_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (language == Language.HINDI) "नमस्ते, रमेश जी" else "Welcome, Ramesh Sharma",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (22 * textScale.scale).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Strings.get("tagline", language),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFB0CBE5),
                            fontSize = (14 * textScale.scale).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onFindDoctorClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClinicTeal,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("home_find_doctor_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Strings.get("find_doctor", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = (14 * textScale.scale).sp
                            )
                        }

                        if (upcomingAppointment != null) {
                            FilledTonalButton(
                                onClick = {
                                    onTrackQueueClick(upcomingAppointment.tokenNumber, upcomingAppointment.doctorId)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White,
                                    contentColor = ClinicNavy
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("home_track_queue_button")
                            ) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.get("track_queue", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (14 * textScale.scale).sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active / Upcoming Appointment Card
        item {
            if (upcomingAppointment != null && upcomingAppointment.status != QueueStatus.COMPLETED && upcomingAppointment.status != QueueStatus.CANCELLED) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("upcoming_appointment_card")
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
                            Text(
                                text = if (language == Language.HINDI) "आज का आगामी अपॉइंटमेंट" else "Upcoming Appointment Today",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ClinicNavy,
                                    fontSize = (16 * textScale.scale).sp
                                )
                            )
                            StatusBadge(
                                status = upcomingAppointment.status,
                                language = language,
                                textScale = textScale
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ClinicDivider)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(ClinicTealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = ClinicTealDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = upcomingAppointment.doctorName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ClinicNavy,
                                        fontSize = (16 * textScale.scale).sp
                                    )
                                )
                                Text(
                                    text = "${upcomingAppointment.doctorSpeciality} • ${upcomingAppointment.clinicName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF475569),
                                        fontSize = (13 * textScale.scale).sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Token and Slot time highlight box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ClinicSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = Strings.get("your_token", language),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF64748B),
                                            fontSize = (11 * textScale.scale).sp
                                        )
                                    )
                                    Text(
                                        text = upcomingAppointment.tokenNumber,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ClinicTeal,
                                            fontSize = (22 * textScale.scale).sp
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (language == Language.HINDI) "तय समय" else "Scheduled Time",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF64748B),
                                            fontSize = (11 * textScale.scale).sp
                                        )
                                    )
                                    Text(
                                        text = upcomingAppointment.time,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ClinicNavy,
                                            fontSize = (16 * textScale.scale).sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Actions: Track Queue & Call Clinic
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onTrackQueueClick(upcomingAppointment.tokenNumber, upcomingAppointment.doctorId)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("track_queue_cta")
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.get("track_queue", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (14 * textScale.scale).sp
                                )
                            }

                            CallClinicButton(
                                phoneNumber = upcomingAppointment.clinicPhone,
                                language = language,
                                textScale = textScale,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // Empty state when no upcoming appointment
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("empty_appointment_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = ClinicTeal,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Strings.get("no_upcoming_appointment", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (16 * textScale.scale).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Strings.get("book_first_appointment", language),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = (13 * textScale.scale).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onFindDoctorClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("empty_book_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Strings.get("book_appointment", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = (14 * textScale.scale).sp
                            )
                        }
                    }
                }
            }
        }

        // Speciality Shortcuts
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = Strings.get("specialities", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (18 * textScale.scale).sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(specialities) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ClinicSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ClinicBorder),
                            modifier = Modifier
                                .width(130.dp)
                                .clickable { onSpecialityClick(item.id) }
                                .testTag("speciality_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(item.color.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.nameEn,
                                        tint = item.color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == Language.HINDI) item.nameHi else item.nameEn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = ClinicNavy,
                                        fontSize = (12 * textScale.scale).sp
                                    ),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // How ClinicFlow Works in 3 Steps
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = Strings.get("how_it_works", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicNavy,
                            fontSize = (16 * textScale.scale).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 1
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(ClinicTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Strings.get("step_1_title", language),
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                            Text(
                                text = Strings.get("step_1_desc", language),
                                color = Color(0xFF475569),
                                fontSize = (12 * textScale.scale).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 2
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(ClinicTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Strings.get("step_2_title", language),
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                            Text(
                                text = Strings.get("step_2_desc", language),
                                color = Color(0xFF475569),
                                fontSize = (12 * textScale.scale).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 3
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(ClinicTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Strings.get("step_3_title", language),
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                            Text(
                                text = Strings.get("step_3_desc", language),
                                color = Color(0xFF475569),
                                fontSize = (12 * textScale.scale).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
