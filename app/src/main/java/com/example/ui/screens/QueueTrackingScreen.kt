package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.QueueEstimate
import com.example.data.model.QueueStatus
import com.example.data.model.TextScale
import com.example.ui.components.CallClinicButton
import com.example.ui.components.EmergencyNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QueueTrackingScreen(
    estimate: QueueEstimate,
    clinicPhone: String,
    language: Language,
    textScale: TextScale,
    isSpeaking: Boolean,
    onSpeakClick: (text: String) -> Unit,
    onStopSpeakClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var lastUpdatedTime by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()))
    }
    var showPassModal by remember { mutableStateOf(false) }

    val statusText = if (language == Language.HINDI) estimate.status.labelHi else estimate.status.labelEn

    // Audio speech description for elderly
    val speechSummary = remember(estimate, language) {
        if (language == Language.HINDI) {
            "आपका टोकन नंबर ${estimate.userToken} है। वर्तमान में डॉक्टर टोकन नंबर ${estimate.currentServingToken ?: "शुरू नहीं"} देख रहे हैं। आपसे पहले ${estimate.patientsAhead} मरीज हैं। अनुमानित प्रतीक्षा समय लगभग ${estimate.estimatedWaitMinutes} मिनट है।"
        } else {
            "Your token number is ${estimate.userToken}. Currently serving token is ${estimate.currentServingToken ?: "Not started"}. There are ${estimate.patientsAhead} patients ahead of you. Estimated waiting time is approximately ${estimate.estimatedWaitMinutes} minutes."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Banner
        EmergencyNoticeBanner(language = language, textScale = textScale)

        // Doctor & Clinic Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("queue_header_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = estimate.doctorName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (18 * textScale.scale).sp
                            )
                        )
                        Text(
                            text = estimate.clinicName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF64748B),
                                fontSize = (13 * textScale.scale).sp
                            )
                        )
                    }
                    StatusBadge(status = estimate.status, language = language, textScale = textScale)
                }

                // Delay / Pause warning alert if active
                if (estimate.delayNoticeMinutes > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusWarningContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTimeFilled,
                                contentDescription = null,
                                tint = StatusWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == Language.HINDI) {
                                    "क्लिनिक सूचना: परामर्श में लगभग ${estimate.delayNoticeMinutes} मिनट की देरी है।"
                                } else {
                                    "Doctor Advisory: Consultations are delayed by ~${estimate.delayNoticeMinutes} mins."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = (12 * textScale.scale).sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // The Big Token & Queue Tracker Box
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().testTag("live_queue_box")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Strings.get("your_token", language),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFB0CBE5),
                        fontSize = (14 * textScale.scale).sp
                    )
                )
                Text(
                    text = estimate.userToken,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = (48 * textScale.scale).sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E3A5F))
                Spacer(modifier = Modifier.height(16.dp))

                // 3 Live Metric Columns: Now Serving, Patients Ahead, Estimated Wait
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Now Serving
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Strings.get("now_serving", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFB0CBE5),
                                fontSize = (11 * textScale.scale).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = estimate.currentServingToken ?: "—",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4DD0E1),
                                fontSize = (24 * textScale.scale).sp
                            )
                        )
                    }

                    // Patients Ahead
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Strings.get("patients_ahead", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFB0CBE5),
                                fontSize = (11 * textScale.scale).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = estimate.patientsAhead.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = (24 * textScale.scale).sp
                            )
                        )
                    }

                    // Estimated Wait
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Strings.get("estimated_wait", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFB0CBE5),
                                fontSize = (11 * textScale.scale).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "~${estimate.estimatedWaitMinutes} ${Strings.get("mins", language)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F),
                                fontSize = (22 * textScale.scale).sp
                            )
                        )
                    }
                }
            }
        }

        // Read Aloud / Audio Assistance Button (Elderly priority!)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
            modifier = Modifier.fillMaxWidth().testTag("audio_assistance_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
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
                            imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Voice Reader",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == Language.HINDI) "आवाज में स्थिति सुनें" else "Listen to Status Aloud",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                        Text(
                            text = if (language == Language.HINDI) "वरिष्ठ नागरिकों के लिए ऑडियो सहायता" else "Audio assistant for seniors",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = (11 * textScale.scale).sp
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isSpeaking) {
                            onStopSpeakClick()
                        } else {
                            onSpeakClick(speechSummary)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSpeaking) StatusError else ClinicTeal
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 44.dp).testTag("speak_queue_button")
                ) {
                    Text(
                        text = if (isSpeaking) Strings.get("stop_speaking", language) else Strings.get("speak_status", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = (12 * textScale.scale).sp
                    )
                }
            }
        }

        // Live Queue Explanation & Disclaimer
        Surface(
            color = ClinicSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ClinicDivider),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ClinicBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == Language.HINDI) "प्रतीक्षा समय का अनुमान कैसे लगाया जाता है?" else "How is waiting time calculated?",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicNavy,
                            fontSize = (13 * textScale.scale).sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == Language.HINDI) {
                        "प्रतीक्षा समय डॉक्टर के औसत परामर्श समय (~15 मिनट) और आपसे पहले कतार में उपस्थित मरीजों की संख्या पर आधारित एक अनुमान है। यदि किसी मरीज को अधिक समय चाहिए, तो समय में थोड़ा परिवर्तन हो सकता है।"
                    } else {
                        "Waiting time is an estimate calculated using the doctor's configured consultation duration (~15 mins), current serving progress, and patients ahead. Actual duration may vary depending on patient clinical needs."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = (12 * textScale.scale).sp,
                        lineHeight = (16 * textScale.scale).sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (language == Language.HINDI) "अंतिम अपडेट" else "Last updated"}: $lastUpdatedTime",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = (11 * textScale.scale).sp
                        )
                    )

                    TextButton(
                        onClick = {
                            lastUpdatedTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                            onRefreshClick()
                        },
                        modifier = Modifier.testTag("refresh_queue_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.get("refresh_queue", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = (12 * textScale.scale).sp
                        )
                    }
                }
            }
        }

        // Action Buttons: Show Clinic Pass & Call Clinic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showPassModal = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClinicNavy),
                modifier = Modifier
                    .weight(1.2f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("show_clinic_pass_button")
            ) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = Strings.get("show_pass", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = (14 * textScale.scale).sp
                )
            }

            CallClinicButton(
                phoneNumber = clinicPhone,
                language = language,
                textScale = textScale,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Digital Clinic Pass Modal
    if (showPassModal) {
        AlertDialog(
            onDismissRequest = { showPassModal = false },
            title = {
                Text(
                    text = Strings.get("show_pass", language),
                    fontWeight = FontWeight.Bold,
                    color = ClinicNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Strings.get("pass_subtitle", language),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ClinicTealLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = Strings.get("your_token", language),
                                style = MaterialTheme.typography.labelSmall.copy(color = ClinicTealDark)
                            )
                            Text(
                                text = estimate.userToken,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ClinicTeal
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = estimate.doctorName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = estimate.clinicName,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPassModal = false }) {
                    Text(text = if (language == Language.HINDI) "बंद करें" else "Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
