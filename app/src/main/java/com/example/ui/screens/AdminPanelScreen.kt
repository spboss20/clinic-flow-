package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminPanelScreen(
    applications: List<VerificationApplication>,
    auditLogs: List<AuditLogEntry>,
    doctorCount: Int,
    clinicCount: Int,
    todayAppointmentCount: Int,
    language: Language,
    textScale: TextScale,
    onApproveApplication: (applicationId: String) -> Unit,
    onRejectApplication: (applicationId: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showRejectDialog by remember { mutableStateOf<VerificationApplication?>(null) }
    var rejectReason by remember { mutableStateOf("Incomplete medical council registration record") }

    val dateFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClinicNavy),
                modifier = Modifier.fillMaxWidth().testTag("admin_header")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Platform Administration Console",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (18 * textScale.scale).sp
                        )
                    )
                    Text(
                        text = "Doctor Credentials Audit & Platform Health",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0CBE5))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overview Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Clinics", color = Color(0xFFB0CBE5), fontSize = 11.sp)
                            Text(text = clinicCount.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Verified Docs", color = Color(0xFFB0CBE5), fontSize = 11.sp)
                            Text(text = doctorCount.toString(), color = Color(0xFF4DD0E1), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Today Bookings", color = Color(0xFFB0CBE5), fontSize = 11.sp)
                            Text(text = todayAppointmentCount.toString(), color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Tab Selector (0: Verification Applications, 1: Audit Log)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ClinicSurface,
                contentColor = ClinicTeal
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Doctor Verifications (${applications.count { it.status == VerificationStatus.PENDING }})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Audit Activity Log", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Doctor Verification Applications
            if (applications.isEmpty()) {
                item {
                    Text(
                        text = "No pending doctor verification requests.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                    )
                }
            } else {
                items(applications) { app ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("verification_app_${app.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = app.doctorName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ClinicNavy,
                                            fontSize = (16 * textScale.scale).sp
                                        )
                                    )
                                    Text(
                                        text = "${app.speciality} • ${app.clinicName}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                    )
                                }

                                Surface(
                                    color = when (app.status) {
                                        VerificationStatus.VERIFIED -> StatusSuccessContainer
                                        VerificationStatus.PENDING -> StatusWarningContainer
                                        VerificationStatus.REJECTED -> StatusErrorContainer
                                        VerificationStatus.SUSPENDED -> Color(0xFFE2E8F0)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = app.status.name,
                                        color = when (app.status) {
                                            VerificationStatus.VERIFIED -> StatusSuccess
                                            VerificationStatus.PENDING -> StatusWarning
                                            VerificationStatus.REJECTED -> StatusError
                                            VerificationStatus.SUSPENDED -> Color(0xFF475569)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = ClinicDivider)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Medical Reg No: ${app.regNumber}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = ClinicNavy)
                            )
                            Text(
                                text = "Registration Council: ${app.regCouncil}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                            )
                            Text(
                                text = "Qualifications: ${app.qualifications}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                            )

                            if (app.rejectionReason != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Rejection Reason: ${app.rejectionReason}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StatusError, fontWeight = FontWeight.Bold)
                                )
                            }

                            if (app.status == VerificationStatus.PENDING) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onApproveApplication(app.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp).testTag("approve_doctor_button")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = Strings.get("approve", language), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { showRejectDialog = app },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp).testTag("reject_doctor_button")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = Strings.get("reject", language), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Audit Activity Log
            items(auditLogs) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("audit_log_${log.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ClinicBlueContainer
                            ) {
                                Text(
                                    text = log.action,
                                    color = ClinicBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodyMedium.copy(color = ClinicNavy, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Actor: ${log.actorName} (${log.actorRole})",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            }
        }
    }

    // Rejection Reason Dialog
    if (showRejectDialog != null) {
        val app = showRejectDialog!!
        AlertDialog(
            onDismissRequest = { showRejectDialog = null },
            title = { Text("Reject Doctor Application", fontWeight = FontWeight.Bold, color = ClinicNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Specify recorded audit reason for rejecting ${app.doctorName}:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectApplication(app.id, rejectReason)
                        showRejectDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Reject Application", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
