package com.example

import com.example.data.ai.ClinicFlowAiService
import com.example.data.ai.MessageSender
import com.example.data.model.Language
import com.example.data.model.UserRole
import com.example.data.repository.ClinicFlowRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: ClinicFlowRepository
    private lateinit var aiService: ClinicFlowAiService

    @Before
    fun setup() {
        repository = ClinicFlowRepository.getInstance()
        aiService = ClinicFlowAiService(repository)
    }

    @Test
    fun testEmergencyDetectionTriggered() = runBlocking {
        val response = aiService.processQuery(
            userPrompt = "Patient has severe chest pain and cannot breathe",
            currentRole = UserRole.PATIENT,
            language = Language.ENGLISH
        )
        assertTrue(response.isEmergency)
        assertTrue(response.text.contains("112") || response.text.contains("108"))
    }

    @Test
    fun testEmergencyDetectionHindi() = runBlocking {
        val response = aiService.processQuery(
            userPrompt = "मरीज के सीने में बहुत तेज दर्द है",
            currentRole = UserRole.PATIENT,
            language = Language.HINDI
        )
        assertTrue(response.isEmergency)
        assertTrue(response.text.contains("112") || response.text.contains("108"))
    }

    @Test
    fun testNaturalLanguageDoctorSearch() = runBlocking {
        val response = aiService.processQuery(
            userPrompt = "Mujhe general physician chahiye under 600",
            currentRole = UserRole.PATIENT,
            language = Language.ENGLISH
        )
        assertTrue(response.doctorResults.isNotEmpty())
        assertTrue(response.doctorResults.all { it.consultationFee <= 600 })
        assertTrue(response.doctorResults.all { it.speciality == "General Physician" })
    }

    @Test
    fun testQueueTokenLookup() = runBlocking {
        val response = aiService.processQuery(
            userPrompt = "Mera token number aur queue status kya hai?",
            currentRole = UserRole.PATIENT,
            language = Language.HINDI
        )
        assertNotNull(response.tokenInfo)
        assertTrue(response.text.contains("टोकन") || response.text.contains("Token"))
    }

    @Test
    fun testDoctorDelayAnnouncementDraft() = runBlocking {
        val response = aiService.processQuery(
            userPrompt = "Running 30 mins delay due to emergency traffic",
            currentRole = UserRole.DOCTOR,
            language = Language.ENGLISH
        )
        assertNotNull(response.draftDelay)
        assertEquals(30, response.draftDelay?.minutes)
    }
}
