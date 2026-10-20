package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.QueueStatus
import com.example.data.model.TextScale
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicFlowTopBar(
    language: Language,
    textScale: TextScale,
    currentRole: UserRole,
    onLanguageChange: (Language) -> Unit,
    onTextScaleChange: (TextScale) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onNotificationClick: () -> Unit,
    unreadNotificationCount: Int = 0,
    canNavigateBack: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    var showRoleMenu by remember { mutableStateOf(false) }
    var showTextScaleMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canNavigateBack) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(48.dp).testTag("topbar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = ClinicNavy
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ClinicTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "ClinicFlow Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column {
                        Text(
                            text = Strings.get("app_title", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (18 * textScale.scale).sp
                            )
                        )
                        Text(
                            text = when (currentRole) {
                                UserRole.PATIENT -> Strings.get("role_patient", language)
                                UserRole.DOCTOR -> Strings.get("role_doctor", language)
                                UserRole.RECEPTIONIST -> Strings.get("role_receptionist", language)
                                UserRole.ADMIN -> Strings.get("role_admin", language)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ClinicTealDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (12 * textScale.scale).sp
                            )
                        )
                    }
                }

                // Action Controls: Language, Text Size, Role Switcher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher
                    OutlinedButton(
                        onClick = {
                            val next = if (language == Language.ENGLISH) Language.HINDI else Language.ENGLISH
                            onLanguageChange(next)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp).testTag("language_switch_button")
                    ) {
                        Text(
                            text = if (language == Language.ENGLISH) "हिंदी" else "English",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Text Scale Button
                    Box {
                        OutlinedButton(
                            onClick = { showTextScaleMenu = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("text_scale_button")
                        ) {
                            Text(
                                text = textScale.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        DropdownMenu(
                            expanded = showTextScaleMenu,
                            onDismissRequest = { showTextScaleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Normal Text (100%)") },
                                onClick = {
                                    onTextScaleChange(TextScale.NORMAL)
                                    showTextScaleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Large Text (118%) - Elderly friendly") },
                                onClick = {
                                    onTextScaleChange(TextScale.LARGE)
                                    showTextScaleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Extra Large (135%)") },
                                onClick = {
                                    onTextScaleChange(TextScale.EXTRA_LARGE)
                                    showTextScaleMenu = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Role Switcher Button
                    Box {
                        FilledTonalButton(
                            onClick = { showRoleMenu = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ClinicBlueContainer,
                                contentColor = ClinicBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("role_switch_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = "Switch Role",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Strings.get("switch_role", language),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                text = { Text("${Strings.get("role_patient", language)} (Ramesh Sharma)") },
                                onClick = {
                                    onRoleChange(UserRole.PATIENT)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                                text = { Text("${Strings.get("role_doctor", language)} (Dr. Priya Deshmukh)") },
                                onClick = {
                                    onRoleChange(UserRole.DOCTOR)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                                text = { Text("${Strings.get("role_receptionist", language)} (Metro Care Desk)") },
                                onClick = {
                                    onRoleChange(UserRole.RECEPTIONIST)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                text = { Text("${Strings.get("role_admin", language)} (Superuser)") },
                                onClick = {
                                    onRoleChange(UserRole.ADMIN)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Demo Mode indicator banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Demo info",
                        tint = Color(0xFF92400E),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.get("demo_mode_banner", language),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium,
                            fontSize = (11 * textScale.scale).sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyNoticeBanner(
    language: Language,
    textScale: TextScale,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StatusErrorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(StatusError),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.get("emergency_notice", language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = StatusError,
                        fontWeight = FontWeight.Medium,
                        fontSize = (12 * textScale.scale).sp,
                        lineHeight = (16 * textScale.scale).sp
                    )
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: QueueStatus,
    language: Language,
    textScale: TextScale,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        QueueStatus.BOOKED -> Triple(ClinicBlueContainer, ClinicBlue, Icons.Default.Event)
        QueueStatus.CHECKED_IN -> Triple(StatusInfoContainer, StatusInfo, Icons.Default.CheckCircle)
        QueueStatus.WAITING -> Triple(StatusWarningContainer, StatusWarning, Icons.Default.HourglassTop)
        QueueStatus.IN_CONSULTATION -> Triple(ClinicTealContainer, ClinicTealDark, Icons.Default.MedicalInformation)
        QueueStatus.COMPLETED -> Triple(StatusSuccessContainer, StatusSuccess, Icons.Default.DoneAll)
        QueueStatus.CANCELLED -> Triple(StatusErrorContainer, StatusError, Icons.Default.Cancel)
        QueueStatus.NO_SHOW -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), Icons.Default.PersonOff)
    }

    val label = if (language == Language.HINDI) status.labelHi else status.labelEn

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size((14 * textScale.scale).dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (12 * textScale.scale).sp
                )
            )
        }
    }
}

@Composable
fun CallClinicButton(
    phoneNumber: String,
    language: Language,
    textScale: TextScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
            }
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ClinicTeal
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag("call_clinic_button")
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Call Clinic",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = Strings.get("call_clinic", language),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (14 * textScale.scale).sp
            )
        )
    }
}
