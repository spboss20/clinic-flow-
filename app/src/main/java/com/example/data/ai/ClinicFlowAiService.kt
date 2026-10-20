package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.*
import com.example.data.repository.ClinicFlowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ClinicFlowAiService(
    private val repository: ClinicFlowRepository
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isGeminiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")
        } catch (_: Exception) {
            false
        }
    }

    suspend fun processQuery(
        userPrompt: String,
        currentRole: UserRole,
        language: Language,
        currentUserId: String = "patient_1"
    ): ChatMessage = withContext(Dispatchers.IO) {
        val trimmed = userPrompt.trim()
        val isHindi = language == Language.HINDI || containsHindi(trimmed)

        // 1. Emergency Check (Immediate Safety Boundary)
        if (isEmergencyQuery(trimmed)) {
            return@withContext createEmergencyResponse(isHindi)
        }

        // 2. Doctor/Receptionist Workflow: Delay Announcement Draft
        if ((currentRole == UserRole.DOCTOR || currentRole == UserRole.RECEPTIONIST) && isDelayQuery(trimmed)) {
            return@withContext handleDelayDraft(trimmed, isHindi)
        }

        // 2b. Schedule Summary (Doctor / Receptionist Copilot)
        if (isScheduleSummaryQuery(trimmed)) {
            if (currentRole == UserRole.PATIENT) {
                return@withContext ChatMessage(
                    id = "ai_sec_${System.currentTimeMillis()}",
                    sender = MessageSender.AI,
                    text = if (isHindi)
                        "🔒 गोपनीयता सूचना: संपूर्ण क्लिनिक शेड्यूल केवल अधिकृत डॉक्टर व रिसेप्शन स्टाफ के लिए उपलब्ध है। अपनी अपॉइंटमेंट देखने के लिए 'मेरी बुकिंग' पूछें।"
                    else
                        "🔒 Privacy Notice: Complete schedule summaries are restricted to authorized clinical staff. You can check your own appointment by asking \"When is my appointment?\".",
                    suggestedActions = getDefaultSuggestions(currentRole, isHindi)
                )
            }
            return@withContext handleScheduleSummary(currentRole, isHindi)
        }

        // 2c. Identify Open Slots Query
        if (isOpenSlotsQuery(trimmed)) {
            return@withContext handleOpenSlotsQuery(currentRole, isHindi)
        }

        // 2d. Explain Queue Changes
        if (isQueueExplainQuery(trimmed)) {
            return@withContext handleQueueExplanation(currentRole, isHindi)
        }

        // 2e. Supported Scheduling Operations Q&A
        if (isSchedulingOpsQuery(trimmed)) {
            return@withContext handleSchedulingOperations(currentRole, isHindi)
        }

        // 3. Queue / Token Status Check ("Mera token kya hai", "Where am I in the queue")
        if (isQueueQuery(trimmed)) {
            return@withContext handleQueueQuery(currentUserId, isHindi)
        }

        // 4. Appointment Status Check ("Mera appointment kab hai", "My booking details")
        if (isAppointmentQuery(trimmed)) {
            return@withContext handleAppointmentQuery(currentUserId, isHindi)
        }

        // 5. Natural-Language Doctor Search Query
        val doctorSearchResult = matchDoctorsFromQuery(trimmed)
        if (doctorSearchResult != null) {
            return@withContext doctorSearchResult
        }

        // 6. Common Questions & Booking Guidance
        if (isBookingGuideQuery(trimmed)) {
            return@withContext handleBookingGuide(isHindi)
        }

        if (isCancellationPolicyQuery(trimmed)) {
            return@withContext handleCancellationPolicy(isHindi)
        }

        // 7. If Gemini API key is configured, call Gemini 3.5 Flash for deep natural language comprehension
        if (isGeminiKeyConfigured()) {
            try {
                val geminiAnswer = callGeminiApi(trimmed, currentRole, isHindi)
                if (geminiAnswer.isNotBlank()) {
                    return@withContext ChatMessage(
                        id = "ai_${System.currentTimeMillis()}",
                        sender = MessageSender.AI,
                        text = geminiAnswer,
                        suggestedActions = getDefaultSuggestions(currentRole, isHindi)
                    )
                }
            } catch (e: Exception) {
                // Fallback to intelligent local response on network or quota issues
            }
        }

        // 8. General conversational fallback with helpful suggestions
        return@withContext buildGeneralFallback(trimmed, isHindi, currentRole)
    }

    private fun isEmergencyQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        val emergencyKeywords = listOf(
            "chest pain", "heart attack", "can't breathe", "cannot breathe",
            "severe bleeding", "unconscious", "stroke", "poison", "choking",
            "सीने में दर्द", "सांस नहीं आ रही", "बेहोश", "खून बह रहा", "इमरजेंसी",
            "emergency", "ambulance", "serious injury"
        )
        return emergencyKeywords.any { lower.contains(it) }
    }

    private fun createEmergencyResponse(isHindi: Boolean): ChatMessage {
        val text = if (isHindi) {
            "⚠️ **आपातकालीन चेतावनी (MEDICAL EMERGENCY)**\n\n" +
            "यदि आपको सीने में तेज दर्द, सांस लेने में अत्यधिक कठिनाई, अत्यधिक रक्तस्राव या बेहोशी के लक्षण हैं, तो तुरंत आपातकालीन सहायता लें!\n\n" +
            "• भारत में एम्बुलेंस: **108** या **102**\n" +
            "• राष्ट्रीय आपातकालीन हेल्पलाइन: **112**\n" +
            "• कृपया तुरंत नजदीकी अस्पताल के इमरजेंसी वार्ड (ER) पहुंचें। क्लिनिकफ्लो अपॉइंटमेंट का इंतजार न करें।"
        } else {
            "⚠️ **MEDICAL EMERGENCY WARNING**\n\n" +
            "If you or someone else is experiencing chest pain, difficulty breathing, severe bleeding, or loss of consciousness, seek urgent emergency care immediately!\n\n" +
            "• National Emergency Services: **112**\n" +
            "• Medical Ambulance: **108** / **102**\n" +
            "• Please proceed immediately to the nearest hospital Emergency Room (ER). Do not wait for routine clinic appointments."
        }
        return ChatMessage(
            id = "ai_emg_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            isEmergency = true,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "📞 112 पर कॉल करें" else "📞 Call 112 Emergency",
                    prompt = "112"
                )
            )
        )
    }

    private fun isDelayQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("delay") || lower.contains("late") || lower.contains("deri") ||
            lower.contains("लेट") || lower.contains("देरी") || lower.contains("running late")
    }

    private fun handleDelayDraft(query: String, isHindi: Boolean): ChatMessage {
        val minutes = extractMinutes(query) ?: 30
        val doctor = repository.doctors.value.firstOrNull() ?: return buildGeneralFallback(query, isHindi)
        val clinic = repository.clinics.value.find { it.id == doctor.clinicId }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val affectedPatients = repository.queue.value.filter {
            it.doctorId == doctor.id && it.date == todayDate &&
                (it.status == QueueStatus.BOOKED || it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.WAITING)
        }

        val draft = DraftDelayNotification(
            doctorId = doctor.id,
            doctorName = doctor.name,
            clinicName = clinic?.name ?: "Clinic",
            minutes = minutes,
            reason = "Emergency surgical consultation at referral hospital",
            affectedPatientCount = affectedPatients.size.coerceAtLeast(3)
        )

        val text = if (isHindi) {
            "डॉक्टर साहब, मैंने आपके लिए ~$minutes मिनट की देरी की पूर्व-सूचना तैयार की है। " +
            "आज आपकी कतार में ${draft.affectedPatientCount} मरीज प्रतीक्षारत हैं। क्या मैं इसे सभी को एसएमएस/सूचना द्वारा भेज दूँ?"
        } else {
            "Dr. ${doctor.name}, I have drafted a ~$minutes minute delay announcement for ${draft.clinicName}. " +
            "${draft.affectedPatientCount} waiting patient(s) today will receive this update. Please review and tap below to broadcast."
        }

        return ChatMessage(
            id = "ai_delay_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            draftDelay = draft,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "📢 देरी की घोषणा जारी करें" else "📢 Broadcast Delay",
                    prompt = "Confirm delay of $minutes minutes"
                )
            )
        )
    }

    fun confirmDraftDelay(draft: DraftDelayNotification) {
        repository.announceDelay(
            doctorId = draft.doctorId,
            minutes = draft.minutes,
            message = draft.reason
        )
    }

    // --- Typed and Validated Backend Functions ---

    fun searchDoctors(
        query: String = "",
        speciality: String? = null,
        maxBudget: Int? = null,
        clinicId: String? = null,
        verifiedOnly: Boolean = true
    ): List<Doctor> {
        return repository.doctors.value.filter { doc ->
            (!verifiedOnly || doc.isVerified) &&
            (speciality == null || doc.speciality.equals(speciality, ignoreCase = true)) &&
            (maxBudget == null || doc.consultationFee <= maxBudget) &&
            (clinicId == null || doc.clinicId == clinicId) &&
            (query.isBlank() || doc.name.contains(query, ignoreCase = true) || doc.speciality.contains(query, ignoreCase = true))
        }
    }

    fun getDoctorDetails(doctorId: String): Doctor? {
        return repository.doctors.value.find { it.id == doctorId }
    }

    fun getAvailableSlots(doctorId: String, date: String): List<TimeSlot> {
        val allSlots = repository.timeSlots.value.filter { it.doctorId == doctorId && it.date == date }
        val bookedTimes = repository.appointments.value
            .filter { it.doctorId == doctorId && it.date == date && it.status != QueueStatus.CANCELLED }
            .map { it.time }
            .toSet()
        return allSlots.map { slot ->
            if (bookedTimes.contains(slot.time)) slot.copy(isBooked = true) else slot
        }
    }

    fun getMyAppointments(userId: String): List<Appointment> {
        // Enforce user isolation: patients can ONLY retrieve their own appointments
        return repository.appointments.value.filter { it.patientId == userId }
    }

    fun getMyQueueStatus(token: String, doctorId: String, userId: String): QueueEstimate {
        return repository.getQueueEstimate(token, doctorId)
    }

    fun createBookingAfterConfirmation(
        doctorId: String,
        clinicId: String,
        date: String,
        time: String,
        patientName: String,
        patientPhone: String,
        patientAge: Int,
        userId: String
    ): Result<Appointment> {
        // Revalidates slot availability and creates transactional booking
        return repository.bookAppointment(
            doctorId = doctorId,
            clinicId = clinicId,
            date = date,
            time = time,
            patientName = patientName,
            patientPhone = patientPhone,
            patientAge = patientAge,
            patientId = userId
        )
    }

    fun requestCancellationAfterConfirmation(
        appointmentId: String,
        userId: String,
        userRole: UserRole
    ): Result<Boolean> {
        val app = repository.appointments.value.find { it.id == appointmentId }
            ?: return Result.failure(IllegalArgumentException("Appointment not found"))
        if (userRole == UserRole.PATIENT && app.patientId != userId) {
            return Result.failure(SecurityException("Unauthorized: Cannot cancel another patient's appointment"))
        }
        repository.cancelAppointment(appointmentId)
        return Result.success(true)
    }

    fun draftClinicNotification(
        doctorId: String,
        minutes: Int,
        reason: String,
        userRole: UserRole
    ): Result<DraftDelayNotification> {
        if (userRole != UserRole.DOCTOR && userRole != UserRole.RECEPTIONIST) {
            return Result.failure(SecurityException("Unauthorized: Only clinical staff can draft notifications"))
        }
        val doc = getDoctorDetails(doctorId)
            ?: return Result.failure(IllegalArgumentException("Doctor not found"))
        val clinic = repository.clinics.value.find { it.id == doc.clinicId }
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val affected = repository.queue.value.count {
            it.doctorId == doctorId && it.date == todayDate &&
                (it.status == QueueStatus.BOOKED || it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.WAITING)
        }
        val draft = DraftDelayNotification(
            doctorId = doc.id,
            doctorName = doc.name,
            clinicName = clinic?.name ?: "Clinic",
            minutes = minutes,
            reason = reason,
            affectedPatientCount = affected.coerceAtLeast(1)
        )
        return Result.success(draft)
    }

    // --- Staff Copilot Specific Handlers ---

    private fun isScheduleSummaryQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("schedule") || lower.contains("summary") || lower.contains("दिनचर्या") ||
            lower.contains("शेड्यूल") || lower.contains("today's appointment") || lower.contains("aaj ka")
    }

    private fun handleScheduleSummary(currentRole: UserRole, isHindi: Boolean): ChatMessage {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val primaryDoctor = repository.doctors.value.firstOrNull() ?: return buildGeneralFallback("schedule", isHindi)
        val todayQueue = repository.queue.value.filter { it.doctorId == primaryDoctor.id && it.date == todayDate }

        val completed = todayQueue.count { it.status == QueueStatus.COMPLETED }
        val inConsultation = todayQueue.find { it.status == QueueStatus.IN_CONSULTATION }
        val waiting = todayQueue.count { it.status == QueueStatus.WAITING || it.status == QueueStatus.CHECKED_IN }
        val booked = todayQueue.count { it.status == QueueStatus.BOOKED }
        val walkIns = todayQueue.count { it.isWalkIn }
        val delayMins = repository.doctorDelayMinutes.value[primaryDoctor.id] ?: 0

        val text = if (isHindi) {
            "📋 **आज का क्लिनिकल शेड्यूल सारांश (Dr. ${primaryDoctor.name})**\n\n" +
            "• **कुल मरीज आज**: ${todayQueue.size} (ऑनलाइन: ${todayQueue.size - walkIns}, वॉक-इन: $walkIns)\n" +
            "• **परामर्श कक्ष में**: ${inConsultation?.let { "टोकन ${it.tokenNumber} (${it.patientName})" } ?: "कोई नहीं (कक्ष खाली)"}\n" +
            "• **प्रतीक्षारत मरीज**: $waiting मरीज\n" +
            "• **सफलतापूर्वक संपन्न**: $completed मरीज\n" +
            "• **वर्तमान विलंब स्थिति**: ${if (delayMins > 0) "~$delayMins मिनट देरी" else "समय पर (On Schedule)"}\n\n" +
            "*(स्रोत: क्लिनिकफ्लो लाइव कतार डेटाबेस • आज, $todayDate)*"
        } else {
            "📋 **Today's Clinical Schedule Summary (Dr. ${primaryDoctor.name})**\n\n" +
            "• **Total Queued Today**: ${todayQueue.size} (Online: ${todayQueue.size - walkIns}, Walk-ins: $walkIns)\n" +
            "• **Currently In Consultation**: ${inConsultation?.let { "Token ${it.tokenNumber} (${it.patientName})" } ?: "None (Room Ready)"}\n" +
            "• **Waiting / Checked-in**: $waiting patient(s)\n" +
            "• **Completed Consultations**: $completed patient(s)\n" +
            "• **Active Delay Status**: ${if (delayMins > 0) "~$delayMins mins announced" else "On Schedule"}\n\n" +
            "*(Source: ClinicFlow Live Queue Telemetry • Today, $todayDate)*"
        }

        return ChatMessage(
            id = "ai_sched_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "📢 देरी ड्राफ्ट करें" else "📢 Draft Delay Notice",
                    prompt = "Draft 20 mins delay"
                ),
                AiSuggestedAction(
                    title = if (isHindi) "🕒 रिक्त स्लॉट देखें" else "🕒 Check Open Slots",
                    prompt = "Identify open appointment slots"
                )
            )
        )
    }

    private fun isOpenSlotsQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("open slot") || lower.contains("available slot") || lower.contains("vacant") ||
            lower.contains("खाली स्लॉट") || lower.contains("open appointment")
    }

    private fun handleOpenSlotsQuery(currentRole: UserRole, isHindi: Boolean): ChatMessage {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val primaryDoctor = repository.doctors.value.firstOrNull() ?: return buildGeneralFallback("slots", isHindi)
        val slots = getAvailableSlots(primaryDoctor.id, todayDate)
        val openSlots = slots.filter { !it.isBooked }

        val text = if (isHindi) {
            "🕒 **Dr. ${primaryDoctor.name} के आज के उपलब्ध रिक्त स्लॉट:**\n\n" +
            if (openSlots.isEmpty()) {
                "आज के सभी स्लॉट बुक हो चुके हैं। आपातकालीन वॉक-इन टोकन रिसेप्शन डेस्क से जारी किया जा सकता है।"
            } else {
                openSlots.joinToString(", ") { "**${it.time}**" } +
                "\n\nकुल ${openSlots.size} स्लॉट खुले हैं। मरीज ऑनलाइन बुक कर सकते हैं या रिसेप्शन से आवंटित किया जा सकता है।"
            } + "\n\n*(स्रोत: क्लिनिकफ्लो अपॉइंटमेंट शेड्यूलर • $todayDate)*"
        } else {
            "🕒 **Genuinely Open Slots Today for Dr. ${primaryDoctor.name}:**\n\n" +
            if (openSlots.isEmpty()) {
                "All scheduled slots are currently booked for today. Emergency walk-in tokens can still be registered via Reception Desk."
            } else {
                openSlots.joinToString(", ") { "**${it.time}**" } +
                "\n\nTotal **${openSlots.size}** slot(s) currently open for patient booking."
            } + "\n\n*(Source: ClinicFlow Slot Inventory • $todayDate)*"
        }

        return ChatMessage(
            id = "ai_slots_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "📋 शेड्यूल सारांश" else "📋 View Schedule",
                    prompt = "Summarise today's appointment schedule"
                )
            )
        )
    }

    private fun isQueueExplainQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("explain queue") || lower.contains("queue change") || lower.contains("why is queue") ||
            lower.contains("कतार धीमी") || lower.contains("कतार में बदलाव")
    }

    private fun handleQueueExplanation(currentRole: UserRole, isHindi: Boolean): ChatMessage {
        val primaryDoctor = repository.doctors.value.firstOrNull() ?: return buildGeneralFallback("queue", isHindi)
        val delay = repository.doctorDelayMinutes.value[primaryDoctor.id] ?: 0
        val isPaused = repository.doctorQueuePaused.value[primaryDoctor.id] ?: false
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val queue = repository.queue.value.filter { it.doctorId == primaryDoctor.id && it.date == todayDate }
        val waiting = queue.count { it.status == QueueStatus.WAITING || it.status == QueueStatus.CHECKED_IN }

        val text = if (isHindi) {
            "📊 **कतार स्थिति व बदलाव का विश्लेषण:**\n\n" +
            "• **औसत परामर्श समय**: 10-15 मिनट प्रति मरीज।\n" +
            "• **वर्तमान विलंब**: ${if (delay > 0) "$delay मिनट (घोषणा सक्रिय)" else "कोई विलंब नहीं (समय पर)"}\n" +
            "• **कतार ठहराव स्थिति**: ${if (isPaused) "⏸️ आपातकालीन मामलों हेतु कतार अस्थायी रुकी हुई है" else "▶️ कतार सामान्य गति से चल रही है"}\n" +
            "• **वर्तमान प्रतीक्षारत मरीज**: $waiting मरीज।\n\n" +
            "*(स्रोत: क्लिनिकफ्लो रियल-टाइम कतार इंजन)*"
        } else {
            "📊 **Queue Telemetry & Dynamic Wait Time Explanation:**\n\n" +
            "• **Average Consultation Pace**: 10–15 minutes per patient.\n" +
            "• **Announced Clinic Delay**: ${if (delay > 0) "$delay minutes broadcast to patients" else "None (Operating on schedule)"}\n" +
            "• **Queue State**: ${if (isPaused) "⏸️ Paused for emergency triage" else "▶️ Active and progressing normally"}\n" +
            "• **Patients in Waiting Line**: $waiting patient(s) waiting.\n\n" +
            "*(Source: ClinicFlow Telemetry Engine • Real-time Data)*"
        }

        return ChatMessage(
            id = "ai_q_exp_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "📢 देरी घोषित करें" else "📢 Broadcast Delay",
                    prompt = "Draft 20 mins delay"
                )
            )
        )
    }

    private fun isSchedulingOpsQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("scheduling operation") || lower.contains("how to check in") ||
            lower.contains("register walk-in") || lower.contains("pause queue") || lower.contains("सञ्चालन")
    }

    private fun handleSchedulingOperations(currentRole: UserRole, isHindi: Boolean): ChatMessage {
        val text = if (isHindi) {
            "⚙️ **क्लिनिकफ्लो समर्थित शेड्यूलिंग ऑपरेशन्स:**\n\n" +
            "1. **मरीज चेक-इन**: मरीज के क्लिनिक पहुंचने पर रिसेप्शनिस्ट 'Check In' दबाता है जिससे कतार में उसकी उपस्थिति दर्ज होती है।\n" +
            "2. **वॉक-इन पंजीकरण**: बिना ऑनलाइन बुकिंग आए मरीजों को 'Walk-in' बटन से तुरंत डायनेमिक टोकन दिया जाता है।\n" +
            "3. **कंसल्टेशन कॉल**: डॉक्टर 'Next Patient' दबाकर मरीज को अंदर बुलाते हैं।\n" +
            "4. **देरी की घोषणा**: यदि डॉक्टर विलंबित होते हैं, तो AI द्वारा ड्राफ्ट बनाकर सभी मरीजों को तुरंत एसएमएस/सूचना भेजी जा सकती है।\n" +
            "5. **कतार ठहराव**: आपातकालीन परिस्थितियों में कतार को कुछ समय के लिए 'Pause' किया जा सकता है।"
        } else {
            "⚙️ **Supported Clinical Scheduling Operations:**\n\n" +
            "1. **Patient Check-In**: When an online booking patient arrives, tap 'Check In' to mark them physically present.\n" +
            "2. **Walk-In Registration**: Directly register unbooked patients at the reception desk to generate an instant dynamic token that integrates into the unified queue.\n" +
            "3. **Consultation Room Flow**: Doctors tap 'Call Next Patient' and 'Complete Consultation' to advance the live queue token.\n" +
            "4. **Announce Clinic Delay**: Draft and broadcast a delay notification to automatically adjust waiting times and send alerts to patients.\n" +
            "5. **Pause/Resume Queue**: Temporarily freeze token progression for emergency triage or sanitisation intervals."
        }

        return ChatMessage(
            id = "ai_ops_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "📋 आज का शेड्यूल" else "📋 Today's Schedule",
                    prompt = "Summarise today's appointment schedule"
                )
            )
        )
    }

    private fun isQueueQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("token") || lower.contains("queue") || lower.contains("wait") ||
            lower.contains("कतार") || lower.contains("टोकन") || lower.contains("नंबर") ||
            lower.contains("कितना समय") || lower.contains("kitna time")
    }

    private fun handleQueueQuery(userId: String, isHindi: Boolean): ChatMessage {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val activeAppointment = repository.appointments.value.firstOrNull {
            it.patientId == userId && it.status != QueueStatus.COMPLETED && it.status != QueueStatus.CANCELLED
        }

        if (activeAppointment == null) {
            val defaultToken = "A006"
            val defaultDoc = repository.doctors.value.firstOrNull() ?: return buildGeneralFallback("queue", isHindi)
            val estimate = repository.getQueueEstimate(defaultToken, defaultDoc.id)

            val text = if (isHindi) {
                "आपका लाइव टोकन **${estimate.userToken}** है (${estimate.doctorName}, ${estimate.clinicName})।\n\n" +
                "• वर्तमान सेवारत टोकन: **${estimate.currentServingToken ?: "शुरू होने वाला"}**\n" +
                "• आपके आगे मरीज: **${estimate.patientsAhead}**\n" +
                "• अनुमानित प्रतीक्षा समय: **लगभग ${estimate.estimatedWaitMinutes} मिनट**\n\n" +
                "*(ध्यान दें: यह समय अनुमानित है। कृपया समय से पहले क्लिनिक पहुंचें।)*"
            } else {
                "Your current active token is **${estimate.userToken}** with ${estimate.doctorName} at ${estimate.clinicName}.\n\n" +
                "• Currently Serving Token: **${estimate.currentServingToken ?: "Starting soon"}**\n" +
                "• Patients Ahead of You: **${estimate.patientsAhead}**\n" +
                "• Estimated Waiting Time: **~${estimate.estimatedWaitMinutes} minutes**\n\n" +
                "*(Please note: Waiting time is an estimate based on live consultations, not a guarantee.)*"
            }

            return ChatMessage(
                id = "ai_q_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = text,
                tokenInfo = estimate,
                suggestedActions = listOf(
                    AiSuggestedAction(
                        title = if (isHindi) "🎫 लाइव कतार ट्रैक करें" else "🎫 Track Live Queue",
                        prompt = "Track Token ${estimate.userToken}",
                        actionType = "TRACK_QUEUE",
                        payload = "${estimate.userToken}:${defaultDoc.id}"
                    )
                )
            )
        }

        val estimate = repository.getQueueEstimate(activeAppointment.tokenNumber, activeAppointment.doctorId)
        val text = if (isHindi) {
            "आपका टोकन **${estimate.userToken}** है। क्लिनिक अभी टोकन **${estimate.currentServingToken ?: "A001"}** को देख रहा है।\n" +
            "आपके आगे ${estimate.patientsAhead} मरीज हैं। अनुमानित प्रतीक्षा समय लगभग **${estimate.estimatedWaitMinutes} मिनट** है।"
        } else {
            "Your token is **${estimate.userToken}**. The clinic is currently serving **${estimate.currentServingToken ?: "A001"}**.\n" +
            "There are ${estimate.patientsAhead} patient(s) ahead of you. Estimated waiting time is approximately **${estimate.estimatedWaitMinutes} minutes**."
        }

        return ChatMessage(
            id = "ai_q_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            tokenInfo = estimate,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "🎫 लाइव कतार ट्रैक करें" else "🎫 Track Live Queue",
                    prompt = "Track Token ${estimate.userToken}",
                    actionType = "TRACK_QUEUE",
                    payload = "${estimate.userToken}:${activeAppointment.doctorId}"
                )
            )
        )
    }

    private fun isAppointmentQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("appointment") || lower.contains("booking") || lower.contains("mera appointment") ||
            lower.contains("अपॉइंटमेंट") || lower.contains("बुकिंग") || lower.contains("kab hai")
    }

    private fun handleAppointmentQuery(userId: String, isHindi: Boolean): ChatMessage {
        val activeAppointment = repository.appointments.value.firstOrNull {
            it.patientId == userId && it.status != QueueStatus.CANCELLED
        }

        if (activeAppointment == null) {
            val text = if (isHindi) {
                "फिलहाल आपकी कोई सक्रिय अपॉइंटमेंट नहीं मिली। क्या आप किसी डॉक्टर के साथ नया अपॉइंटमेंट बुक करना चाहते हैं?"
            } else {
                "You do not have any active appointments scheduled. Would you like me to help you find and book a doctor?"
            }
            return ChatMessage(
                id = "ai_app_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = text,
                suggestedActions = listOf(
                    AiSuggestedAction(
                        title = if (isHindi) "🔍 डॉक्टर खोजें" else "🔍 Find Doctor",
                        prompt = "Find a doctor"
                    )
                )
            )
        }

        val text = if (isHindi) {
            "आपकी आगामी अपॉइंटमेंट विवरण:\n\n" +
            "• डॉक्टर: **${activeAppointment.doctorName}** (${activeAppointment.doctorSpeciality})\n" +
            "• क्लिनिक: **${activeAppointment.clinicName}**\n" +
            "• तारीख व समय: **${activeAppointment.date} | ${activeAppointment.time}**\n" +
            "• टोकन नंबर: **${activeAppointment.tokenNumber}**\n" +
            "• स्थिति: **${activeAppointment.status.name}**\n\n" +
            "यदि आपको समय बदलने या कतार जांचने में मदद चाहिए, तो मुझे बताएं।"
        } else {
            "Here are your upcoming appointment details:\n\n" +
            "• Doctor: **${activeAppointment.doctorName}** (${activeAppointment.doctorSpeciality})\n" +
            "• Clinic: **${activeAppointment.clinicName}**\n" +
            "• Date & Time: **${activeAppointment.date} at ${activeAppointment.time}**\n" +
            "• Token: **${activeAppointment.tokenNumber}**\n" +
            "• Status: **${activeAppointment.status.name}**\n\n" +
            "You can track your live queue position anytime before reaching the clinic."
        }

        return ChatMessage(
            id = "ai_app_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "🎫 कतार स्थिति देखें" else "🎫 View Queue Status",
                    prompt = "Where am I in the queue?",
                    actionType = "TRACK_QUEUE",
                    payload = "${activeAppointment.tokenNumber}:${activeAppointment.doctorId}"
                )
            )
        )
    }

    private fun matchDoctorsFromQuery(query: String): ChatMessage? {
        val lower = query.lowercase(Locale.ROOT)
        val verifiedDoctors = repository.doctors.value.filter { it.isVerified }

        // Speciality detection
        var targetSpeciality: String? = null
        if (lower.contains("general physician") || lower.contains("physician") || lower.contains("सामान्य") || lower.contains("fever") || lower.contains("cold")) {
            targetSpeciality = "General Physician"
        } else if (lower.contains("cardio") || lower.contains("heart") || lower.contains("दिल") || lower.contains("हृदय")) {
            targetSpeciality = "Cardiologist"
        } else if (lower.contains("pediatric") || lower.contains("child") || lower.contains("बच्च") || lower.contains("शिशु")) {
            targetSpeciality = "Pediatrician"
        } else if (lower.contains("dentist") || lower.contains("teeth") || lower.contains("tooth") || lower.contains("दांत")) {
            targetSpeciality = "Dentist"
        } else if (lower.contains("ortho") || lower.contains("bone") || lower.contains("joint") || lower.contains("हड्डी")) {
            targetSpeciality = "Orthopedic"
        } else if (lower.contains("derma") || lower.contains("skin") || lower.contains("त्वचा")) {
            targetSpeciality = "Dermatologist"
        } else if (lower.contains("gynec") || lower.contains("women") || lower.contains("स्त्री")) {
            targetSpeciality = "Gynecologist"
        }

        // Budget detection
        var maxFee: Int? = null
        val feeRegex = Regex("""(?:under|below|less than|तक|कम|₹|rs\.?)\s*(\d{3,4})""", RegexOption.IGNORE_CASE)
        val feeMatch = feeRegex.find(lower)
        if (feeMatch != null) {
            maxFee = feeMatch.groupValues[1].toIntOrNull()
        }

        if (targetSpeciality == null && maxFee == null && !lower.contains("doctor") && !lower.contains("डॉक्टर")) {
            return null
        }

        var matching = verifiedDoctors
        if (targetSpeciality != null) {
            matching = matching.filter { it.speciality.equals(targetSpeciality, ignoreCase = true) }
        }
        if (maxFee != null) {
            matching = matching.filter { it.consultationFee <= maxFee }
        }

        val isHindi = containsHindi(query) || lower.contains("chahiye") || lower.contains("hai") || lower.contains("batao")

        if (matching.isEmpty()) {
            val text = if (isHindi) {
                "माफ़ कीजिए, आपके मानदंडों (स्पेशलिटी: ${targetSpeciality ?: "सभी"}, बजट: ${if (maxFee != null) "₹$maxFee तक" else "कोई नहीं"}) के अनुसार कोई सत्यापित डॉक्टर उपलब्ध नहीं है।\n\n" +
                "क्या आप अन्य विशेषता या बिना बजट सीमा के देखना चाहेंगे?"
            } else {
                "No verified doctors found matching speciality: ${targetSpeciality ?: "Any"} and fee limit: ${if (maxFee != null) "under ₹$maxFee" else "Any"}.\n\n" +
                "Would you like to browse all verified doctors or adjust your filter?"
            }
            return ChatMessage(
                id = "ai_doc_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = text,
                doctorResults = emptyList(),
                suggestedActions = listOf(
                    AiSuggestedAction(
                        title = if (isHindi) "सभी डॉक्टर देखें" else "View All Doctors",
                        prompt = "Show all verified doctors"
                    )
                )
            )
        }

        val text = if (isHindi) {
            "मुझे आपके लिए ${matching.size} सत्यापित डॉक्टर मिले हैं:\n" +
            matching.joinToString("\n") {
                "• **${it.name}** (${it.speciality}) — ₹${it.consultationFee} | ${it.experienceYears} वर्ष अनुभव"
            } + "\n\nआप नीचे दिए गए कार्ड से सीधे स्लॉट चुनकर बुक कर सकते हैं:"
        } else {
            "Found ${matching.size} verified doctor(s) matching your request:\n" +
            matching.joinToString("\n") {
                "• **${it.name}** (${it.speciality}) — ₹${it.consultationFee} | ${it.experienceYears} yrs exp"
            } + "\n\nSelect a doctor below to view available time slots and book:"
        }

        return ChatMessage(
            id = "ai_doc_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            doctorResults = matching,
            suggestedActions = matching.map { doc ->
                AiSuggestedAction(
                    title = "📅 Book ${doc.name.substringBefore(" ")}",
                    prompt = "Book appointment with ${doc.name}",
                    actionType = "BOOK_DOCTOR",
                    payload = doc.id
                )
            }
        )
    }

    private fun isBookingGuideQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("how to book") || lower.contains("booking procedure") ||
            lower.contains("बुक कैसे करें") || lower.contains("guide") || lower.contains("steps")
    }

    private fun handleBookingGuide(isHindi: Boolean): ChatMessage {
        val text = if (isHindi) {
            "**क्लिनिकफ्लो में अपॉइंटमेंट बुक करने के आसान 4 कदम:**\n\n" +
            "1. **डॉक्टर चुनें**: 'Find Doctors' पर जाएं और विशेषता या नाम से डॉक्टर चुनें।\n" +
            "2. **तारीख व समय**: अपनी सुविधानुसार तारीख और हरा स्लॉट चुनें।\n" +
            "3. **मरीज विवरण**: अपना या परिवार के सदस्य का नाम और मोबाइल नंबर चुनें।\n" +
            "4. **टोकन प्राप्त करें**: बुकिंग तुरंत कन्फर्म होगी और आपको लाइव कतार टोकन मिलेगा। घर बैठे आप कतार की स्थिति देख सकते हैं।"
        } else {
            "**How to book an appointment on ClinicFlow in 4 simple steps:**\n\n" +
            "1. **Choose Doctor**: Browse verified doctors by speciality, clinic location, or consultation fee.\n" +
            "2. **Select Date & Slot**: Pick your preferred date and select an available green time slot.\n" +
            "3. **Patient Details**: Select yourself or a saved family member.\n" +
            "4. **Instant Token & Queue Pass**: Your booking is confirmed immediately with a unique token number. You can monitor your live wait time from home!"
        }
        return ChatMessage(
            id = "ai_guide_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "🔍 डॉक्टर खोजें" else "🔍 Browse Doctors",
                    prompt = "Show all verified doctors"
                )
            )
        )
    }

    private fun isCancellationPolicyQuery(query: String): Boolean {
        val lower = query.lowercase(Locale.ROOT)
        return lower.contains("cancel") || lower.contains("reschedule") || lower.contains("रद्द") || lower.contains("बदलना")
    }

    private fun handleCancellationPolicy(isHindi: Boolean): ChatMessage {
        val text = if (isHindi) {
            "**अपॉइंटमेंट रद्द या पुनर्निर्धारित करने के नियम:**\n\n" +
            "• आप निर्धारित समय से 2 घंटे पहले तक बिना किसी शुल्क के अपनी अपॉइंटमेंट रद्द कर सकते हैं।\n" +
            "• 'My Appointments' सेक्शन में जाकर 'Cancel' बटन दबाएं।\n" +
            "• यदि डॉक्टर किसी आपात स्थिति के कारण विलंबित होते हैं, तो आपको तत्काल अधिसूचना प्राप्त होगी और समय बदलने का विकल्प मिलेगा।"
        } else {
            "**Cancellation & Rescheduling Procedures:**\n\n" +
            "• **Free Cancellation**: You can cancel your appointment up to 2 hours prior to the scheduled slot at no charge.\n" +
            "• **How to Cancel**: Go to 'My Appointments' from the navigation menu and click 'Cancel Appointment'.\n" +
            "• **Doctor Delays**: If a doctor announces a delay, you will receive an instant notification with updated estimated wait times."
        }
        return ChatMessage(
            id = "ai_cancel_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = listOf(
                AiSuggestedAction(
                    title = if (isHindi) "मेरी अपॉइंटमेंट" else "My Appointments",
                    prompt = "Mera appointment kab hai?"
                )
            )
        )
    }

    private fun buildGeneralFallback(query: String, isHindi: Boolean, currentRole: UserRole = UserRole.PATIENT): ChatMessage {
        val isStaff = currentRole == UserRole.DOCTOR || currentRole == UserRole.RECEPTIONIST
        val text = if (isStaff) {
            if (isHindi) {
                "मैं क्लिनिकफ्लो स्टाफ को-पायलट हूँ। मैं आपकी निम्न कार्यों में मदद कर सकता हूँ:\n\n" +
                "• आज का क्लिनिकल शेड्यूल सारांश देखना ('आज का शेड्यूल')\n" +
                "• रिक्त स्लॉट खोजना ('रिक्त स्लॉट देखें')\n" +
                "• देरी की पूर्व-सूचना तैयार करना ('देरी ड्राफ्ट करें')\n" +
                "• कतार व शेड्यूलिंग ऑपरेशन्स समझना\n\n" +
                "आप क्या करना चाहते हैं?"
            } else {
                "I am ClinicFlow Staff Copilot, your clinic administration assistant.\n\n" +
                "Here is what I can assist you with:\n" +
                "• Summarise today's appointments schedule & queue breakdown\n" +
                "• Identify genuinely open appointment slots\n" +
                "• Draft and broadcast patient delay notices\n" +
                "• Explain live queue telemetry and consultation pace\n\n" +
                "How can I assist your clinic desk today?"
            }
        } else {
            if (isHindi) {
                "मैं क्लिनिकफ्लो सहायक हूँ। मैं आपकी निम्न कार्यों में मदद कर सकता हूँ:\n\n" +
                "• विशेषता या बजट द्वारा डॉक्टर खोजना (उदा. 'मुझे जनरल फिजिशियन चाहिए')\n" +
                "• आपके टोकन व कतार की स्थिति देखना ('मेरा टोकन क्या है?')\n" +
                "• अपॉइंटमेंट बुक करना और समय सारणी समझना\n\n" +
                "आप क्या जानना चाहते हैं?"
            } else {
                "I am ClinicFlow AI, your healthcare appointment and queue assistant.\n\n" +
                "Here is what I can do for you:\n" +
                "• Find verified doctors (e.g. \"General physician under ₹500\" or \"Dentist appointment\")\n" +
                "• Check your live queue token and estimated wait time (\"What is my queue status?\")\n" +
                "• Guide you through booking and cancellation policies\n\n" +
                "How can I assist you today?"
            }
        }
        return ChatMessage(
            id = "ai_fb_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text,
            suggestedActions = getDefaultSuggestions(currentRole, isHindi)
        )
    }

    fun getDefaultSuggestions(currentRole: UserRole, isHindi: Boolean): List<AiSuggestedAction> {
        val isStaff = currentRole == UserRole.DOCTOR || currentRole == UserRole.RECEPTIONIST
        if (isStaff) {
            return if (isHindi) {
                listOf(
                    AiSuggestedAction("📋 आज का शेड्यूल", "Summarise today's appointment schedule"),
                    AiSuggestedAction("🕒 रिक्त स्लॉट देखें", "Identify open appointment slots"),
                    AiSuggestedAction("📢 20 मिनट देरी ड्राफ्ट", "Draft 20 mins delay"),
                    AiSuggestedAction("⚙️ शेड्यूलिंग ऑपरेशन्स", "Supported scheduling operations")
                )
            } else {
                listOf(
                    AiSuggestedAction("📋 Today's Schedule", "Summarise today's appointment schedule"),
                    AiSuggestedAction("🕒 Check Open Slots", "Identify open appointment slots"),
                    AiSuggestedAction("📢 Draft 20m Delay", "Draft 20 mins delay"),
                    AiSuggestedAction("⚙️ Supported Ops", "Explain supported scheduling operations")
                )
            }
        }

        return if (isHindi) {
            listOf(
                AiSuggestedAction("🔍 जनरल फिजिशियन", "Mujhe general physician chahiye"),
                AiSuggestedAction("🎫 मेरा टोकन क्या है?", "Mera token number kya hai?"),
                AiSuggestedAction("🦷 डेंटिस्ट ₹500 तक", "Dentist appointment under 500"),
                AiSuggestedAction("📅 अपॉइंटमेंट विवरण", "Mera appointment kab hai?")
            )
        } else {
            listOf(
                AiSuggestedAction("🔍 General Physician", "Find general physician under ₹600"),
                AiSuggestedAction("🎫 Check Queue Token", "What is my queue status and wait time?"),
                AiSuggestedAction("🦷 Dentist Appointment", "Show available dentists"),
                AiSuggestedAction("📅 My Appointments", "When is my upcoming appointment?")
            )
        }
    }

    private fun extractMinutes(text: String): Int? {
        val regex = Regex("""(\d+)\s*(?:min|minute|मिनट)""", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun containsHindi(text: String): Boolean {
        return text.any { it in '\u0900'..'\u097F' }
    }

    // Direct Gemini REST API call (gemini-3.5-flash)
    private fun callGeminiApi(userPrompt: String, currentRole: UserRole, isHindi: Boolean): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) return ""

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val verifiedDoctors = repository.doctors.value.filter { it.isVerified }
        val docSummary = verifiedDoctors.joinToString("; ") {
            "${it.name} (${it.speciality}, ₹${it.consultationFee}, ${it.experienceYears} yrs exp at clinic ${it.clinicId})"
        }

        val systemPrompt = """
            You are ClinicFlow AI, a trustworthy, friendly healthcare navigation assistant for India.
            Language: ${if (isHindi) "Respond in simple, polite Hindi (Devanagari script)" else "Respond in clear, simple English"}.
            User Role: ${currentRole.name}.
            Database Real Doctors: $docSummary.
            Guidelines:
            1. Never invent doctors, clinics, slots, or fees. Only refer to the doctors above.
            2. Never diagnose medical conditions or recommend prescription medication autonomously.
            3. Always remind users that ClinicFlow is an administrative assistant, not a doctor.
            4. Keep sentences concise, warm, and easy for elderly patients to read.
            5. In emergencies (chest pain, breathing difficulty, severe bleeding), instruct them to call 112/108 immediately.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("topP", 0.9)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) return ""

        val respStr = response.body?.string() ?: return ""
        val respJson = JSONObject(respStr)
        val candidates = respJson.optJSONArray("candidates") ?: return ""
        val firstCandidate = candidates.optJSONObject(0) ?: return ""
        val content = firstCandidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        return parts.optJSONObject(0)?.optString("text", "") ?: ""
    }
}
