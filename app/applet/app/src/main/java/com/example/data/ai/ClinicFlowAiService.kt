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
    private val repository: ClinicFlowRepository = ClinicFlowRepository.getInstance()
) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun isGeminiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun processQuery(
        userPrompt: String,
        currentRole: UserRole,
        language: Language,
        currentUserId: String = "patient_1"
    ): ChatMessage = withContext(Dispatchers.IO) {
        val trimmed = userPrompt.trim()

        // 1. Healthcare Safety Check: Medical Emergency Triage Guardrail
        val emergencyKeywords = listOf(
            "chest pain", "breathing difficulty", "severe bleeding", "unconscious", "heart attack", "stroke",
            "सीने में दर्द", "सांस लेने में तकलीफ", "खून बह रहा", "बेहोश", "गंभीर चोट", "हार्ट अटैक"
        )
        if (emergencyKeywords.any { trimmed.contains(it, ignoreCase = true) }) {
            val alertText = if (language == Language.HINDI) {
                "⚠️ आपातकालीन चिकित्सा सूचना: आपके द्वारा बताए गए लक्षण (जैसे सीने में दर्द या सांस की तकलीफ) गंभीर हो सकते हैं। कृपया साधारण अपॉइंटमेंट की प्रतीक्षा न करें। तुरंत राष्ट्रीय आपातकालीन सेवा 112 या एम्बुलेंस 108 पर कॉल करें या नजदीकी आपातकालीन अस्पताल जाएं।"
            } else {
                "⚠️ MEDICAL EMERGENCY ADVISORY: The symptoms you described may indicate an urgent medical situation. Please do not wait for a routine clinic appointment. Immediately call national emergency services 112 / 108 or proceed to the nearest hospital emergency room."
            }
            return@withContext ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = alertText
            )
        }

        // 2. Healthcare Safety Check: Medical Advice / Prescription Refusal
        val medicalAdviceKeywords = listOf(
            "which medicine", "give medicine", "what medicine", "prescribe", "tablets for fever", "dosage", "cure", "diagnose",
            "दवाई बताएं", "कौन सी दवा लूं", "इलाज बताओ", "गोली बताओ"
        )
        if (medicalAdviceKeywords.any { trimmed.contains(it, ignoreCase = true) }) {
            val disclaimer = if (language == Language.HINDI) {
                "क्लिनिकफ्लो एआई केवल अपॉइंटमेंट बुकिंग और कतार प्रबंधन में सहायता करता है। मैं डॉक्टर नहीं हूँ और दवा या चिकित्सीय सलाह नहीं दे सकता। कृपया उचित निदान और उपचार के लिए हमारे किसी सत्यापित डॉक्टर से परामर्श लें।"
            } else {
                "ClinicFlow AI assists with appointments and queue navigation. I am not a physician and cannot provide medical advice, diagnosis, or prescriptions. Please book a consultation with one of our verified doctors for professional care."
            }
            return@withContext ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = disclaimer,
                actionCtaText = if (language == Language.HINDI) "डॉक्टर खोजें" else "Find a Doctor",
                actionCtaType = "SEARCH_DOCTOR"
            )
        }

        // 3. Process with backend functions
        if (isGeminiKeyConfigured()) {
            try {
                return@withContext callGeminiApiWithTools(trimmed, currentRole, language, currentUserId)
            } catch (_: Exception) {
                // Fall back gracefully to local intelligent tool execution
            }
        }

        // Local Deterministic ClinicFlow AI Engine
        return@withContext executeLocalAiIntent(trimmed, currentRole, language, currentUserId)
    }

    private fun executeLocalAiIntent(
        prompt: String,
        currentRole: UserRole,
        language: Language,
        currentUserId: String
    ): ChatMessage {
        val lower = prompt.lowercase(Locale.ROOT)

        // A. Staff Copilot: Doctor Schedule Summary
        if (currentRole == UserRole.DOCTOR && (lower.contains("summary") || lower.contains("schedule") || lower.contains("today") || lower.contains("कतार") || lower.contains("शेड्यूल"))) {
            val doctor = repository.doctors.value.first()
            val queue = repository.queue.value.filter { it.doctorId == doctor.id && it.date == todayDate }
            val completed = queue.count { it.status == QueueStatus.COMPLETED }
            val waiting = queue.count { it.status == QueueStatus.WAITING || it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.BOOKED }
            val inConsult = queue.find { it.status == QueueStatus.IN_CONSULTATION }

            val text = if (language == Language.HINDI) {
                "डॉ. ${doctor.name} के लिए आज का सारांश:\n• कुल मरीज: ${queue.size}\n• परामर्श संपन्न: $completed\n• प्रतीक्षारत: $waiting\n• वर्तमान परामर्श: ${inConsult?.patientName ?: "कोई नहीं"} (टोकन: ${inConsult?.tokenNumber ?: "—"})\n\nक्या आप अगले मरीज को बुलाना चाहते हैं या देरी की घोषणा करना चाहते हैं?"
            } else {
                "Today's Practice Summary for ${doctor.name}:\n• Total patients scheduled: ${queue.size}\n• Completed: $completed\n• Waiting in queue: $waiting\n• Currently in consultation: ${inConsult?.patientName ?: "None"} (Token: ${inConsult?.tokenNumber ?: "—"})\n\nWould you like me to draft a delay advisory or call the next patient?"
            }
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = text
            )
        }

        // B. Staff Copilot: Draft Delay Notification
        if ((currentRole == UserRole.DOCTOR || currentRole == UserRole.RECEPTIONIST) && (lower.contains("delay") || lower.contains("देरी") || lower.contains("late"))) {
            val doctor = repository.doctors.value.first()
            val waitingCount = repository.queue.value.count {
                it.doctorId == doctor.id && it.date == todayDate &&
                    (it.status == QueueStatus.WAITING || it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.BOOKED)
            }
            val draft = DraftDelayNotification(
                doctorId = doctor.id,
                doctorName = doctor.name,
                minutes = 20,
                reason = "Doctor attending an urgent inpatient review",
                affectedPatientCount = waitingCount
            )
            val text = if (language == Language.HINDI) {
                "मैंने ~20 मिनट की देरी का ड्राफ्ट तैयार किया है। इससे आज के $waitingCount प्रतीक्षारत मरीजों को सूचना मिलेगी। क्या आप इसे प्रसारित करना चाहते हैं?"
            } else {
                "I have drafted a 20-minute delay notification due to an urgent review. This will update waiting times for $waitingCount patients in queue. Review and broadcast below:"
            }
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = text,
                draftNotification = draft
            )
        }

        // C. Patient: Personal Queue & Token Lookup ("Mera token kya hai?", "Track my queue", "Wait time")
        if (lower.contains("queue") || lower.contains("token") || lower.contains("wait") || lower.contains("टोकन") || lower.contains("कतार") || lower.contains("प्रतीक्षा")) {
            val activeAppointment = repository.appointments.value.firstOrNull {
                it.patientId == currentUserId && it.status != QueueStatus.COMPLETED && it.status != QueueStatus.CANCELLED
            }
            if (activeAppointment != null) {
                val estimate = repository.getQueueEstimate(activeAppointment.tokenNumber, activeAppointment.doctorId)
                val text = if (language == Language.HINDI) {
                    "आपका टोकन नंबर ${estimate.userToken} है। क्लिनिक में इस समय डॉक्टर टोकन नंबर ${estimate.currentServingToken ?: "—"} देख रहे हैं। आपसे पहले ${estimate.patientsAhead} मरीज कतार में हैं। अनुमानित प्रतीक्षा समय लगभग ${estimate.estimatedWaitMinutes} मिनट है।"
                } else {
                    "Your token is ${estimate.userToken}. The clinic is currently serving ${estimate.currentServingToken ?: "—"}. There are ${estimate.patientsAhead} patients ahead of you. Estimated waiting time is approximately ${estimate.estimatedWaitMinutes} minutes."
                }
                return ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    sender = MessageSender.AI,
                    text = text,
                    queueStatusCard = estimate,
                    actionCtaText = if (language == Language.HINDI) "लाइव कतार देखें" else "Open Live Queue Tracker",
                    actionCtaType = "QUEUE",
                    targetToken = estimate.userToken,
                    targetDoctorId = activeAppointment.doctorId
                )
            } else {
                val text = if (language == Language.HINDI) {
                    "आपके खाते में आज कोई सक्रिय अपॉइंटमेंट या टोकन दर्ज नहीं है। क्या आप किसी डॉक्टर से अपॉइंटमेंट बुक करना चाहते हैं?"
                } else {
                    "You do not have an active appointment or queue token scheduled for today. Would you like to find a doctor and book an appointment?"
                }
                return ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    sender = MessageSender.AI,
                    text = text,
                    actionCtaText = if (language == Language.HINDI) "डॉक्टर खोजें" else "Find a Doctor",
                    actionCtaType = "SEARCH_DOCTOR"
                )
            }
        }

        // D. Patient: Appointment Details Lookup ("Mera appointment kab hai?", "My appointments")
        if (lower.contains("mera appointment") || lower.contains("my appointment") || lower.contains("kab hai") || lower.contains("booking")) {
            val appointments = repository.appointments.value.filter { it.patientId == currentUserId }
            val next = appointments.firstOrNull { it.status != QueueStatus.COMPLETED && it.status != QueueStatus.CANCELLED }
            if (next != null) {
                val text = if (language == Language.HINDI) {
                    "आपकी आगामी अपॉइंटमेंट ${next.doctorName} (${next.doctorSpeciality}) के साथ ${next.date} को ${next.time} पर ${next.clinicName} में तय है। टोकन नंबर: ${next.tokenNumber} (संदर्भ: ${next.bookingRef})।"
                } else {
                    "Your upcoming appointment is with ${next.doctorName} (${next.doctorSpeciality}) on ${next.date} at ${next.time} at ${next.clinicName}. Your Token Number is ${next.tokenNumber} (Ref: ${next.bookingRef})."
                }
                return ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    sender = MessageSender.AI,
                    text = text,
                    appointmentCard = next,
                    actionCtaText = if (language == Language.HINDI) "कतार ट्रैक करें" else "Track My Queue",
                    actionCtaType = "QUEUE",
                    targetToken = next.tokenNumber,
                    targetDoctorId = next.doctorId
                )
            } else {
                val text = if (language == Language.HINDI) {
                    "आपके पास वर्तमान में कोई आगामी अपॉइंटमेंट नहीं है। आप किसी भी समय डॉक्टर खोजकर स्लॉट चुन सकते हैं।"
                } else {
                    "You currently do not have any upcoming appointments. Would you like to browse doctors and book a slot?"
                }
                return ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    sender = MessageSender.AI,
                    text = text,
                    actionCtaText = if (language == Language.HINDI) "अपॉइंटमेंट बुक करें" else "Book Appointment",
                    actionCtaType = "SEARCH_DOCTOR"
                )
            }
        }

        // E. Natural-Language Doctor Search by Speciality, Fee, or Location
        val doctors = repository.doctors.value.filter { it.isVerified }
        val specialitiesMap = mapOf(
            "general physician" to "General Physician",
            "physician" to "General Physician",
            "fever" to "General Physician",
            "cold" to "General Physician",
            "बुखार" to "General Physician",
            "डॉक्टर" to "General Physician",
            "cardiologist" to "Cardiologist",
            "heart" to "Cardiologist",
            "दिल" to "Cardiologist",
            "हृदय" to "Cardiologist",
            "pediatrician" to "Pediatrician",
            "child" to "Pediatrician",
            "baby" to "Pediatrician",
            "बच्चा" to "Pediatrician",
            "शिशु" to "Pediatrician",
            "orthopedic" to "Orthopedic",
            "bone" to "Orthopedic",
            "joint" to "Orthopedic",
            "हड्डी" to "Orthopedic",
            "dentist" to "Dentist",
            "teeth" to "Dentist",
            "tooth" to "Dentist",
            "दांत" to "Dentist",
            "gynecologist" to "Gynecologist",
            "women" to "Gynecologist",
            "महिला" to "Gynecologist",
            "dermatologist" to "Dermatologist",
            "skin" to "Dermatologist",
            "त्वचा" to "Dermatologist"
        )

        var matchedSpec: String? = null
        for ((key, spec) in specialitiesMap) {
            if (lower.contains(key)) {
                matchedSpec = spec
                break
            }
        }

        val maxFee = when {
            lower.contains("500") -> 500
            lower.contains("600") -> 600
            lower.contains("700") -> 700
            lower.contains("400") -> 400
            lower.contains("sasta") || lower.contains("budget") || lower.contains("affordable") -> 500
            else -> null
        }

        val locationQuery = when {
            lower.contains("indiranagar") -> "Indiranagar"
            lower.contains("koramangala") -> "Koramangala"
            lower.contains("malleshwaram") -> "Malleshwaram"
            else -> null
        }

        var matchingDocs = doctors
        if (matchedSpec != null) {
            matchingDocs = matchingDocs.filter { it.speciality.equals(matchedSpec, ignoreCase = true) }
        }
        if (maxFee != null) {
            matchingDocs = matchingDocs.filter { it.consultationFee <= maxFee }
        }
        if (locationQuery != null) {
            matchingDocs = matchingDocs.filter {
                it.clinicName.contains(locationQuery, ignoreCase = true) || it.clinicAddress.contains(locationQuery, ignoreCase = true)
            }
        }

        if (matchingDocs.isNotEmpty()) {
            val topDoc = matchingDocs.first()
            val availableSlots = repository.timeSlots.value.filter { it.doctorId == topDoc.id && !it.isBooked && it.date == todayDate }.take(4)
            val slotsText = if (availableSlots.isNotEmpty()) {
                " Available today: " + availableSlots.joinToString { it.time }
            } else ""

            val text = if (language == Language.HINDI) {
                "मुझे ${matchingDocs.size} सत्यापित डॉक्टर मिले हैं। सबसे उपयुक्त: ${topDoc.name} (${topDoc.specialityHindi}), शुल्क: ₹${topDoc.consultationFee}, क्लिनिक: ${topDoc.clinicName}।$slotsText"
            } else {
                "I found ${matchingDocs.size} verified doctor(s) matching your request. Best match: ${topDoc.name} (${topDoc.speciality}), Fee: ₹${topDoc.consultationFee} at ${topDoc.clinicName}.$slotsText"
            }
            return ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = MessageSender.AI,
                text = text,
                suggestedDoctors = matchingDocs.take(3),
                suggestedSlots = availableSlots,
                actionCtaText = if (language == Language.HINDI) "अपॉइंटमेंट बुक करें" else "Book Appointment",
                actionCtaType = "BOOK",
                targetDoctorId = topDoc.id
            )
        }

        // F. General Assistance & Elderly Guidance
        val defaultText = if (language == Language.HINDI) {
            "नमस्ते! मैं क्लिनिकफ्लो एआई हूँ। मैं आपको डॉक्टर खोजने, खाली समय स्लॉट देखने, अपॉइंटमेंट दर्ज करने और लाइव टोकन कतार ट्रैक करने में मदद कर सकता हूँ। आप मुझसे क्या पूछना चाहते हैं?"
        } else {
            "Hello! I am ClinicFlow AI. I can help you find verified doctors by speciality or budget, check available time slots, book an appointment, and track your live token and estimated wait time. How can I assist you today?"
        }
        return ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = defaultText
        )
    }

    private suspend fun callGeminiApiWithTools(
        prompt: String,
        currentRole: UserRole,
        language: Language,
        currentUserId: String
    ): ChatMessage {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val systemInstruction = """
            You are ClinicFlow AI, a trustworthy, friendly, and respectful healthcare appointment and smart queue management assistant for clinics in India.
            You speak fluent, respectful, and simple English and Hindi depending on user preference.
            IMPORTANT HEALTHCARE RULES:
            - You are an administrative assistant, NOT a doctor.
            - NEVER diagnose illnesses, prescribe medications, or recommend drug dosages.
            - If emergency symptoms (chest pain, severe bleeding, breathing distress) are mentioned, direct them immediately to emergency services (112 / 108).
            - Only refer to real verified doctors, clinics, tokens, and actual appointment slots provided by the database.
            - Keep responses concise, clear, and easy to read for senior citizens.
        """.trimIndent()

        val activeApp = repository.appointments.value.firstOrNull {
            it.patientId == currentUserId && it.status != QueueStatus.COMPLETED && it.status != QueueStatus.CANCELLED
        }
        val activeQueueInfo = if (activeApp != null) {
            val est = repository.getQueueEstimate(activeApp.tokenNumber, activeApp.doctorId)
            "Active appointment: ${activeApp.doctorName} at ${activeApp.time}. Token: ${est.userToken}, Now serving: ${est.currentServingToken}, Patients ahead: ${est.patientsAhead}, Wait: ~${est.estimatedWaitMinutes} mins."
        } else "No active appointment."

        val doctorsInfo = repository.doctors.value.take(4).joinToString("; ") {
            "${it.name} (${it.speciality}, ₹${it.consultationFee}, ${it.clinicName})"
        }

        val userContextPrompt = "Current User Role: $currentRole. User Language: ${language.code}. User Context: $activeQueueInfo. Doctors in directory: $doctorsInfo. User says: $prompt"

        val payload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userContextPrompt)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("topP", 0.9)
                put("maxOutputTokens", 512)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini API call failed with code: ${response.code}")
        }

        val responseBody = response.body?.string() ?: throw Exception("Empty response from Gemini")
        val json = JSONObject(responseBody)
        val text = json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")

        if (text.isNullOrBlank()) {
            throw Exception("No text in candidate response")
        }

        return ChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            sender = MessageSender.AI,
            text = text.trim()
        )
    }

    fun confirmDraftDelay(draft: DraftDelayNotification) {
        repository.announceDelay(draft.doctorId, draft.minutes, draft.reason)
    }
}
