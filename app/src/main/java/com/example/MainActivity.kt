package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.*
import com.example.ui.components.ClinicFlowTopBar
import com.example.ui.screens.*
import com.example.ui.theme.ClinicFlowTheme
import com.example.ui.theme.ClinicNavy
import com.example.ui.theme.ClinicTeal
import com.example.util.Strings
import com.example.util.TtsManager

class MainActivity : ComponentActivity() {

    private val viewModel: ClinicFlowViewModel by viewModels()
    private var ttsManager: TtsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ttsManager = TtsManager(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val doctors by viewModel.doctors.collectAsStateWithLifecycle()
            val clinics by viewModel.clinics.collectAsStateWithLifecycle()
            val timeSlots by viewModel.timeSlots.collectAsStateWithLifecycle()
            val appointments by viewModel.appointments.collectAsStateWithLifecycle()
            val queue by viewModel.queue.collectAsStateWithLifecycle()
            val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
            val notifications by viewModel.notifications.collectAsStateWithLifecycle()
            val applications by viewModel.verificationApplications.collectAsStateWithLifecycle()
            val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
            val delayMap by viewModel.doctorDelayMinutes.collectAsStateWithLifecycle()
            val pauseMap by viewModel.doctorQueuePaused.collectAsStateWithLifecycle()

            val language = uiState.language
            val textScale = uiState.textScale

            // Intercept hardware and gesture back navigation
            BackHandler(enabled = uiState.backStack.size > 1) {
                viewModel.navigateBack()
            }

            ClinicFlowTheme {
                Scaffold(
                    topBar = {
                        ClinicFlowTopBar(
                            language = language,
                            textScale = textScale,
                            currentRole = uiState.currentRole,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onTextScaleChange = { viewModel.setTextScale(it) },
                            onRoleChange = { viewModel.switchRole(it) },
                            onNotificationClick = { viewModel.navigateTo(Screen.Notifications) },
                            unreadNotificationCount = notifications.count { !it.isRead },
                            canNavigateBack = uiState.backStack.size > 1,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    },
                    bottomBar = {
                        ClinicFlowBottomBar(
                            currentRole = uiState.currentRole,
                            currentScreen = uiState.currentScreen,
                            language = language,
                            textScale = textScale,
                            onNavigate = { screen -> viewModel.navigateTo(screen) }
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val screen = uiState.currentScreen) {
                            is Screen.Home -> {
                                val upcoming = appointments.firstOrNull {
                                    it.patientId == "patient_1" &&
                                        it.status != QueueStatus.COMPLETED &&
                                        it.status != QueueStatus.CANCELLED
                                }
                                HomeScreen(
                                    language = language,
                                    textScale = textScale,
                                    upcomingAppointment = upcoming,
                                    onFindDoctorClick = { viewModel.navigateTo(Screen.DoctorSearch) },
                                    onTrackQueueClick = { token, docId ->
                                        viewModel.setTrackingTarget(token, docId)
                                    },
                                    onSpecialityClick = { spec ->
                                        viewModel.setSelectedSpeciality(spec)
                                        viewModel.navigateTo(Screen.DoctorSearch)
                                    },
                                    onViewAllAppointmentsClick = { viewModel.navigateTo(Screen.MyAppointments) },
                                    onBookAppointmentClick = { viewModel.navigateTo(Screen.DoctorSearch) }
                                )
                            }

                            is Screen.DoctorSearch -> {
                                DoctorSearchScreen(
                                    language = language,
                                    textScale = textScale,
                                    doctors = doctors,
                                    searchQuery = uiState.searchQuery,
                                    selectedSpeciality = uiState.selectedSpeciality,
                                    maxFeeFilter = uiState.maxFeeFilter,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onSpecialityChange = { viewModel.setSelectedSpeciality(it) },
                                    onMaxFeeChange = { viewModel.setMaxFeeFilter(it) },
                                    onSelectDoctor = { doc ->
                                        viewModel.navigateTo(Screen.DoctorProfile(doc.id))
                                    },
                                    onBookDoctor = { doc ->
                                        viewModel.navigateTo(Screen.BookingFlow(doc.id))
                                    }
                                )
                            }

                            is Screen.DoctorProfile -> {
                                val doctor = doctors.find { it.id == screen.doctorId } ?: doctors.first()
                                val clinic = clinics.find { it.id == doctor.clinicId }
                                DoctorProfileScreen(
                                    doctor = doctor,
                                    clinic = clinic,
                                    language = language,
                                    textScale = textScale,
                                    onBookAppointmentClick = {
                                        viewModel.navigateTo(Screen.BookingFlow(doctor.id))
                                    }
                                )
                            }

                            is Screen.BookingFlow -> {
                                val doctor = doctors.find { it.id == screen.doctorId } ?: doctors.first()
                                val clinic = clinics.find { it.id == doctor.clinicId }
                                BookingFlowScreen(
                                    doctor = doctor,
                                    clinic = clinic,
                                    timeSlots = timeSlots,
                                    familyMembers = familyMembers,
                                    language = language,
                                    textScale = textScale,
                                    onConfirmBooking = { dId, cId, date, time, name, phone, age ->
                                        viewModel.bookAppointment(dId, cId, date, time, name, phone, age)
                                    },
                                    onTrackQueueClick = { token, docId ->
                                        viewModel.setTrackingTarget(token, docId)
                                    }
                                )
                            }

                            is Screen.QueueTracking -> {
                                val estimate = viewModel.getQueueEstimate(screen.tokenNumber, screen.doctorId)
                                val doctor = doctors.find { it.id == screen.doctorId }
                                val clinic = clinics.find { it.id == doctor?.clinicId }
                                QueueTrackingScreen(
                                    estimate = estimate,
                                    clinicPhone = clinic?.phone ?: "+91 80 2520 1199",
                                    language = language,
                                    textScale = textScale,
                                    isSpeaking = uiState.isTtsSpeaking,
                                    onSpeakClick = { text ->
                                        viewModel.setTtsSpeaking(true)
                                        ttsManager?.speak(text, isHindi = language == Language.HINDI)
                                    },
                                    onStopSpeakClick = {
                                        viewModel.setTtsSpeaking(false)
                                        ttsManager?.stop()
                                    },
                                    onRefreshClick = {
                                        // Re-triggers state observation
                                    }
                                )
                            }

                            is Screen.MyAppointments -> {
                                MyAppointmentsScreen(
                                    appointments = appointments,
                                    language = language,
                                    textScale = textScale,
                                    onTrackQueue = { token, docId ->
                                        viewModel.setTrackingTarget(token, docId)
                                    },
                                    onCancelAppointment = { appId ->
                                        viewModel.cancelAppointment(appId)
                                    },
                                    onFindDoctorClick = { viewModel.navigateTo(Screen.DoctorSearch) }
                                )
                            }

                            is Screen.FamilyMembers -> {
                                FamilyMembersScreen(
                                    members = familyMembers,
                                    language = language,
                                    textScale = textScale,
                                    onAddMember = { name, relation, age, gender, phone ->
                                        viewModel.addFamilyMember(name, relation, age, gender, phone)
                                    },
                                    onRemoveMember = { id -> viewModel.removeFamilyMember(id) }
                                )
                            }

                            is Screen.Notifications -> {
                                NotificationsScreen(
                                    notifications = notifications,
                                    language = language,
                                    textScale = textScale
                                )
                            }

                            is Screen.DoctorDashboard -> {
                                val primaryDoctor = doctors.first()
                                val docQueue = queue.filter { it.doctorId == primaryDoctor.id }
                                val delay = delayMap[primaryDoctor.id] ?: 0
                                val isPaused = pauseMap[primaryDoctor.id] ?: false

                                DoctorDashboardScreen(
                                    doctorName = primaryDoctor.name,
                                    speciality = primaryDoctor.speciality,
                                    clinicName = primaryDoctor.clinicName,
                                    queueList = docQueue,
                                    delayMinutes = delay,
                                    isQueuePaused = isPaused,
                                    language = language,
                                    textScale = textScale,
                                    onCallNextPatient = { viewModel.callNextPatient(primaryDoctor.id) },
                                    onStartConsultation = { qId -> viewModel.startConsultation(qId) },
                                    onCompleteConsultation = { qId -> viewModel.completeConsultation(qId) },
                                    onMarkNoShow = { qId -> viewModel.markNoShow(qId) },
                                    onAnnounceDelay = { mins, reason -> viewModel.announceDelay(primaryDoctor.id, mins, reason) },
                                    onTogglePauseQueue = { viewModel.togglePauseQueue(primaryDoctor.id) }
                                )
                            }

                            is Screen.ReceptionistDashboard -> {
                                ReceptionistDashboardScreen(
                                    doctors = doctors,
                                    queueList = queue,
                                    language = language,
                                    textScale = textScale,
                                    onRegisterWalkIn = { dId, cId, name, phone, age ->
                                        viewModel.registerWalkIn(dId, cId, name, phone, age)
                                    },
                                    onCheckInPatient = { qId -> viewModel.checkInPatient(qId) },
                                    onCallNextPatient = { dId -> viewModel.callNextPatient(dId) },
                                    onMarkNoShow = { qId -> viewModel.markNoShow(qId) }
                                )
                            }

                            is Screen.AdminPanel -> {
                                AdminPanelScreen(
                                    applications = applications,
                                    auditLogs = auditLogs,
                                    doctorCount = doctors.count { it.isVerified },
                                    clinicCount = clinics.size,
                                    todayAppointmentCount = appointments.size,
                                    language = language,
                                    textScale = textScale,
                                    onApproveApplication = { appId -> viewModel.approveDoctorApplication(appId) },
                                    onRejectApplication = { appId, reason -> viewModel.rejectDoctorApplication(appId, reason) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager?.shutdown()
        ttsManager = null
    }
}

@Composable
fun ClinicFlowBottomBar(
    currentRole: UserRole,
    currentScreen: Screen,
    language: Language,
    textScale: TextScale,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("clinicflow_bottom_navigation")
    ) {
        when (currentRole) {
            UserRole.PATIENT -> {
                NavigationBarItem(
                    selected = currentScreen is Screen.Home,
                    onClick = { onNavigate(Screen.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.DoctorSearch,
                    onClick = { onNavigate(Screen.DoctorSearch) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Find Doctor") },
                    label = { Text("Doctors", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.MyAppointments,
                    onClick = { onNavigate(Screen.MyAppointments) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Appointments") },
                    label = { Text("Bookings", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.QueueTracking,
                    onClick = { onNavigate(Screen.QueueTracking("A006", "d1")) },
                    icon = { Icon(Icons.Default.HourglassTop, contentDescription = "Track Queue") },
                    label = { Text("Queue", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.FamilyMembers,
                    onClick = { onNavigate(Screen.FamilyMembers) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Family") },
                    label = { Text("Family", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
            }
            UserRole.DOCTOR -> {
                NavigationBarItem(
                    selected = currentScreen is Screen.DoctorDashboard,
                    onClick = { onNavigate(Screen.DoctorDashboard) },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Doctor Desk") },
                    label = { Text("Doctor Desk", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Notifications,
                    onClick = { onNavigate(Screen.Notifications) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
            }
            UserRole.RECEPTIONIST -> {
                NavigationBarItem(
                    selected = currentScreen is Screen.ReceptionistDashboard,
                    onClick = { onNavigate(Screen.ReceptionistDashboard) },
                    icon = { Icon(Icons.Default.AssignmentInd, contentDescription = "Reception") },
                    label = { Text("Reception Desk", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Notifications,
                    onClick = { onNavigate(Screen.Notifications) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
            }
            UserRole.ADMIN -> {
                NavigationBarItem(
                    selected = currentScreen is Screen.AdminPanel,
                    onClick = { onNavigate(Screen.AdminPanel) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin Console", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = currentScreen is Screen.Notifications,
                    onClick = { onNavigate(Screen.Notifications) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("System Logs", fontSize = (11 * textScale.scale).sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}
