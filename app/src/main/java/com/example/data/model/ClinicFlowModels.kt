package com.example.data.model

enum class UserRole {
    PATIENT,
    DOCTOR,
    RECEPTIONIST,
    ADMIN
}

enum class Language(val code: String, val label: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिंदी")
}

enum class TextScale(val scale: Float, val label: String) {
    NORMAL(1.0f, "A"),
    LARGE(1.18f, "A+"),
    EXTRA_LARGE(1.35f, "A++")
}

enum class VerificationStatus {
    VERIFIED,
    PENDING,
    REJECTED,
    SUSPENDED
}

enum class QueueStatus(val labelEn: String, val labelHi: String) {
    BOOKED("Booked", "दर्ज किया गया"),
    CHECKED_IN("Checked In", "उपस्थित"),
    WAITING("Waiting", "प्रतीक्षारत"),
    IN_CONSULTATION("In Consultation", "परामर्श जारी"),
    COMPLETED("Completed", "परामर्श संपन्न"),
    CANCELLED("Cancelled", "रद्द किया गया"),
    NO_SHOW("No Show", "अनुपस्थित")
}

data class Clinic(
    val id: String,
    val name: String,
    val address: String,
    val area: String,
    val city: String,
    val phone: String,
    val openHours: String,
)

data class Doctor(
    val id: String,
    val name: String,
    val speciality: String,
    val specialityHindi: String,
    val qualifications: String,
    val regNumber: String,
    val regCouncil: String,
    val verificationStatus: VerificationStatus,
    val clinicId: String,
    val clinicName: String,
    val clinicAddress: String,
    val consultationFee: Int,
    val avgDurationMinutes: Int,
    val experienceYears: Int,
    val workingHours: String,
    val availableDays: List<String>,
    val phone: String,
    val isDemo: Boolean = true,
) {
    val isVerified: Boolean get() = verificationStatus == VerificationStatus.VERIFIED
}

data class TimeSlot(
    val id: String,
    val doctorId: String,
    val date: String,
    val time: String,
    val isBooked: Boolean,
)

data class QueueEntry(
    val id: String,
    val bookingId: String,
    val doctorId: String,
    val doctorName: String,
    val clinicId: String,
    val clinicName: String,
    val patientName: String,
    val patientPhone: String,
    val patientAge: Int,
    val tokenNumber: String,
    val isWalkIn: Boolean,
    val status: QueueStatus,
    val slotTime: String,
    val date: String,
    val position: Int,
    val checkedInAt: Long? = null,
    val consultationStartedAt: Long? = null,
    val completedAt: Long? = null,
    val delayAnnouncedMinutes: Int = 0,
)

data class Appointment(
    val id: String,
    val bookingRef: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpeciality: String,
    val clinicId: String,
    val clinicName: String,
    val clinicAddress: String,
    val clinicPhone: String,
    val patientId: String,
    val patientName: String,
    val patientPhone: String,
    val patientAge: Int,
    val date: String,
    val time: String,
    val tokenNumber: String,
    val status: QueueStatus,
    val fee: Int,
    val bookedAt: Long,
    val cancellationPolicy: String = "Free cancellation up to 2 hours prior to scheduled appointment slot.",
    val delayMinutes: Int = 0,
)

data class FamilyMember(
    val id: String,
    val patientId: String,
    val name: String,
    val relation: String,
    val relationHindi: String,
    val age: Int,
    val gender: String,
    val phone: String,
)

data class NotificationItem(
    val id: String,
    val userId: String,
    val title: String,
    val titleHindi: String,
    val message: String,
    val messageHindi: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String, // BOOKING, QUEUE_ALERT, DELAY, SYSTEM
)

data class VerificationApplication(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val speciality: String,
    val regNumber: String,
    val regCouncil: String,
    val qualifications: String,
    val clinicName: String,
    val status: VerificationStatus,
    val submittedDate: String,
    val rejectionReason: String? = null,
)

data class AuditLogEntry(
    val id: String,
    val action: String,
    val actorRole: String,
    val actorName: String,
    val details: String,
    val timestamp: Long,
)

data class QueueEstimate(
    val currentServingToken: String?,
    val userToken: String,
    val patientsAhead: Int,
    val estimatedWaitMinutes: Int,
    val status: QueueStatus,
    val doctorName: String,
    val clinicName: String,
    val delayNoticeMinutes: Int,
    val isQueuePaused: Boolean = false,
)
