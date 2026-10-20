package com.example.data.ai

import com.example.data.model.Doctor
import com.example.data.model.QueueEstimate

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class AiSuggestedAction(
    val title: String,
    val prompt: String,
    val actionType: String = "PROMPT", // PROMPT, BOOK_DOCTOR, TRACK_QUEUE
    val payload: String? = null
)

data class DraftDelayNotification(
    val doctorId: String,
    val doctorName: String,
    val clinicName: String,
    val minutes: Int,
    val reason: String,
    val affectedPatientCount: Int
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<AiSuggestedAction> = emptyList(),
    val doctorResults: List<Doctor> = emptyList(),
    val tokenInfo: QueueEstimate? = null,
    val draftDelay: DraftDelayNotification? = null,
    val isEmergency: Boolean = false
)
