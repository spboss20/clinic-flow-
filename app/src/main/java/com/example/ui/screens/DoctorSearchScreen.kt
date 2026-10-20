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
import com.example.data.model.TextScale
import com.example.data.model.VerificationStatus
import com.example.ui.components.CallClinicButton
import com.example.ui.theme.*
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchScreen(
    language: Language,
    textScale: TextScale,
    doctors: List<Doctor>,
    searchQuery: String,
    selectedSpeciality: String?,
    maxFeeFilter: Int?,
    onSearchQueryChange: (String) -> Unit,
    onSpecialityChange: (String?) -> Unit,
    onMaxFeeChange: (Int?) -> Unit,
    onSelectDoctor: (Doctor) -> Unit,
    onBookDoctor: (Doctor) -> Unit,
    modifier: Modifier = Modifier
) {
    val specialities = listOf(
        "All",
        "General Physician",
        "Cardiologist",
        "Pediatrician",
        "Orthopedic",
        "Dentist",
        "Gynecologist"
    )

    val filteredDoctors = remember(doctors, searchQuery, selectedSpeciality, maxFeeFilter) {
        doctors.filter { doc ->
            // Verification check: only show verified doctors in search directory
            val isVerified = doc.verificationStatus == VerificationStatus.VERIFIED
            val matchesQuery = searchQuery.isBlank() ||
                doc.name.contains(searchQuery, ignoreCase = true) ||
                doc.speciality.contains(searchQuery, ignoreCase = true) ||
                doc.clinicName.contains(searchQuery, ignoreCase = true) ||
                doc.clinicAddress.contains(searchQuery, ignoreCase = true)

            val matchesSpec = selectedSpeciality == null ||
                selectedSpeciality == "All" ||
                doc.speciality.equals(selectedSpeciality, ignoreCase = true)

            val matchesFee = maxFeeFilter == null || doc.consultationFee <= maxFeeFilter

            isVerified && matchesQuery && matchesSpec && matchesFee
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
    ) {
        // Search header
        Surface(
            color = ClinicSurface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = Strings.get("search_hint", language),
                            fontSize = (14 * textScale.scale).sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ClinicTeal
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("doctor_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Speciality Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(specialities) { spec ->
                        val isSelected = (selectedSpeciality == null && spec == "All") ||
                            (selectedSpeciality == spec)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSpecialityChange(if (spec == "All") null else spec)
                            },
                            label = {
                                Text(
                                    text = when (spec) {
                                        "All" -> Strings.get("all", language)
                                        "General Physician" -> Strings.get("gen_physician", language)
                                        "Cardiologist" -> Strings.get("cardiologist", language)
                                        "Pediatrician" -> Strings.get("pediatrician", language)
                                        "Orthopedic" -> Strings.get("orthopedic", language)
                                        "Dentist" -> Strings.get("dentist", language)
                                        "Gynecologist" -> Strings.get("gynecologist", language)
                                        else -> spec
                                    },
                                    fontSize = (13 * textScale.scale).sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClinicTeal,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Fee Filter Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${Strings.get("consultation_fee", language)}:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = (12 * textScale.scale).sp
                        )
                    )
                    listOf(null to Strings.get("all", language), 500 to "≤ ₹500", 700 to "≤ ₹700").forEach { (fee, label) ->
                        val isSelected = maxFeeFilter == fee
                        FilterChip(
                            selected = isSelected,
                            onClick = { onMaxFeeChange(fee) },
                            label = { Text(label, fontSize = (12 * textScale.scale).sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClinicBlueContainer,
                                selectedLabelColor = ClinicBlue
                            )
                        )
                    }
                }
            }
        }

        // Doctor Results List
        if (filteredDoctors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == Language.HINDI) "कोई डॉक्टर नहीं मिला" else "No Doctors Match Your Search",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicNavy
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == Language.HINDI) "कृपया फ़िल्टर बदलें या दूसरा नाम खोजें।" else "Try clearing your filters or search with another speciality.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredDoctors) { doctor ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDoctor(doctor) }
                            .testTag("doctor_card_${doctor.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top Row: Avatar + Name + Verified Badge
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(ClinicTealContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ClinicTealDark,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = doctor.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ClinicNavy,
                                                fontSize = (16 * textScale.scale).sp
                                            )
                                        )
                                        // Verified Badge
                                        Surface(
                                            color = StatusSuccessContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "Verified Doctor",
                                                    tint = StatusSuccess,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = Strings.get("verified_doctor", language),
                                                    color = StatusSuccess,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = (11 * textScale.scale).sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = if (language == Language.HINDI) doctor.specialityHindi else doctor.speciality,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = ClinicTealDark,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = (14 * textScale.scale).sp
                                        )
                                    )

                                    Text(
                                        text = doctor.qualifications,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF64748B),
                                            fontSize = (12 * textScale.scale).sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = ClinicDivider)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Clinic Name & Address
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ClinicBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${doctor.clinicName} (${doctor.clinicAddress})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ClinicNavy,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = (12 * textScale.scale).sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Experience & Fee highlights
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkHistory,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${doctor.experienceYears} ${Strings.get("years", language)} ${Strings.get("experience", language)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF475569),
                                            fontSize = (12 * textScale.scale).sp
                                        )
                                    )
                                }

                                Text(
                                    text = "₹${doctor.consultationFee}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ClinicTeal,
                                        fontSize = (16 * textScale.scale).sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Buttons: View Profile + Book Appointment
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSelectDoctor(doctor) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicNavy),
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("view_profile_${doctor.id}")
                                ) {
                                    Text(
                                        text = if (language == Language.HINDI) "विवरण देखें" else "View Profile",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (13 * textScale.scale).sp
                                    )
                                }

                                Button(
                                    onClick = { onBookDoctor(doctor) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("book_button_${doctor.id}")
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Strings.get("book_appointment", language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (13 * textScale.scale).sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
