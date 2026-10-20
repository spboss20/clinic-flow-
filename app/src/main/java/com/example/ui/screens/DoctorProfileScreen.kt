package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Clinic
import com.example.data.model.Doctor
import com.example.data.model.Language
import com.example.data.model.TextScale
import com.example.ui.components.CallClinicButton
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun DoctorProfileScreen(
    doctor: Doctor,
    clinic: Clinic?,
    language: Language,
    textScale: TextScale,
    onBookAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Doctor Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("doctor_profile_hero")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ClinicTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Doctor photo",
                        tint = ClinicTealDark,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (20 * textScale.scale).sp
                    )
                )

                Text(
                    text = if (language == Language.HINDI) doctor.specialityHindi else doctor.speciality,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ClinicTeal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (16 * textScale.scale).sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Verified Badge
                Surface(
                    color = StatusSuccessContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified badge",
                            tint = StatusSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.get("verified_doctor", language),
                            color = StatusSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = (12 * textScale.scale).sp
                        )
                    }
                }
            }
        }

        // Credentials & Registration Details (Transparency)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == Language.HINDI) "चिकित्सा योग्यता व पंजीकरण" else "Qualifications & Registration",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (16 * textScale.scale).sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = ClinicDivider)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.School, contentDescription = null, tint = ClinicBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "डिग्री / योग्यता" else "Qualifications",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                        )
                        Text(
                            text = doctor.qualifications,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = ClinicBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "पंजीकरण संख्या व परिषद" else "Registration No. & Council",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                        )
                        Text(
                            text = "${doctor.regNumber} • ${doctor.regCouncil}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = ClinicBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "औसत परामर्श समय" else "Average Consultation Duration",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                        )
                        Text(
                            text = "${doctor.avgDurationMinutes} ${Strings.get("mins", language)}",
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

        // Clinic Location & Working Hours
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == Language.HINDI) "क्लिनिक का पता व समय" else "Clinic Location & Timings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicNavy,
                        fontSize = (16 * textScale.scale).sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = ClinicDivider)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = doctor.clinicName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ClinicNavy)
                )
                Text(
                    text = clinic?.address ?: doctor.clinicAddress,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${if (language == Language.HINDI) "समय" else "Hours"}: ${doctor.workingHours}",
                    style = MaterialTheme.typography.bodySmall.copy(color = ClinicTealDark, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Call Clinic button
                CallClinicButton(
                    phoneNumber = clinic?.phone ?: doctor.phone,
                    language = language,
                    textScale = textScale,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom Fixed CTA
        Button(
            onClick = onBookAppointmentClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .testTag("profile_book_cta")
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${Strings.get("book_appointment", language)} (₹${doctor.consultationFee})",
                fontWeight = FontWeight.Bold,
                fontSize = (16 * textScale.scale).sp
            )
        }
    }
}
