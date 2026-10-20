package com.example

import com.example.data.ai.ClinicFlowAiService
import com.example.data.ai.MessageSender
import com.example.data.model.Language
import com.example.data.model.QueueStatus
import com.example.data.model.UserRole
import com.example.data.repository.ClinicFlowRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClinicFlowAiTest {

    private lateinit var repository: ClinicFlowRepository
    private lateinit var aiService: ClinicFlowAiService
    private val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    @Before
    fun setUp() {
        repository = ClinicFlowRepository.getInstance()
        aiService = ClinicFlowAiService(repository)
    }

    @Test
    fun `test English doctor search returns real verified doctors under budget`() = runBlocking {
        val query = "Find a general physician under 600"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Response should contain doctor recommendation", response.doctorResults.isNotEmpty())
        assertTrue("Doctors must be verified", response.doctorResults.all { it.isVerified })
        assertTrue("Doctor consultation fee must respect budget", response.doctorResults.all { it.consultationFee <= 600 })
        assertTrue("Doctor speciality must match general physician", response.doctorResults.all { it.speciality.contains("Physician", ignoreCase = true) })
    }

    @Test
    fun `test Hindi doctor search returns matching records`() = runBlocking {
        val query = "Mujhe general physician chahiye"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.HINDI)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Response should contain doctor recommendation", response.doctorResults.isNotEmpty())
        assertTrue("Doctor must be general physician", response.doctorResults.any { it.speciality.contains("Physician", ignoreCase = true) })
    }

    @Test
    fun `test no matching doctor returns polite message without fabricating records`() = runBlocking {
        val query = "Find a neurosurgeon under 100"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Should not fabricate doctors", response.doctorResults.isEmpty())
        assertTrue("Should explain no matching doctor found", response.text.contains("No verified doctors", ignoreCase = true) || response.text.contains("could not find", ignoreCase = true) || response.text.contains("₹100"))
    }

    @Test
    fun `test emergency query triggers immediate safety emergency warning`() = runBlocking {
        val query = "Patient having severe chest pain and cannot breathe"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Emergency flag must be set", response.isEmergency)
        assertTrue("Must display 112 emergency advice", response.text.contains("112"))
        assertTrue("Must warn user not to wait for routine clinic appointment", response.text.contains("EMERGENCY", ignoreCase = true))
    }

    @Test
    fun `test medical question disclaimer ensures administrative role only`() = runBlocking {
        val query = "What medicine should I take for bacterial pneumonia?"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        // Never autonomously prescribe; fallback clarifies administrative assistant
        assertTrue("Must clarify assistant role", response.text.contains("ClinicFlow", ignoreCase = true))
    }

    @Test
    fun `test queue token query retrieves verified estimate`() = runBlocking {
        val doctor = repository.doctors.value.first()
        val query = "What is my queue status and token?"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.ENGLISH, currentUserId = "patient_1")

        assertEquals(MessageSender.AI, response.sender)
        assertNotNull("Response should provide token info", response.tokenInfo)
        assertTrue("Estimated wait time is labeled as estimate", response.text.contains("estimate", ignoreCase = true) || response.text.contains("अनुमानित", ignoreCase = true))
    }

    @Test
    fun `test patient cannot access staff schedule summary directly`() = runBlocking {
        val query = "Summarise today's appointment schedule"
        val response = aiService.processQuery(query, UserRole.PATIENT, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Should enforce privacy for patient role", response.text.contains("restricted", ignoreCase = true) || response.text.contains("Privacy", ignoreCase = true))
    }

    @Test
    fun `test doctor role can view operational schedule summary with source attribution`() = runBlocking {
        val query = "Summarise today's appointment schedule"
        val response = aiService.processQuery(query, UserRole.DOCTOR, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Should include clinical schedule summary", response.text.contains("Clinical Schedule Summary", ignoreCase = true))
        assertTrue("Must include real data source attribution", response.text.contains("Source:", ignoreCase = true))
    }

    @Test
    fun `test staff copilot can identify genuinely open slots`() = runBlocking {
        val query = "Identify open appointment slots"
        val response = aiService.processQuery(query, UserRole.DOCTOR, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertTrue("Should check open slots", response.text.contains("Open Slots", ignoreCase = true))
        assertTrue("Must state slot inventory source", response.text.contains("Source:", ignoreCase = true))
    }

    @Test
    fun `test doctor role can draft delay notification without automatic sending`() = runBlocking {
        val query = "Doctor running late by 25 minutes due to traffic"
        val response = aiService.processQuery(query, UserRole.DOCTOR, Language.ENGLISH)

        assertEquals(MessageSender.AI, response.sender)
        assertNotNull("Draft delay should be populated for review", response.draftDelay)
        assertEquals(25, response.draftDelay?.minutes)
        assertTrue("Action requires explicit user confirmation before broadcast", response.suggestedActions.any { it.title.contains("Broadcast", ignoreCase = true) })
    }

    @Test
    fun `test user isolation - patient cannot access other patients appointments`() {
        val user1 = "patient_user_1"
        val user2 = "patient_user_2"

        val doc = repository.doctors.value.first()
        val slot1 = repository.timeSlots.value.first { it.doctorId == doc.id && !it.isBooked }
        repository.bookAppointment(
            doctorId = doc.id,
            clinicId = doc.clinicId,
            date = slot1.date,
            time = slot1.time,
            patientName = "User One",
            patientPhone = "+91 99999 11111",
            patientAge = 35,
            patientId = user1
        )

        val user1Apps = aiService.getMyAppointments(user1)
        val user2Apps = aiService.getMyAppointments(user2)

        assertTrue("User 1 should see own appointment", user1Apps.any { it.patientName == "User One" })
        assertTrue("User 2 must NOT see user 1 appointments", user2Apps.none { it.patientName == "User One" })
    }

    @Test
    fun `test cancellation permission enforcement prevents unauthorized deletion`() {
        val user1 = "patient_user_1"
        val user2 = "patient_user_2"

        val doc = repository.doctors.value.first()
        val slot = repository.timeSlots.value.first { it.doctorId == doc.id && !it.isBooked }
        val booking = repository.bookAppointment(
            doctorId = doc.id,
            clinicId = doc.clinicId,
            date = slot.date,
            time = slot.time,
            patientName = "Cancel Security Test",
            patientPhone = "+91 99999 22222",
            patientAge = 40,
            patientId = user1
        ).getOrThrow()

        // User 2 attempts to cancel User 1 appointment
        val unauthorizedResult = aiService.requestCancellationAfterConfirmation(booking.id, user2, UserRole.PATIENT)
        assertTrue("Unauthorized user cannot cancel another patient booking", unauthorizedResult.isFailure)

        // User 1 cancels own appointment
        val authorizedResult = aiService.requestCancellationAfterConfirmation(booking.id, user1, UserRole.PATIENT)
        assertTrue("Authorized patient can cancel own booking", authorizedResult.isSuccess)

        val updated = repository.appointments.value.find { it.id == booking.id }
        assertEquals(QueueStatus.CANCELLED, updated?.status)
    }

    @Test
    fun `test duplicate booking prevention on the same slot`() {
        val doc = repository.doctors.value.first()
        val slot = repository.timeSlots.value.first { it.doctorId == doc.id && !it.isBooked }

        val firstBooking = aiService.createBookingAfterConfirmation(
            doctorId = doc.id,
            clinicId = doc.clinicId,
            date = slot.date,
            time = slot.time,
            patientName = "First Booker",
            patientPhone = "+91 98888 11111",
            patientAge = 30,
            userId = "user_a"
        )
        assertTrue("First booking succeeds", firstBooking.isSuccess)

        val secondBooking = aiService.createBookingAfterConfirmation(
            doctorId = doc.id,
            clinicId = doc.clinicId,
            date = slot.date,
            time = slot.time,
            patientName = "Duplicate Booker",
            patientPhone = "+91 98888 22222",
            patientAge = 32,
            userId = "user_b"
        )
        assertTrue("Duplicate booking on same slot must be rejected", secondBooking.isFailure)
    }
}
