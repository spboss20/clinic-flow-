package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.*
import com.example.data.model.Language
import com.example.data.model.TextScale
import com.example.data.model.UserRole
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AiAssistantScreen(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    isGeminiLiveConfigured: Boolean,
    currentRole: UserRole,
    language: Language,
    textScale: TextScale,
    onSendPrompt: (String) -> Unit,
    onClearChat: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onBookDoctorClick: (String) -> Unit,
    onTrackQueueClick: (String, String) -> Unit,
    onConfirmDraftDelay: (DraftDelayNotification) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice-to-text speech recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = spoken
                onSendPrompt(spoken)
                inputText = ""
            }
        }
    }

    val quickChips = remember(language, currentRole) {
        val isStaff = currentRole == UserRole.DOCTOR || currentRole == UserRole.RECEPTIONIST
        when {
            isStaff && language == Language.HINDI -> listOf(
                "📋 आज का शेड्यूल",
                "🕒 रिक्त स्लॉट देखें",
                "📢 20 मिनट देरी ड्राफ्ट करें",
                "📊 कतार विश्लेषण",
                "⚙️ शेड्यूलिंग ऑपरेशन्स"
            )
            isStaff -> listOf(
                "📋 Summarise today's schedule",
                "🕒 Identify open slots",
                "📢 Draft 20 mins delay",
                "📊 Explain queue changes",
                "⚙️ Supported scheduling ops"
            )
            language == Language.HINDI -> listOf(
                "🔍 जनरल फिजिशियन ₹500 तक",
                "🎫 मेरा टोकन क्या है?",
                "🦷 डेंटिस्ट अपॉइंटमेंट",
                "📅 मेरी बुकिंग कब है?",
                "❓ स्लॉट कैसे बुक करें?"
            )
            else -> listOf(
                "🔍 General Physician under ₹500",
                "🎫 Where am I in the queue?",
                "🦷 Earliest dentist slot",
                "📅 When is my appointment?",
                "❓ How to book a slot?"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicBackground)
    ) {
        // AI Assistant Top Header Card
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ClinicTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "ClinicFlow AI",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (language == Language.HINDI) "क्लिनिकफ्लो एआई" else "ClinicFlow AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicNavy,
                                fontSize = (17 * textScale.scale).sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isGeminiLiveConfigured) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isGeminiLiveConfigured) Color(0xFF2E7D32) else ClinicTeal)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isGeminiLiveConfigured) "Gemini 3.5 Flash" else "Smart Offline/DB",
                                    color = if (isGeminiLiveConfigured) Color(0xFF2E7D32) else ClinicNavy,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (language == Language.HINDI)
                            "अपॉइंटमेंट व कतार सहायक (हिंदी / English)"
                        else
                            "Smart Healthcare Navigation & Queue Assistant",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicNavy.copy(alpha = 0.7f),
                            fontSize = (12 * textScale.scale).sp
                        )
                    )
                }

                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("ai_clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = ClinicNavy.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    language = language,
                    textScale = textScale,
                    onSpeak = { onSpeakMessage(msg.text) },
                    onActionClick = { prompt ->
                        onSendPrompt(prompt)
                    },
                    onBookDoctor = onBookDoctorClick,
                    onTrackQueue = onTrackQueueClick,
                    onConfirmDelay = onConfirmDraftDelay
                )
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ClinicTeal,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == Language.HINDI) "क्लिनिकफ्लो एआई सोच रहा है..." else "ClinicFlow AI is thinking...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ClinicNavy.copy(alpha = 0.7f),
                                fontSize = (14 * textScale.scale).sp
                            )
                        )
                    }
                }
            }
        }

        // Quick Suggestions Horizontal Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickChips) { chipText ->
                SuggestionChip(
                    onClick = {
                        onSendPrompt(chipText)
                    },
                    label = {
                        Text(
                            text = chipText,
                            fontSize = (13 * textScale.scale).sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.White,
                        labelColor = ClinicNavy
                    ),
                    border = BorderStroke(1.dp, ClinicTeal.copy(alpha = 0.3f))
                )
            }
        }

        // Input Area
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_prompt_input_field"),
                        placeholder = {
                            Text(
                                text = if (language == Language.HINDI) "हिंदी या English में पूछें..." else "Type doctor, queue, or question...",
                                fontSize = (14 * textScale.scale).sp,
                                color = Color.Gray
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClinicTeal,
                            unfocusedBorderColor = Color(0xFFD1D5DB)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendPrompt(inputText)
                                inputText = ""
                            }
                        })
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Voice Input Button
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == Language.HINDI) "hi-IN" else "en-IN")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == Language.HINDI) "बोलिए, क्लिनिकफ्लो सुन रहा है..." else "Speak, ClinicFlow is listening...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (_: Exception) {
                                // Graceful fallback
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ClinicTealLight)
                            .testTag("ai_voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = ClinicTeal
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendPrompt(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) ClinicTeal else Color(0xFFE2E8F0))
                            .testTag("ai_send_prompt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else Color.Gray
                        )
                    }
                }

                // Disclaimer
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == Language.HINDI)
                        "क्लिनिकफ्लो एआई एक प्रशासनिक सहायता उपकरण है, डॉक्टर नहीं। आपात स्थिति में 112 / 108 डायल करें।"
                    else
                        "ClinicFlow AI provides navigation & scheduling assistance, not medical diagnosis. In emergencies, call 112 / 108.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (11 * textScale.scale).sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    language: Language,
    textScale: TextScale,
    onSpeak: () -> Unit,
    onActionClick: (String) -> Unit,
    onBookDoctor: (String) -> Unit,
    onTrackQueue: (String, String) -> Unit,
    onConfirmDelay: (DraftDelayNotification) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val isEmergency = message.isEmergency

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isEmergency) Color(0xFFD32F2F) else ClinicTeal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEmergency) Icons.Default.Warning else Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Main Bubble Card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isUser -> ClinicNavy
                        isEmergency -> Color(0xFFFFEBEE)
                        else -> Color.White
                    }
                ),
                border = if (!isUser && !isEmergency) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isUser) Color.White else if (isEmergency) Color(0xFFB71C1C) else ClinicNavy,
                            fontSize = (15 * textScale.scale).sp,
                            lineHeight = (22 * textScale.scale).sp
                        )
                    )

                    // Text-To-Speech Button on AI messages
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSpeak() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = ClinicTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == Language.HINDI) "सुनें" else "Listen",
                                fontSize = (12 * textScale.scale).sp,
                                color = ClinicTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Embedded Doctor Recommendations Cards
            if (message.doctorResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    message.doctorResults.forEach { doctor ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, ClinicTeal.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doctor.name,
                                            fontWeight = FontWeight.Bold,
                                            color = ClinicNavy,
                                            fontSize = (14 * textScale.scale).sp
                                        )
                                        Text(
                                            text = "${doctor.speciality} • ₹${doctor.consultationFee}",
                                            color = ClinicTealDark,
                                            fontSize = (12 * textScale.scale).sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (doctor.isVerified) {
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Verified",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onBookDoctor(doctor.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = if (language == Language.HINDI) "स्लॉट चुनें व बुक करें" else "Select Slot & Book",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Embedded Live Queue Token Card
            if (message.tokenInfo != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val q = message.tokenInfo
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ClinicTealLight),
                    border = BorderStroke(1.dp, ClinicTeal.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Token #${q.userToken}",
                                fontWeight = FontWeight.Bold,
                                fontSize = (18 * textScale.scale).sp,
                                color = ClinicNavy
                            )
                            Surface(
                                color = ClinicTeal,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Serving: ${q.currentServingToken ?: "A001"}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${q.patientsAhead} patients ahead • Est. Wait: ~${q.estimatedWaitMinutes} mins",
                            fontSize = (13 * textScale.scale).sp,
                            color = ClinicNavy
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onTrackQueue(q.userToken, "d1")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text("Track Live Queue Screen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Embedded Delay Broadcast Card (Doctor / Receptionist)
            if (message.draftDelay != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val draft = message.draftDelay
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    border = BorderStroke(1.dp, Color(0xFFF97316)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFC2410C))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delay Draft: ~${draft.minutes} Mins",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9A3412),
                                fontSize = (14 * textScale.scale).sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Affected patients today: ${draft.affectedPatientCount}",
                            fontSize = (12 * textScale.scale).sp,
                            color = Color(0xFF7C2D12)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onConfirmDelay(draft) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text("Confirm & Broadcast to Patients", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Suggested Action Chips
            if (message.suggestedActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    message.suggestedActions.take(2).forEach { act ->
                        AssistChip(
                            onClick = {
                                if (act.actionType == "BOOK_DOCTOR" && act.payload != null) {
                                    onBookDoctor(act.payload)
                                } else if (act.actionType == "TRACK_QUEUE" && act.payload != null) {
                                    val parts = act.payload.split(":")
                                    val token = parts.getOrNull(0) ?: "A006"
                                    val docId = parts.getOrNull(1) ?: "d1"
                                    onTrackQueue(token, docId)
                                } else {
                                    onActionClick(act.prompt)
                                }
                            },
                            label = {
                                Text(
                                    text = act.title,
                                    fontSize = (12 * textScale.scale).sp,
                                    color = ClinicTealDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = ClinicTealLight)
                        )
                    }
                }
            }
        }
    }
}
