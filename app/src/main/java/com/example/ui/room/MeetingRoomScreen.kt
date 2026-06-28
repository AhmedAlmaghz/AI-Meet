package com.example.ui.room

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRoomScreen(
    room: MeetingRoom,
    currentUser: User,
    participants: List<Participant>,
    currentLanguage: AppLanguage,
    forcedSpotlightId: String?,
    isModeratorMuteAll: Boolean,
    localMutedIds: Set<String>,
    isLiveTranslationActive: Boolean,
    isTTSAudioEnabled: Boolean,
    chatMessages: List<ChatMessage>,
    onLeave: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleScreenShare: () -> Unit,
    onToggleSpotlight: (String) -> Unit,
    onToggleLocalMuteParticipant: (String) -> Unit,
    onMuteAllExcept: (Set<String>) -> Unit,
    onModeratorMuteAllExceptHost: () -> Unit,
    onToggleLiveTranslation: () -> Unit,
    onToggleTTSAudio: () -> Unit,
    onSendMessage: (text: String, isPrivate: Boolean, recipient: Participant?) -> Unit
) {
    var showChatSheet by remember { mutableStateOf(false) }
    var showParticipantsSheet by remember { mutableStateOf(false) }
    var dmRecipient by remember { mutableStateOf<Participant?>(null) }

    val isAr = currentLanguage == AppLanguage.ARABIC
    val me = participants.find { it.id == currentUser.id }
    val isMyMicMuted = me?.isMuted ?: false
    val isMyCamOff = !(me?.isVideoOn ?: true)
    val isMyScreenSharing = me?.isScreenSharing ?: false

    // Find active translator speech
    val activeTranslator = participants.find { it.id != currentUser.id && it.translatedSpeech.isNotBlank() }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                Spacer(Modifier.width(4.dp))
                                Text("REC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = room.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isModeratorMuteAll) (if (isAr) "🔒 كتم عام إجباري للمشاركين" else "🔒 Forced Moderator Mute") else (if (isAr) "🟢 اجتماع مباشر والسماعات حرة" else "🟢 Live Open Room"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isModeratorMuteAll) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.clickable { showParticipantsSheet = true }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${participants.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = onLeave,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("room_leave_button")
                        ) {
                            Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isAr) "مغادرة" else "Leave", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Gemini Live Subtitle Overlay Box
                    AnimatedVisibility(visible = isLiveTranslationActive && activeTranslator != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Translate, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(activeTranslator?.name ?: "مشارك", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(" (${activeTranslator?.originalLanguage?.uppercase()}) ➔ ${currentLanguage.displayName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    }
                                    Text(
                                        text = activeTranslator?.translatedSpeech ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = onToggleTTSAudio, modifier = Modifier.testTag("room_tts_toggle")) {
                                    Icon(
                                        if (isTTSAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                        contentDescription = "TTS Toggle",
                                        tint = if (isTTSAudioEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Control Bar Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Toggle
                        ControlIconButton(
                            icon = if (isMyMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMyMicMuted) (if (isAr) "مكتم" else "Muted") else (if (isAr) "الميكروفون" else "Mic"),
                            isActive = !isMyMicMuted,
                            onClick = onToggleMic,
                            tag = "room_mic_toggle"
                        )

                        // Camera Toggle
                        ControlIconButton(
                            icon = if (isMyCamOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            label = if (isMyCamOff) (if (isAr) "الكاميرا متوقفة" else "Cam Off") else (if (isAr) "الكاميرا" else "Camera"),
                            isActive = !isMyCamOff,
                            onClick = onToggleCamera,
                            tag = "room_cam_toggle"
                        )

                        // Screen Share Toggle
                        ControlIconButton(
                            icon = if (isMyScreenSharing) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                            label = if (isMyScreenSharing) (if (isAr) "إيقاف المشاركة" else "Stop Share") else (if (isAr) "مشاركة الشاشة" else "Share Screen"),
                            isActive = isMyScreenSharing,
                            isSpecial = true,
                            onClick = onToggleScreenShare,
                            tag = "room_share_toggle"
                        )

                        // Chat Sheet Button
                        Box {
                            ControlIconButton(
                                icon = Icons.AutoMirrored.Filled.Chat,
                                label = if (isAr) "الدردشة" else "Chat",
                                isActive = false,
                                onClick = {
                                    dmRecipient = null
                                    showChatSheet = true
                                },
                                tag = "room_chat_button"
                            )
                            if (chatMessages.size > 1) {
                                Badge(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                                    Text("${chatMessages.size}")
                                }
                            }
                        }

                        // Participants List Button
                        ControlIconButton(
                            icon = Icons.Default.People,
                            label = if (isAr) "المشاركين" else "People",
                            isActive = false,
                            onClick = { showParticipantsSheet = true },
                            tag = "room_people_button"
                        )

                        // Translation Toggle
                        ControlIconButton(
                            icon = Icons.Default.Translate,
                            label = if (isLiveTranslationActive) "Gemini ON" else "AI OFF",
                            isActive = isLiveTranslationActive,
                            onClick = onToggleLiveTranslation,
                            tag = "room_trans_toggle"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ParticipantGrid(
                participants = participants,
                currentUserId = currentUser.id,
                forcedSpotlightId = forcedSpotlightId,
                localMutedIds = localMutedIds,
                isAr = isAr,
                onToggleSpotlight = onToggleSpotlight,
                onToggleLocalMute = onToggleLocalMuteParticipant,
                onOpenDM = { target ->
                    dmRecipient = target
                    showChatSheet = true
                },
                onMuteAllExceptThis = { targetId ->
                    onMuteAllExcept(setOf(targetId))
                }
            )
        }
    }

    if (showChatSheet) {
        ChatSheet(
            isAr = isAr,
            messages = chatMessages,
            currentUserId = currentUser.id,
            recipient = dmRecipient,
            onDismiss = { showChatSheet = false },
            onSend = { text, isPriv ->
                onSendMessage(text, isPriv, dmRecipient)
            },
            onSwitchRecipient = { newRec ->
                dmRecipient = newRec
            }
        )
    }

    if (showParticipantsSheet) {
        ParticipantListSheet(
            isAr = isAr,
            participants = participants,
            currentUserId = currentUser.id,
            isModeratorMuteAll = isModeratorMuteAll,
            localMutedIds = localMutedIds,
            onDismiss = { showParticipantsSheet = false },
            onToggleSpotlight = onToggleSpotlight,
            onToggleLocalMute = onToggleLocalMuteParticipant,
            onOpenDM = { target ->
                showParticipantsSheet = false
                dmRecipient = target
                showChatSheet = true
            },
            onModeratorMuteAllExceptHost = onModeratorMuteAllExceptHost,
            onMuteAllExceptThis = { targetId ->
                onMuteAllExcept(setOf(targetId))
            }
        )
    }
}

@Composable
fun ControlIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String,
    isSpecial: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (isSpecial && isActive) MaterialTheme.colorScheme.tertiary else (if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
            tonalElevation = if (isActive) 6.dp else 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .testTag(tag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSpecial && isActive) MaterialTheme.colorScheme.onTertiary else (if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
