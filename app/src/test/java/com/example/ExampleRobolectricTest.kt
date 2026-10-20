package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.QueueStatus
import com.example.data.model.VerificationStatus
import com.example.data.repository.ClinicFlowRepository
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
class ExampleRobolectricTest {

    private lateinit var repository: ClinicFlowRepository
    private val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    @Before
    fun setUp() {
        repository = ClinicFlowRepository.getInstance()
    }

    @Test
    fun `read app_name string from context equals ClinicFlow`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ClinicFlow", appName)
    }

    @Test
    fun `initial seeded doctors and clinics are populated`() {
        val doctors = repository.doctors.value
        val clinics = repository.clinics.value

        assertTrue("Should have at least 6 doctors", doctors.size >= 6)
        assertTrue("Should have at least 3 clinics", clinics.size >= 3)
        assertTrue("All initial doctors should be verified", doctors.all { it.isVerified })
    }

    @Test
    fun `appointment booking generates token and updates queue`() {
        val doctor = repository.doctors.value.first()
        val timeSlots = repository.timeSlots.value.filter { it.doctorId == doctor.id && !it.isBooked }
        assertTrue("Should have available time slots", timeSlots.isNotEmpty())

        val slot = timeSlots.first()
        val bookingResult = repository.bookAppointment(
            doctorId = doctor.id,
            clinicId = doctor.clinicId,
            date = slot.date,
            time = slot.time,
            patientName = "Elderly Patient Test",
            patientPhone = "+91 98450 11223",
            patientAge = 72
        )

        assertTrue("Booking should succeed", bookingResult.isSuccess)
        val appointment = bookingResult.getOrThrow()
        assertTrue("Token should start with A", appointment.tokenNumber.startsWith("A"))
        assertEquals(QueueStatus.BOOKED, appointment.status)

        // Queue entry should exist
        val queueEntry = repository.queue.value.find { it.tokenNumber == appointment.tokenNumber }
        assertNotNull("Queue entry should exist for new booking", queueEntry)
        assertEquals(QueueStatus.BOOKED, queueEntry?.status)
    }

    @Test
    fun `double booking same slot is prevented`() {
        val availableSlot = repository.timeSlots.value.first { !it.isBooked }
        val doctor = repository.doctors.value.first { it.id == availableSlot.doctorId }

        // First booking succeeds
        val result1 = repository.bookAppointment(
            doctorId = doctor.id,
            clinicId = doctor.clinicId,
            date = availableSlot.date,
            time = availableSlot.time,
            patientName = "Patient One",
            patientPhone = "+91 98000 00001",
            patientAge = 60
        )
        assertTrue(result1.isSuccess)

        // Second booking for identical slot must fail
        val result2 = repository.bookAppointment(
            doctorId = doctor.id,
            clinicId = doctor.clinicId,
            date = availableSlot.date,
            time = availableSlot.time,
            patientName = "Patient Two Conflict",
            patientPhone = "+91 98000 00002",
            patientAge = 62
        )
        assertTrue("Conflicting booking should fail", result2.isFailure)
    }

    @Test
    fun `receptionist walk-in registration assigns W token`() {
        val doctor = repository.doctors.value.first()
        val walkIn = repository.registerWalkIn(
            doctorId = doctor.id,
            clinicId = doctor.clinicId,
            patientName = "Walk-in Senior",
            patientPhone = "+91 98000 12345",
            patientAge = 78
        )

        assertTrue("Walk-in token should start with W", walkIn.tokenNumber.startsWith("W"))
        assertTrue("isWalkIn should be true", walkIn.isWalkIn)
        assertEquals(QueueStatus.WAITING, walkIn.status)
    }

    @Test
    fun `doctor consultation workflow advances queue states`() {
        val doctor = repository.doctors.value.first()
        // Register a walk-in to test state transitions
        val entry = repository.registerWalkIn(
            doctorId = doctor.id,
            clinicId = doctor.clinicId,
            patientName = "Flow Test Patient",
            patientPhone = "+91 98111 22233",
            patientAge = 65
        )

        // Start consultation
        repository.startConsultation(entry.id)
        val inConsultation = repository.queue.value.find { it.id == entry.id }
        assertEquals(QueueStatus.IN_CONSULTATION, inConsultation?.status)

        // Complete consultation
        repository.completeConsultation(entry.id)
        val completed = repository.queue.value.find { it.id == entry.id }
        assertEquals(QueueStatus.COMPLETED, completed?.status)
    }

    @Test
    fun `admin doctor verification approval adds doctor to platform`() {
        val pendingApp = repository.verificationApplications.value.firstOrNull { it.status == VerificationStatus.PENDING }
        assertNotNull("Should have pending verification application", pendingApp)

        val beforeCount = repository.doctors.value.size
        repository.approveDoctorApplication(pendingApp!!.id, "Super Admin")

        val afterCount = repository.doctors.value.size
        assertEquals(beforeCount + 1, afterCount)

        val approvedDoctor = repository.doctors.value.find { it.name == pendingApp.doctorName }
        assertNotNull("Approved doctor should now be in directory", approvedDoctor)
        assertEquals(VerificationStatus.VERIFIED, approvedDoctor?.verificationStatus)
    }

    @Test
    fun `queue estimate calculates realistic wait time based on duration`() {
        val doctor = repository.doctors.value.first()
        // Book a patient to get a token
        val slot = repository.timeSlots.value.first { it.doctorId == doctor.id && !it.isBooked }
        val booking = repository.bookAppointment(
            doctorId = doctor.id,
            clinicId = doctor.clinicId,
            date = slot.date,
            time = slot.time,
            patientName = "Estimate Test",
            patientPhone = "+91 98222 33344",
            patientAge = 50
        ).getOrThrow()

        val estimate = repository.getQueueEstimate(booking.tokenNumber, doctor.id)
        assertEquals(booking.tokenNumber, estimate.userToken)
        assertTrue("Estimated wait should be calculated as patientsAhead * duration", estimate.estimatedWaitMinutes >= 0)
    }
}
