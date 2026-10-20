package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class ClinicFlowRepository private constructor() {

    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val tomorrowDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
        Date(System.currentTimeMillis() + 86400000L)
    )

    private val _clinics = MutableStateFlow<List<Clinic>>(emptyList())
    val clinics: StateFlow<List<Clinic>> = _clinics.asStateFlow()

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow()

    private val _timeSlots = MutableStateFlow<List<TimeSlot>>(emptyList())
    val timeSlots: StateFlow<List<TimeSlot>> = _timeSlots.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _queue = MutableStateFlow<List<QueueEntry>>(emptyList())
    val queue: StateFlow<List<QueueEntry>> = _queue.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    private val _verificationApplications = MutableStateFlow<List<VerificationApplication>>(emptyList())
    val verificationApplications: StateFlow<List<VerificationApplication>> = _verificationApplications.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _doctorDelayMinutes = MutableStateFlow<Map<String, Int>>(emptyMap())
    val doctorDelayMinutes: StateFlow<Map<String, Int>> = _doctorDelayMinutes.asStateFlow()

    private val _doctorQueuePaused = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val doctorQueuePaused: StateFlow<Map<String, Boolean>> = _doctorQueuePaused.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        // 3 Real clinics in Bangalore
        val clinicList = listOf(
            Clinic(
                id = "c1",
                name = "Metro Care Health Clinic",
                address = "14, 100ft Road, HAL 2nd Stage, Indiranagar",
                area = "Indiranagar",
                city = "Bengaluru",
                phone = "+91 80 2520 1199",
                openHours = "08:30 AM - 08:30 PM"
            ),
            Clinic(
                id = "c2",
                name = "Apex Multi-Speciality Clinic",
                address = "88, 5th Block, Near Jyoti Nivas College, Koramangala",
                area = "Koramangala",
                city = "Bengaluru",
                phone = "+91 80 4112 3450",
                openHours = "09:00 AM - 09:00 PM"
            ),
            Clinic(
                id = "c3",
                name = "Sanjeevani Family Healthcare",
                address = "42, Sampige Road, 7th Cross, Malleshwaram",
                area = "Malleshwaram",
                city = "Bengaluru",
                phone = "+91 80 2334 8877",
                openHours = "08:00 AM - 08:00 PM"
            )
        )
        _clinics.value = clinicList

        // 6 Verified Doctors + 1 Pending
        val doctorList = listOf(
            Doctor(
                id = "d1",
                name = "Dr. Priya Deshmukh",
                speciality = "Cardiologist",
                specialityHindi = "हृदय रोग विशेषज्ञ",
                qualifications = "MBBS, MD (Medicine), DM (Cardiology)",
                regNumber = "KMC-48291",
                regCouncil = "Karnataka Medical Council",
                verificationStatus = VerificationStatus.VERIFIED,
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                clinicAddress = "Indiranagar, Bengaluru",
                consultationFee = 600,
                avgDurationMinutes = 15,
                experienceYears = 14,
                workingHours = "10:00 AM - 02:00 PM, 05:00 PM - 08:00 PM",
                availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
                phone = "+91 80 2520 1199"
            ),
            Doctor(
                id = "d2",
                name = "Dr. Rajesh K. Sundaram",
                speciality = "General Physician",
                specialityHindi = "सामान्य चिकित्सक",
                qualifications = "MBBS, DNB (Internal Medicine)",
                regNumber = "KMC-39102",
                regCouncil = "Karnataka Medical Council",
                verificationStatus = VerificationStatus.VERIFIED,
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                clinicAddress = "Indiranagar, Bengaluru",
                consultationFee = 400,
                avgDurationMinutes = 12,
                experienceYears = 18,
                workingHours = "09:00 AM - 01:00 PM, 04:30 PM - 07:30 PM",
                availableDays = listOf("Mon", "Wed", "Thu", "Fri", "Sat"),
                phone = "+91 80 2520 1199"
            ),
            Doctor(
                id = "d3",
                name = "Dr. Ananya Sen",
                speciality = "Pediatrician",
                specialityHindi = "शिशु रोग विशेषज्ञ",
                qualifications = "MBBS, MD (Pediatrics), FIAP",
                regNumber = "KMC-51820",
                regCouncil = "Karnataka Medical Council",
                verificationStatus = VerificationStatus.VERIFIED,
                clinicId = "c2",
                clinicName = "Apex Multi-Speciality Clinic",
                clinicAddress = "Koramangala, Bengaluru",
                consultationFee = 500,
                avgDurationMinutes = 15,
                experienceYears = 11,
                workingHours = "10:30 AM - 02:30 PM, 05:30 PM - 08:30 PM",
                availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                phone = "+91 80 4112 3450"
            ),
            Doctor(
                id = "d4",
                name = "Dr. Vikram Joshi",
                speciality = "Orthopedic",
                specialityHindi = "हड्डी रोग विशेषज्ञ",
                qualifications = "MBBS, MS (Orthopaedics), MCh",
                regNumber = "KMC-34789",
                regCouncil = "Karnataka Medical Council",
                verificationStatus = VerificationStatus.VERIFIED,
                clinicId = "c2",
                clinicName = "Apex Multi-Speciality Clinic",
                clinicAddress = "Koramangala, Bengaluru",
                consultationFee = 700,
                avgDurationMinutes = 20,
                experienceYears = 16,
                workingHours = "11:00 AM - 03:00 PM",
                availableDays = listOf("Mon", "Tue", "Thu", "Fri", "Sat"),
                phone = "+91 80 4112 3450"
            ),
            Doctor(
                id = "d5",
                name = "Dr. Sneha Hegde",
                speciality = "Dentist",
                specialityHindi = "दंत चिकित्सक",
                qualifications = "BDS, MDS (Endodontics)",
                regNumber = "KDC-19842",
                regCouncil = "Karnataka State Dental Council",
                verificationStatus = VerificationStatus.VERIFIED,
                clinicId = "c3",
                clinicName = "Sanjeevani Family Healthcare",
                clinicAddress = "Malleshwaram, Bengaluru",
                consultationFee = 350,
                avgDurationMinutes = 20,
                experienceYears = 9,
                workingHours = "09:30 AM - 01:30 PM, 04:00 PM - 07:30 PM",
                availableDays = listOf("Mon", "Tue", "Wed", "Fri", "Sat"),
                phone = "+91 80 2334 8877"
            ),
            Doctor(
                id = "d6",
                name = "Dr. Manjula Rao",
                speciality = "Gynecologist",
                specialityHindi = "स्त्री रोग विशेषज्ञ",
                qualifications = "MBBS, MS (OBG), DGO",
                regNumber = "KMC-41238",
                regCouncil = "Karnataka Medical Council",
                verificationStatus = VerificationStatus.VERIFIED,
                clinicId = "c3",
                clinicName = "Sanjeevani Family Healthcare",
                clinicAddress = "Malleshwaram, Bengaluru",
                consultationFee = 650,
                avgDurationMinutes = 15,
                experienceYears = 20,
                workingHours = "10:00 AM - 02:00 PM",
                availableDays = listOf("Mon", "Wed", "Fri", "Sat"),
                phone = "+91 80 2334 8877"
            )
        )
        _doctors.value = doctorList

        // Time slots for today and tomorrow
        val slotTimes = listOf(
            "10:00 AM", "10:20 AM", "10:40 AM", "11:00 AM", "11:20 AM", "11:40 AM",
            "12:00 PM", "12:20 PM", "05:00 PM", "05:20 PM", "05:40 PM", "06:00 PM"
        )
        val slots = mutableListOf<TimeSlot>()
        doctorList.forEach { doc ->
            listOf(todayDate, tomorrowDate).forEach { date ->
                slotTimes.forEachIndexed { idx, time ->
                    // Make some slots booked and some open
                    val isBooked = (doc.id == "d1" && date == todayDate && idx < 5)
                    slots.add(
                        TimeSlot(
                            id = "slot_${doc.id}_${date}_$idx",
                            doctorId = doc.id,
                            date = date,
                            time = time,
                            isBooked = isBooked
                        )
                    )
                }
            }
        }
        _timeSlots.value = slots

        // Seeded Queue for Dr. Priya Deshmukh (d1) today:
        // Now serving A003, A004 waiting, A005 waiting, Ramesh Sharma has A006
        val initialQueue = listOf(
            QueueEntry(
                id = "q_1",
                bookingId = "b_1",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                patientName = "Suresh Patel",
                patientPhone = "+91 98450 12341",
                patientAge = 62,
                tokenNumber = "A001",
                isWalkIn = false,
                status = QueueStatus.COMPLETED,
                slotTime = "10:00 AM",
                date = todayDate,
                position = 1,
                completedAt = System.currentTimeMillis() - 2400000L
            ),
            QueueEntry(
                id = "q_2",
                bookingId = "b_2",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                patientName = "Kavita Reddy",
                patientPhone = "+91 98450 23452",
                patientAge = 45,
                tokenNumber = "A002",
                isWalkIn = false,
                status = QueueStatus.COMPLETED,
                slotTime = "10:20 AM",
                date = todayDate,
                position = 2,
                completedAt = System.currentTimeMillis() - 1200000L
            ),
            QueueEntry(
                id = "q_3",
                bookingId = "b_3",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                patientName = "Mohan Lal",
                patientPhone = "+91 98450 34563",
                patientAge = 71,
                tokenNumber = "A003",
                isWalkIn = false,
                status = QueueStatus.IN_CONSULTATION,
                slotTime = "10:40 AM",
                date = todayDate,
                position = 3,
                consultationStartedAt = System.currentTimeMillis() - 300000L
            ),
            QueueEntry(
                id = "q_4",
                bookingId = "b_4",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                patientName = "Deepak Verma",
                patientPhone = "+91 98450 45674",
                patientAge = 38,
                tokenNumber = "W001",
                isWalkIn = true,
                status = QueueStatus.WAITING,
                slotTime = "Walk-in 11:00 AM",
                date = todayDate,
                position = 4,
                checkedInAt = System.currentTimeMillis() - 900000L
            ),
            QueueEntry(
                id = "q_5",
                bookingId = "b_5",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                patientName = "Sunita Rao",
                patientPhone = "+91 98450 56785",
                patientAge = 54,
                tokenNumber = "A005",
                isWalkIn = false,
                status = QueueStatus.CHECKED_IN,
                slotTime = "11:20 AM",
                date = todayDate,
                position = 5,
                checkedInAt = System.currentTimeMillis() - 600000L
            ),
            QueueEntry(
                id = "q_6",
                bookingId = "b_6",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                patientName = "Ramesh Sharma",
                patientPhone = "+91 98450 99887",
                patientAge = 67,
                tokenNumber = "A006",
                isWalkIn = false,
                status = QueueStatus.BOOKED,
                slotTime = "11:40 AM",
                date = todayDate,
                position = 6
            )
        )
        _queue.value = initialQueue

        // Seeded Appointment for current patient (Ramesh Sharma)
        val initialAppointments = listOf(
            Appointment(
                id = "b_6",
                bookingRef = "CF-928172",
                doctorId = "d1",
                doctorName = "Dr. Priya Deshmukh",
                doctorSpeciality = "Cardiologist",
                clinicId = "c1",
                clinicName = "Metro Care Health Clinic",
                clinicAddress = "14, 100ft Road, HAL 2nd Stage, Indiranagar, Bengaluru",
                clinicPhone = "+91 80 2520 1199",
                patientId = "patient_1",
                patientName = "Ramesh Sharma",
                patientPhone = "+91 98450 99887",
                patientAge = 67,
                date = todayDate,
                time = "11:40 AM",
                tokenNumber = "A006",
                status = QueueStatus.BOOKED,
                fee = 600,
                bookedAt = System.currentTimeMillis() - 7200000L
            )
        )
        _appointments.value = initialAppointments

        // Family members
        _familyMembers.value = listOf(
            FamilyMember(
                id = "fm_1",
                patientId = "patient_1",
                name = "Kanti Devi",
                relation = "Mother",
                relationHindi = "माताजी",
                age = 68,
                gender = "Female",
                phone = "+91 98450 99880"
            ),
            FamilyMember(
                id = "fm_2",
                patientId = "patient_1",
                name = "Aarav Sharma",
                relation = "Son",
                relationHindi = "बेटा",
                age = 9,
                gender = "Male",
                phone = "+91 98450 99887"
            )
        )

        // Pending Doctor Verification Application for Platform Admin review
        _verificationApplications.value = listOf(
            VerificationApplication(
                id = "app_1",
                doctorId = "d_pending_1",
                doctorName = "Dr. Alok Verma",
                speciality = "Dermatologist",
                regNumber = "KMC-62941",
                regCouncil = "Karnataka Medical Council",
                qualifications = "MBBS, MD (Dermatology, Venereology & Leprosy)",
                clinicName = "Skin & Laser Wellness Clinic, HSR Layout",
                status = VerificationStatus.PENDING,
                submittedDate = todayDate
            )
        )

        // Initial Notifications
        _notifications.value = listOf(
            NotificationItem(
                id = "notif_1",
                userId = "patient_1",
                title = "Upcoming Appointment Today",
                titleHindi = "आज का आगामी अपॉइंटमेंट",
                message = "Your appointment with Dr. Priya Deshmukh is confirmed for 11:40 AM. Token #A006. Track your turn live on the app.",
                messageHindi = "डॉ. प्रिया देशमुख के साथ 11:40 AM पर आपकी अपॉइंटमेंट दर्ज है। टोकन #A006। लाइव कतार ऐप में देखें।",
                timestamp = System.currentTimeMillis() - 3600000L,
                type = "BOOKING"
            ),
            NotificationItem(
                id = "notif_2",
                userId = "patient_1",
                title = "Clinic Advisory",
                titleHindi = "क्लिनिक सूचना",
                message = "Metro Care Clinic has convenient elevator access and elderly assistance at Ground Floor desk.",
                messageHindi = "मेट्रो केयर क्लिनिक में भूतल पर लिफ्ट और वरिष्ठ नागरिकों के लिए सहायता डेस्क उपलब्ध है।",
                timestamp = System.currentTimeMillis() - 7200000L,
                type = "SYSTEM"
            )
        )

        // Initial audit log
        _auditLogs.value = listOf(
            AuditLogEntry(
                id = "log_1",
                action = "QUEUE_INITIALIZED",
                actorRole = "SYSTEM",
                actorName = "ClinicFlow Engine",
                details = "Daily schedule and token queue created for Metro Care Clinic",
                timestamp = System.currentTimeMillis() - 14400000L
            ),
            AuditLogEntry(
                id = "log_2",
                action = "BOOKING_CREATED",
                actorRole = "PATIENT",
                actorName = "Ramesh Sharma",
                details = "Booked slot 11:40 AM with Dr. Priya Deshmukh, Token A006",
                timestamp = System.currentTimeMillis() - 7200000L
            )
        )
    }

    // Appointment Booking Flow
    @Synchronized
    fun bookAppointment(
        doctorId: String,
        clinicId: String,
        date: String,
        time: String,
        patientName: String,
        patientPhone: String,
        patientAge: Int,
        patientId: String = "patient_1"
    ): Result<Appointment> {
        val doctor = _doctors.value.find { it.id == doctorId }
            ?: return Result.failure(Exception("Doctor not found"))
        val clinic = _clinics.value.find { it.id == clinicId }
            ?: return Result.failure(Exception("Clinic not found"))

        // Conflict check
        val currentSlots = _timeSlots.value
        val slotIndex = currentSlots.indexOfFirst { it.doctorId == doctorId && it.date == date && it.time == time }
        if (slotIndex >= 0 && currentSlots[slotIndex].isBooked) {
            return Result.failure(Exception("This slot was just booked by another patient. Please choose another time."))
        }

        // Generate Token: e.g. A001, A002...
        val existingToday = _queue.value.filter { it.doctorId == doctorId && it.date == date }
        val nextTokenNum = existingToday.size + 1
        val token = "A" + String.format(Locale.US, "%03d", nextTokenNum)
        val bookingRef = "CF-" + (100000 + Random().nextInt(900000))

        val newAppointment = Appointment(
            id = "b_${System.currentTimeMillis()}",
            bookingRef = bookingRef,
            doctorId = doctor.id,
            doctorName = doctor.name,
            doctorSpeciality = doctor.speciality,
            clinicId = clinic.id,
            clinicName = clinic.name,
            clinicAddress = clinic.address,
            clinicPhone = clinic.phone,
            patientId = patientId,
            patientName = patientName,
            patientPhone = patientPhone,
            patientAge = patientAge,
            date = date,
            time = time,
            tokenNumber = token,
            status = QueueStatus.BOOKED,
            fee = doctor.consultationFee,
            bookedAt = System.currentTimeMillis()
        )

        val newQueueEntry = QueueEntry(
            id = "q_${System.currentTimeMillis()}",
            bookingId = newAppointment.id,
            doctorId = doctor.id,
            doctorName = doctor.name,
            clinicId = clinic.id,
            clinicName = clinic.name,
            patientName = patientName,
            patientPhone = patientPhone,
            patientAge = patientAge,
            tokenNumber = token,
            isWalkIn = false,
            status = QueueStatus.BOOKED,
            slotTime = time,
            date = date,
            position = nextTokenNum
        )

        // Mark slot as booked
        if (slotIndex >= 0) {
            val updatedSlots = currentSlots.toMutableList()
            updatedSlots[slotIndex] = updatedSlots[slotIndex].copy(isBooked = true)
            _timeSlots.value = updatedSlots
        }

        _appointments.value = listOf(newAppointment) + _appointments.value
        _queue.value = _queue.value + newQueueEntry

        // Notification
        val notif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            userId = patientId,
            title = "Appointment Booked: Token $token",
            titleHindi = "अपॉइंटमेंट दर्ज: टोकन $token",
            message = "Your appointment with ${doctor.name} on $date at $time is confirmed. Token #$token.",
            messageHindi = "${doctor.name} के साथ $date को $time पर आपकी अपॉइंटमेंट दर्ज हो गई है। टोकन #$token।",
            timestamp = System.currentTimeMillis(),
            type = "BOOKING"
        )
        _notifications.value = listOf(notif) + _notifications.value

        logAudit("BOOKING_CREATED", "PATIENT", patientName, "Booked $time on $date with ${doctor.name}. Token: $token")
        return Result.success(newAppointment)
    }

    // Register Walk-in patient (by Receptionist)
    @Synchronized
    fun registerWalkIn(
        doctorId: String,
        clinicId: String,
        patientName: String,
        patientPhone: String,
        patientAge: Int
    ): QueueEntry {
        val doctor = _doctors.value.find { it.id == doctorId } ?: _doctors.value.first()
        val clinic = _clinics.value.find { it.id == clinicId } ?: _clinics.value.first()

        val existingToday = _queue.value.filter { it.doctorId == doctorId && it.date == todayDate }
        val walkInCount = existingToday.count { it.isWalkIn } + 1
        val token = "W" + String.format(Locale.US, "%03d", walkInCount)

        val queueEntry = QueueEntry(
            id = "q_walkin_${System.currentTimeMillis()}",
            bookingId = "walkin_${System.currentTimeMillis()}",
            doctorId = doctor.id,
            doctorName = doctor.name,
            clinicId = clinic.id,
            clinicName = clinic.name,
            patientName = patientName,
            patientPhone = patientPhone,
            patientAge = patientAge,
            tokenNumber = token,
            isWalkIn = true,
            status = QueueStatus.WAITING,
            slotTime = "Walk-in",
            date = todayDate,
            position = existingToday.size + 1,
            checkedInAt = System.currentTimeMillis()
        )

        _queue.value = _queue.value + queueEntry
        logAudit("WALK_IN_REGISTERED", "RECEPTIONIST", "Clinic Reception", "Registered walk-in $patientName for ${doctor.name}. Token: $token")
        return queueEntry
    }

    // Queue updates
    @Synchronized
    fun checkInPatient(queueEntryId: String) {
        val updated = _queue.value.map { entry ->
            if (entry.id == queueEntryId) {
                entry.copy(status = QueueStatus.CHECKED_IN, checkedInAt = System.currentTimeMillis())
            } else entry
        }
        _queue.value = updated

        // Sync with appointment
        val target = updated.find { it.id == queueEntryId }
        if (target != null) {
            _appointments.value = _appointments.value.map { app ->
                if (app.id == target.bookingId) app.copy(status = QueueStatus.CHECKED_IN) else app
            }
            logAudit("CHECKED_IN", "RECEPTIONIST", "Clinic Reception", "Checked in ${target.patientName} (Token ${target.tokenNumber})")
        }
    }

    @Synchronized
    fun callNextPatient(doctorId: String): QueueEntry? {
        val doctorQueue = _queue.value.filter { it.doctorId == doctorId && it.date == todayDate }
        // Find next eligible (CHECKED_IN or BOOKED)
        val nextCandidate = doctorQueue.firstOrNull { it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.WAITING || it.status == QueueStatus.BOOKED }
            ?: return null

        val updated = _queue.value.map { entry ->
            if (entry.id == nextCandidate.id) {
                entry.copy(status = QueueStatus.WAITING)
            } else entry
        }
        _queue.value = updated

        _appointments.value = _appointments.value.map { app ->
            if (app.id == nextCandidate.bookingId) app.copy(status = QueueStatus.WAITING) else app
        }

        logAudit("CALLED_NEXT", "DOCTOR", nextCandidate.doctorName, "Called next patient ${nextCandidate.patientName} (Token ${nextCandidate.tokenNumber})")
        return nextCandidate
    }

    @Synchronized
    fun startConsultation(queueEntryId: String) {
        val target = _queue.value.find { it.id == queueEntryId } ?: return

        // If another was in consultation for this doctor, complete it first
        val updated = _queue.value.map { entry ->
            if (entry.doctorId == target.doctorId && entry.status == QueueStatus.IN_CONSULTATION && entry.id != queueEntryId) {
                entry.copy(status = QueueStatus.COMPLETED, completedAt = System.currentTimeMillis())
            } else if (entry.id == queueEntryId) {
                entry.copy(status = QueueStatus.IN_CONSULTATION, consultationStartedAt = System.currentTimeMillis())
            } else entry
        }
        _queue.value = updated

        _appointments.value = _appointments.value.map { app ->
            if (app.id == target.bookingId) app.copy(status = QueueStatus.IN_CONSULTATION) else app
        }

        logAudit("CONSULTATION_STARTED", "DOCTOR", target.doctorName, "Started consultation for ${target.patientName} (Token ${target.tokenNumber})")
    }

    @Synchronized
    fun completeConsultation(queueEntryId: String) {
        val target = _queue.value.find { it.id == queueEntryId } ?: return

        val updated = _queue.value.map { entry ->
            if (entry.id == queueEntryId) {
                entry.copy(status = QueueStatus.COMPLETED, completedAt = System.currentTimeMillis())
            } else entry
        }
        _queue.value = updated

        _appointments.value = _appointments.value.map { app ->
            if (app.id == target.bookingId) app.copy(status = QueueStatus.COMPLETED) else app
        }

        logAudit("CONSULTATION_COMPLETED", "DOCTOR", target.doctorName, "Completed consultation for ${target.patientName} (Token ${target.tokenNumber})")
    }

    @Synchronized
    fun markNoShow(queueEntryId: String) {
        val target = _queue.value.find { it.id == queueEntryId } ?: return

        val updated = _queue.value.map { entry ->
            if (entry.id == queueEntryId) {
                entry.copy(status = QueueStatus.NO_SHOW)
            } else entry
        }
        _queue.value = updated

        _appointments.value = _appointments.value.map { app ->
            if (app.id == target.bookingId) app.copy(status = QueueStatus.NO_SHOW) else app
        }

        logAudit("MARKED_NO_SHOW", "RECEPTIONIST", "Clinic Reception", "Marked ${target.patientName} (Token ${target.tokenNumber}) as No-Show")
    }

    @Synchronized
    fun cancelAppointment(appointmentId: String, reason: String = "Patient requested") {
        val app = _appointments.value.find { it.id == appointmentId } ?: return
        _appointments.value = _appointments.value.map {
            if (it.id == appointmentId) it.copy(status = QueueStatus.CANCELLED) else it
        }

        // Release slot
        val updatedSlots = _timeSlots.value.map { slot ->
            if (slot.doctorId == app.doctorId && slot.date == app.date && slot.time == app.time) {
                slot.copy(isBooked = false)
            } else slot
        }
        _timeSlots.value = updatedSlots

        // Remove/mark cancelled in queue
        _queue.value = _queue.value.map { entry ->
            if (entry.bookingId == appointmentId) entry.copy(status = QueueStatus.CANCELLED) else entry
        }

        logAudit("APPOINTMENT_CANCELLED", "PATIENT", app.patientName, "Cancelled booking ${app.bookingRef} (Token ${app.tokenNumber}). Reason: $reason")
    }

    @Synchronized
    fun announceDelay(doctorId: String, minutes: Int, message: String) {
        val map = _doctorDelayMinutes.value.toMutableMap()
        map[doctorId] = minutes
        _doctorDelayMinutes.value = map

        val doctor = _doctors.value.find { it.id == doctorId }
        val docName = doctor?.name ?: "Doctor"

        // Broadcast notification to all waiting patients of this doctor today
        val waitingPatients = _queue.value.filter {
            it.doctorId == doctorId && it.date == todayDate &&
                (it.status == QueueStatus.BOOKED || it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.WAITING)
        }

        val newNotifs = waitingPatients.map { p ->
            NotificationItem(
                id = "notif_delay_${System.currentTimeMillis()}_${p.id}",
                userId = "patient_1",
                title = "Queue Delay Update: ~$minutes Mins",
                titleHindi = "कतार में देरी की सूचना: ~$minutes मिनट",
                message = "$docName is running approximately $minutes minutes delayed due to: $message. Please adjust your travel accordingly.",
                messageHindi = "$docName के परामर्श में लगभग $minutes मिनट की देरी हो रही है: $message। तदनुसार क्लिनिक पहुंचें।",
                timestamp = System.currentTimeMillis(),
                type = "DELAY"
            )
        }
        _notifications.value = newNotifs + _notifications.value

        logAudit("DELAY_ANNOUNCED", "DOCTOR", docName, "Announced $minutes minutes delay. Reason: $message")
    }

    @Synchronized
    fun togglePauseQueue(doctorId: String): Boolean {
        val map = _doctorQueuePaused.value.toMutableMap()
        val current = map[doctorId] ?: false
        val next = !current
        map[doctorId] = next
        _doctorQueuePaused.value = map

        val doctor = _doctors.value.find { it.id == doctorId }
        val docName = doctor?.name ?: "Doctor"
        val action = if (next) "PAUSED" else "RESUMED"
        logAudit("QUEUE_$action", "DOCTOR", docName, "Doctor $action queue consultations")
        return next
    }

    // Doctor Verification Workflow (Admin)
    @Synchronized
    fun approveDoctorApplication(applicationId: String, adminName: String) {
        val app = _verificationApplications.value.find { it.id == applicationId } ?: return
        _verificationApplications.value = _verificationApplications.value.map {
            if (it.id == applicationId) it.copy(status = VerificationStatus.VERIFIED) else it
        }

        // Add newly approved doctor to platform directory!
        val newDoc = Doctor(
            id = app.doctorId,
            name = app.doctorName,
            speciality = app.speciality,
            specialityHindi = "त्वचा रोग विशेषज्ञ",
            qualifications = app.qualifications,
            regNumber = app.regNumber,
            regCouncil = app.regCouncil,
            verificationStatus = VerificationStatus.VERIFIED,
            clinicId = "c2",
            clinicName = app.clinicName,
            clinicAddress = "HSR Layout, Bengaluru",
            consultationFee = 550,
            avgDurationMinutes = 15,
            experienceYears = 10,
            workingHours = "10:00 AM - 01:00 PM, 05:00 PM - 08:00 PM",
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            phone = "+91 80 4112 3450"
        )
        if (_doctors.value.none { it.id == newDoc.id }) {
            _doctors.value = _doctors.value + newDoc
            val newSlots = listOf("10:00 AM", "11:00 AM", "12:00 PM", "05:00 PM").mapIndexed { idx, time ->
                TimeSlot(
                    id = "slot_${newDoc.id}_${todayDate}_$idx",
                    doctorId = newDoc.id,
                    date = todayDate,
                    time = time,
                    isBooked = false
                )
            }
            _timeSlots.value = _timeSlots.value + newSlots
        }

        logAudit("DOCTOR_VERIFIED", "ADMIN", adminName, "Approved verification for ${app.doctorName} (${app.regNumber}, ${app.regCouncil})")
    }

    @Synchronized
    fun rejectDoctorApplication(applicationId: String, reason: String, adminName: String) {
        val app = _verificationApplications.value.find { it.id == applicationId } ?: return
        _verificationApplications.value = _verificationApplications.value.map {
            if (it.id == applicationId) it.copy(status = VerificationStatus.REJECTED, rejectionReason = reason) else it
        }
        logAudit("DOCTOR_REJECTED", "ADMIN", adminName, "Rejected verification for ${app.doctorName}. Reason: $reason")
    }

    // Family Member Management
    @Synchronized
    fun addFamilyMember(patientId: String, name: String, relation: String, age: Int, gender: String, phone: String) {
        val relationHi = when (relation.lowercase(Locale.ROOT)) {
            "mother" -> "माताजी"
            "father" -> "पिताजी"
            "spouse", "wife", "husband" -> "जीवनसाथी"
            "son" -> "बेटा"
            "daughter" -> "बेटी"
            else -> relation
        }
        val member = FamilyMember(
            id = "fm_${System.currentTimeMillis()}",
            patientId = patientId,
            name = name,
            relation = relation,
            relationHindi = relationHi,
            age = age,
            gender = gender,
            phone = phone
        )
        _familyMembers.value = _familyMembers.value + member
        logAudit("FAMILY_MEMBER_ADDED", "PATIENT", name, "Added family member $name ($relation)")
    }

    @Synchronized
    fun removeFamilyMember(id: String) {
        val member = _familyMembers.value.find { it.id == id }
        _familyMembers.value = _familyMembers.value.filter { it.id != id }
        if (member != null) {
            logAudit("FAMILY_MEMBER_REMOVED", "PATIENT", member.name, "Removed family member ${member.name}")
        }
    }

    // Queue Estimate Calculation
    fun getQueueEstimate(userToken: String, doctorId: String): QueueEstimate {
        val doctor = _doctors.value.find { it.id == doctorId }
        val docName = doctor?.name ?: "Doctor"
        val clinic = _clinics.value.find { it.id == doctor?.clinicId }
        val clinicName = clinic?.name ?: "Clinic"
        val delay = _doctorDelayMinutes.value[doctorId] ?: 0
        val isPaused = _doctorQueuePaused.value[doctorId] ?: false

        val doctorQueue = _queue.value.filter { it.doctorId == doctorId && it.date == todayDate }
        val currentServing = doctorQueue.find { it.status == QueueStatus.IN_CONSULTATION }
            ?: doctorQueue.find { it.status == QueueStatus.WAITING }

        val userEntry = doctorQueue.find { it.tokenNumber == userToken }
        val userStatus = userEntry?.status ?: QueueStatus.BOOKED

        if (userStatus == QueueStatus.COMPLETED || userStatus == QueueStatus.CANCELLED || userStatus == QueueStatus.NO_SHOW) {
            return QueueEstimate(
                currentServingToken = currentServing?.tokenNumber,
                userToken = userToken,
                patientsAhead = 0,
                estimatedWaitMinutes = 0,
                status = userStatus,
                doctorName = docName,
                clinicName = clinicName,
                delayNoticeMinutes = delay,
                isQueuePaused = isPaused
            )
        }

        // Active queue entries before user
        val activeEntries = doctorQueue.filter {
            it.status == QueueStatus.IN_CONSULTATION || it.status == QueueStatus.WAITING ||
                it.status == QueueStatus.CHECKED_IN || it.status == QueueStatus.BOOKED
        }

        val userIdx = activeEntries.indexOfFirst { it.tokenNumber == userToken }
        val patientsAhead = if (userIdx > 0) userIdx else 0

        val avgDuration = doctor?.avgDurationMinutes ?: 15
        val waitMinutes = (patientsAhead * avgDuration) + delay

        return QueueEstimate(
            currentServingToken = currentServing?.tokenNumber ?: "None",
            userToken = userToken,
            patientsAhead = patientsAhead,
            estimatedWaitMinutes = waitMinutes,
            status = userStatus,
            doctorName = docName,
            clinicName = clinicName,
            delayNoticeMinutes = delay,
            isQueuePaused = isPaused
        )
    }

    private fun logAudit(action: String, actorRole: String, actorName: String, details: String) {
        val entry = AuditLogEntry(
            id = "log_${System.currentTimeMillis()}",
            action = action,
            actorRole = actorRole,
            actorName = actorName,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    companion object {
        @Volatile
        private var instance: ClinicFlowRepository? = null

        fun getInstance(): ClinicFlowRepository {
            return instance ?: synchronized(this) {
                instance ?: ClinicFlowRepository().also { instance = it }
            }
        }
    }
}
