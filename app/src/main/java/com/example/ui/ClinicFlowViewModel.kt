package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ChatMessage
import com.example.data.ai.ClinicFlowAiService
import com.example.data.ai.DraftDelayNotification
import com.example.data.ai.MessageSender
import com.example.data.model.*
import com.example.data.repository.ClinicFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object DoctorSearch : Screen("doctor_search")
    data class DoctorProfile(val doctorId: String) : Screen("doctor_profile")
    data class BookingFlow(val doctorId: String) : Screen("booking_flow")
    data class QueueTracking(val tokenNumber: String, val doctorId: String) : Screen("queue_tracking")
    data object MyAppointments : Screen("my_appointments")
    data object FamilyMembers : Screen("family_members")
    data object Notifications : Screen("notifications")
    data object DoctorDashboard : Screen("doctor_dashboard")
    data object ReceptionistDashboard : Screen("receptionist_dashboard")
    data object AdminPanel : Screen("admin_panel")
    data object AiAssistant : Screen("ai_assistant")
}

data class UiState(
    val language: Language = Language.ENGLISH,
    val textScale: TextScale = TextScale.NORMAL,
    val currentRole: UserRole = UserRole.PATIENT,
    val currentScreen: Screen = Screen.Home,
    val backStack: List<Screen> = listOf(Screen.Home),
    val searchQuery: String = "",
    val selectedSpeciality: String? = null,
    val maxFeeFilter: Int? = null,
    val isEmergencyNoticeExpanded: Boolean = true,
    val bannerMessage: String? = null,
    val selectedDoctorId: String? = null,
    val activeTokenForTracking: String = "A006",
    val activeDoctorIdForTracking: String = "d1",
    val isTtsSpeaking: Boolean = false,
    val isAiThinking: Boolean = false,
    val isGeminiLiveConfigured: Boolean = false,
    val aiMessages: List<ChatMessage> = emptyList(),
)

class ClinicFlowViewModel(
    private val repository: ClinicFlowRepository = ClinicFlowRepository.getInstance(),
    private val aiService: ClinicFlowAiService = ClinicFlowAiService(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val clinics = repository.clinics
    val doctors = repository.doctors
    val timeSlots = repository.timeSlots
    val appointments = repository.appointments
    val queue = repository.queue
    val familyMembers = repository.familyMembers
    val verificationApplications = repository.verificationApplications
    val notifications = repository.notifications
    val auditLogs = repository.auditLogs
    val doctorDelayMinutes = repository.doctorDelayMinutes
    val doctorQueuePaused = repository.doctorQueuePaused

    init {
        _uiState.update {
            it.copy(
                isGeminiLiveConfigured = aiService.isGeminiKeyConfigured(),
                aiMessages = listOf(
                    ChatMessage(
                        id = "welcome_msg",
                        sender = MessageSender.AI,
                        text = "Hello! I am ClinicFlow AI, your appointment and queue assistant. I can help you find verified doctors by speciality or budget, check available time slots, book an appointment, and track your live token and estimated wait time. You can type or speak in English or हिंदी."
                    )
                )
            )
        }
    }

    fun navigateTo(screen: Screen) {
        val currentStack = _uiState.value.backStack
        _uiState.update {
            it.copy(
                currentScreen = screen,
                backStack = currentStack + screen
            )
        }
    }

    fun navigateBack(): Boolean {
        val stack = _uiState.value.backStack
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            _uiState.update {
                it.copy(
                    currentScreen = newStack.last(),
                    backStack = newStack
                )
            }
            return true
        }
        return false
    }

    fun setLanguage(language: Language) {
        _uiState.update { current ->
            val isStaff = current.currentRole == UserRole.DOCTOR || current.currentRole == UserRole.RECEPTIONIST
            val isHindi = language == Language.HINDI
            val updatedMessages = if (current.aiMessages.size <= 1) {
                val welcomeText = when {
                    isStaff && isHindi ->
                        "नमस्ते! मैं क्लिनिकफ्लो स्टाफ को-पायलट हूँ। मैं आज का क्लिनिकल शेड्यूल सारांश, रिक्त स्लॉट और कतार में देरी की घोषणा तैयार करने में आपकी मदद कर सकता हूँ।"
                    isStaff ->
                        "Welcome to ClinicFlow Staff Copilot. I can help summarise today's schedule, identify open appointment slots, explain queue changes, and draft delay announcements."
                    isHindi ->
                        "नमस्ते! मैं क्लिनिकफ्लो एआई हूँ। मैं डॉक्टर खोजने, स्लॉट देखने और लाइव टोकन कतार ट्रैक करने में आपकी मदद कर सकता हूँ।"
                    else ->
                        "Hello! I am ClinicFlow AI. How can I help you find a doctor, book a slot, or track your queue today?"
                }
                listOf(
                    ChatMessage(
                        id = "welcome_${System.currentTimeMillis()}",
                        sender = MessageSender.AI,
                        text = welcomeText,
                        suggestedActions = aiService.getDefaultSuggestions(current.currentRole, isHindi)
                    )
                )
            } else current.aiMessages

            current.copy(
                language = language,
                aiMessages = updatedMessages
            )
        }
    }

    fun setTextScale(scale: TextScale) {
        _uiState.update { it.copy(textScale = scale) }
    }

    fun switchRole(role: UserRole) {
        val isStaff = role == UserRole.DOCTOR || role == UserRole.RECEPTIONIST
        val isHindi = _uiState.value.language == Language.HINDI
        val welcomeText = when {
            isStaff && isHindi ->
                "नमस्ते! मैं क्लिनिकफ्लो स्टाफ को-पायलट हूँ। मैं आज का क्लिनिकल शेड्यूल सारांश, रिक्त स्लॉट और कतार में देरी की घोषणा तैयार करने में आपकी मदद कर सकता हूँ।"
            isStaff ->
                "Welcome to ClinicFlow Staff Copilot. I can help summarise today's schedule, identify open appointment slots, explain queue changes, and draft delay announcements."
            isHindi ->
                "नमस्ते! मैं क्लिनिकफ्लो एआई हूँ। मैं डॉक्टर खोजने, स्लॉट देखने और लाइव टोकन कतार ट्रैक करने में आपकी मदद कर सकता हूँ।"
            else ->
                "Hello! I am ClinicFlow AI. How can I help you find a doctor, book a slot, or track your queue today?"
        }
        _uiState.update {
            val destination = when (role) {
                UserRole.PATIENT -> Screen.Home
                UserRole.DOCTOR -> Screen.DoctorDashboard
                UserRole.RECEPTIONIST -> Screen.ReceptionistDashboard
                UserRole.ADMIN -> Screen.AdminPanel
            }
            it.copy(
                currentRole = role,
                currentScreen = destination,
                backStack = listOf(destination),
                aiMessages = listOf(
                    ChatMessage(
                        id = "welcome_${role.name}_${System.currentTimeMillis()}",
                        sender = MessageSender.AI,
                        text = welcomeText,
                        suggestedActions = aiService.getDefaultSuggestions(role, isHindi)
                    )
                )
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedSpeciality(speciality: String?) {
        _uiState.update { it.copy(selectedSpeciality = speciality) }
    }

    fun setMaxFeeFilter(fee: Int?) {
        _uiState.update { it.copy(maxFeeFilter = fee) }
    }

    fun setTrackingTarget(tokenNumber: String, doctorId: String) {
        _uiState.update {
            it.copy(
                activeTokenForTracking = tokenNumber,
                activeDoctorIdForTracking = doctorId
            )
        }
        navigateTo(Screen.QueueTracking(tokenNumber, doctorId))
    }

    fun setTtsSpeaking(speaking: Boolean) {
        _uiState.update { it.copy(isTtsSpeaking = speaking) }
    }

    fun sendAiPrompt(prompt: String) {
        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = prompt
        )
        val currentMsgs = _uiState.value.aiMessages
        _uiState.update {
            it.copy(
                aiMessages = currentMsgs + userMsg,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val response = aiService.processQuery(
                userPrompt = prompt,
                currentRole = _uiState.value.currentRole,
                language = _uiState.value.language,
                currentUserId = "patient_1"
            )
            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + response,
                    isAiThinking = false
                )
            }
        }
    }

    fun clearAiChat() {
        val role = _uiState.value.currentRole
        val isStaff = role == UserRole.DOCTOR || role == UserRole.RECEPTIONIST
        val isHindi = _uiState.value.language == Language.HINDI
        val welcomeText = when {
            isStaff && isHindi ->
                "नमस्ते! मैं क्लिनिकफ्लो स्टाफ को-पायलट हूँ। मैं आज का क्लिनिकल शेड्यूल सारांश, रिक्त स्लॉट और कतार में देरी की घोषणा तैयार करने में आपकी मदद कर सकता हूँ।"
            isStaff ->
                "Welcome to ClinicFlow Staff Copilot. I can help summarise today's schedule, identify open appointment slots, explain queue changes, and draft delay announcements."
            isHindi ->
                "नमस्ते! मैं क्लिनिकफ्लो एआई हूँ। मैं डॉक्टर खोजने, स्लॉट देखने और लाइव टोकन कतार ट्रैक करने में आपकी मदद कर सकता हूँ।"
            else ->
                "Hello! I am ClinicFlow AI. How can I help you find a doctor, book a slot, or track your queue today?"
        }
        _uiState.update {
            it.copy(
                aiMessages = listOf(
                    ChatMessage(
                        id = "welcome_msg_${System.currentTimeMillis()}",
                        sender = MessageSender.AI,
                        text = welcomeText,
                        suggestedActions = aiService.getDefaultSuggestions(role, isHindi)
                    )
                )
            )
        }
    }

    fun confirmAiDraftDelay(draft: DraftDelayNotification) {
        aiService.confirmDraftDelay(draft)
        val notifMsg = ChatMessage(
            id = "sys_${System.currentTimeMillis()}",
            sender = MessageSender.SYSTEM,
            text = "✅ Delay notification for ~${draft.minutes} minutes broadcasted successfully to ${draft.affectedPatientCount} waiting patient(s)."
        )
        _uiState.update {
            it.copy(aiMessages = it.aiMessages + notifMsg)
        }
    }

    // Repository Actions
    fun bookAppointment(
        doctorId: String,
        clinicId: String,
        date: String,
        time: String,
        patientName: String,
        patientPhone: String,
        patientAge: Int
    ): Result<Appointment> {
        val result = repository.bookAppointment(
            doctorId = doctorId,
            clinicId = clinicId,
            date = date,
            time = time,
            patientName = patientName,
            patientPhone = patientPhone,
            patientAge = patientAge
        )
        if (result.isSuccess) {
            val app = result.getOrThrow()
            _uiState.update {
                it.copy(
                    activeTokenForTracking = app.tokenNumber,
                    activeDoctorIdForTracking = app.doctorId
                )
            }
        }
        return result
    }

    fun registerWalkIn(doctorId: String, clinicId: String, name: String, phone: String, age: Int): QueueEntry {
        return repository.registerWalkIn(doctorId, clinicId, name, phone, age)
    }

    fun checkInPatient(queueEntryId: String) {
        repository.checkInPatient(queueEntryId)
    }

    fun callNextPatient(doctorId: String): QueueEntry? {
        return repository.callNextPatient(doctorId)
    }

    fun startConsultation(queueEntryId: String) {
        repository.startConsultation(queueEntryId)
    }

    fun completeConsultation(queueEntryId: String) {
        repository.completeConsultation(queueEntryId)
    }

    fun markNoShow(queueEntryId: String) {
        repository.markNoShow(queueEntryId)
    }

    fun cancelAppointment(appointmentId: String, reason: String = "Patient requested") {
        repository.cancelAppointment(appointmentId, reason)
    }

    fun announceDelay(doctorId: String, minutes: Int, message: String) {
        repository.announceDelay(doctorId, minutes, message)
    }

    fun togglePauseQueue(doctorId: String): Boolean {
        return repository.togglePauseQueue(doctorId)
    }

    fun approveDoctorApplication(appId: String, adminName: String = "Platform Admin") {
        repository.approveDoctorApplication(appId, adminName)
    }

    fun rejectDoctorApplication(appId: String, reason: String, adminName: String = "Platform Admin") {
        repository.rejectDoctorApplication(appId, reason, adminName)
    }

    fun addFamilyMember(name: String, relation: String, age: Int, gender: String, phone: String) {
        repository.addFamilyMember("patient_1", name, relation, age, gender, phone)
    }

    fun removeFamilyMember(id: String) {
        repository.removeFamilyMember(id)
    }

    fun getQueueEstimate(token: String, doctorId: String): QueueEstimate {
        return repository.getQueueEstimate(token, doctorId)
    }
}
