package com.example.data.ai

import com.example.data.model.Appointment
import com.example.data.model.Doctor
import com.example.data.model.QueueEstimate
import com.example.data.model.TimeSlot
import com.example.data.model.UserRole

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class DraftDelayNotification(
    val doctorId: String,
    val doctorName: String,
    val minutes: Int,
    val reason: String,
    val affectedPatientCount: Int,
    val isConfirmed: Boolean = false
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedDoctors: List<Doctor>? = null,
    val suggestedSlots: List<TimeSlot>? = null,
    val appointmentCard: Appointment? = null,
    val queueStatusCard: QueueEstimate? = null,
    val draftNotification: DraftDelayNotification? = null,
    val actionCtaText: String? = null,
    val actionCtaType: String? = null, // "BOOK", "QUEUE", "SEARCH_DOCTOR"
    val targetDoctorId: String? = null,
    val targetToken: String? = null,
)

data class AiQuickAction(
    val labelEn: String,
    val labelHi: String,
    val prompt: String,
    val role: UserRole? = null
)
