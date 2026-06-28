package com.example.data

import com.example.R
import com.example.model.*
import com.example.network.GeminiNetwork
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AppRepository(
    private val chatDao: ChatDao,
    private val roomDao: RoomDao
) {
    private val _currentRoom = MutableStateFlow<MeetingRoom?>(null)
    val currentRoom: StateFlow<MeetingRoom?> = _currentRoom.asStateFlow()

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _forcedSpotlightId = MutableStateFlow<String?>(null)
    val forcedSpotlightId: StateFlow<String?> = _forcedSpotlightId.asStateFlow()

    // Moderator rule: when true, everyone except host & AI is muted
    private val _isModeratorMuteAllActive = MutableStateFlow(false)
    val isModeratorMuteAllActive: StateFlow<Boolean> = _isModeratorMuteAllActive.asStateFlow()

    // Personal mute filter: set of muted participant IDs for this local user
    private val _localMutedParticipantIds = MutableStateFlow<Set<String>>(emptySet())
    val localMutedParticipantIds: StateFlow<Set<String>> = _localMutedParticipantIds.asStateFlow()

    val savedRoomsFlow: Flow<List<SavedRoomEntity>> = roomDao.getSavedRooms()

    fun getChatMessagesFlow(roomId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForRoom(roomId).map { list ->
            list.map { entity ->
                ChatMessage(
                    id = entity.id,
                    roomId = entity.roomId,
                    senderId = entity.senderId,
                    senderName = entity.senderName,
                    senderAvatar = entity.senderAvatarRes,
                    message = entity.messageText,
                    timestamp = entity.timestamp,
                    isPrivate = entity.isPrivate,
                    recipientId = entity.recipientId
                )
            }
        }
    }

    val publicRooms = listOf(
        MeetingRoom(
            id = "room_ai",
            title = "🤖 تطبيقات الذكاء الاصطناعي وجيمني",
            topic = "الترجمة الفورية للصوت والفيديو والنماذج المتعددة الوسائط",
            category = "تقنية وتطوير",
            hostName = "د. سارة خبير الذكاء",
            participantCount = 14,
            isClassroom = true,
            hasAIAgent = true
        ),
        MeetingRoom(
            id = "room_global",
            title = "🌍 غرفة الملتقى العالمي الفوري",
            topic = "دردشة حرة بين مشاركين من اليابان، إسبانيا، أمريكا والشرق الأوسط",
            category = "اجتماعي وثقافي",
            hostName = "أليكس جونسون",
            participantCount = 8,
            isClassroom = false,
            hasAIAgent = true
        ),
        MeetingRoom(
            id = "room_kotlin",
            title = "💻 فصلي الافتراضي: معمارية كوتلن الحديثة",
            topic = "شرح Jetpack Compose وقواعد بيانات Room وتصميم MVVM",
            category = "تعليم وفصول افتراضية",
            hostName = "أحمد المهندس",
            participantCount = 22,
            isClassroom = true,
            hasAIAgent = true
        )
    )

    fun login(name: String, email: String, isGuest: Boolean) {
        _currentUser.value = User(
            id = if (isGuest) "u_guest_${System.currentTimeMillis()}" else "u_auth",
            name = name.ifBlank { "مشارك ضيف" },
            email = email.ifBlank { "guest@aimeet.app" },
            isGuest = isGuest
        )
    }

    fun joinRoom(room: MeetingRoom, scope: CoroutineScope) {
        _currentRoom.value = room
        _forcedSpotlightId.value = null
        _isModeratorMuteAllActive.value = false
        _localMutedParticipantIds.value = emptySet()

        // Populate room participants
        val me = Participant(
            id = currentUser.value.id,
            name = "${currentUser.value.name} (أنت)",
            avatarRes = currentUser.value.avatarRes,
            isSpeaking = false,
            isMuted = false,
            isScreenSharing = false,
            isVideoOn = true,
            isTranslating = true,
            originalLanguage = "ar",
            isModerator = true
        )

        val aiTeacher = Participant(
            id = "ai_agent",
            name = "🤖 الأستاذ ذكاء (AI Assistant)",
            avatarRes = R.drawable.img_avatar_teacher,
            isSpeaking = false,
            isVideoOn = true,
            originalLanguage = "ar",
            currentSpeech = "مرحباً بكم جميعاً في غرفة (${room.title})! أنا هنا للمساعدة والإجابة على أي استفسار.",
            isAI = true,
            isModerator = true
        )

        val alex = Participant(
            id = "p_alex",
            name = "Alex Johnson",
            avatarRes = R.drawable.img_avatar_student1,
            isSpeaking = true,
            isVideoOn = true,
            originalLanguage = "en",
            currentSpeech = "Hello everyone! It is amazing how we can speak different languages and understand each other instantly."
        )

        val satoshi = Participant(
            id = "p_satoshi",
            name = "Satoshi Tanaka (田中 智)",
            avatarRes = R.drawable.img_avatar_student2,
            isSpeaking = false,
            isVideoOn = true,
            originalLanguage = "ja",
            currentSpeech = "皆さん、こんにちは！AIのリアルタイム翻訳のおかげで、スムーズに会話ができます。"
        )

        val carlos = Participant(
            id = "p_carlos",
            name = "Carlos Rodriguez",
            avatarRes = R.drawable.img_hero_banner,
            isSpeaking = false,
            isVideoOn = false,
            isScreenSharing = true,
            originalLanguage = "es",
            currentSpeech = "¡Hola amigos! Estoy compartiendo mi pantalla para mostrar la estructura del proyecto."
        )

        _participants.value = listOf(me, aiTeacher, alex, satoshi, carlos)

        // Save to room db
        scope.launch {
            roomDao.insertOrUpdateRoom(
                SavedRoomEntity(
                    id = room.id,
                    title = room.title,
                    topic = room.topic,
                    category = room.category,
                    hostName = room.hostName,
                    participantCount = room.participantCount,
                    isClassroom = room.isClassroom
                )
            )

            // Simulate incoming greeting msg
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = "msg_${System.currentTimeMillis()}",
                    roomId = room.id,
                    senderId = aiTeacher.id,
                    senderName = aiTeacher.name,
                    senderAvatarRes = aiTeacher.avatarRes,
                    messageText = "مرحباً بكم في اجتماع (${room.title}). خدمة الترجمة الصوتية الفورية مفعلة لجميع المشاركين 🌐",
                    timestamp = System.currentTimeMillis(),
                    isPrivate = false,
                    recipientId = null
                )
            )
        }
    }

    fun leaveRoom() {
        _currentRoom.value = null
        _participants.value = emptyList()
    }

    fun toggleLocalMic() {
        val myId = currentUser.value.id
        _participants.value = _participants.value.map { p ->
            if (p.id == myId) p.copy(isMuted = !p.isMuted, isSpeaking = !p.isMuted) else p
        }
    }

    fun toggleLocalCamera() {
        val myId = currentUser.value.id
        _participants.value = _participants.value.map { p ->
            if (p.id == myId) p.copy(isVideoOn = !p.isVideoOn) else p
        }
    }

    fun toggleLocalScreenShare() {
        val myId = currentUser.value.id
        _participants.value = _participants.value.map { p ->
            if (p.id == myId) {
                val newShare = !p.isScreenSharing
                if (newShare) _forcedSpotlightId.value = myId
                p.copy(isScreenSharing = newShare)
            } else p
        }
    }

    fun toggleParticipantMuteLocal(targetParticipantId: String) {
        val current = _localMutedParticipantIds.value.toMutableSet()
        if (current.contains(targetParticipantId)) {
            current.remove(targetParticipantId)
        } else {
            current.add(targetParticipantId)
        }
        _localMutedParticipantIds.value = current
    }

    fun muteAllExcept(allowedParticipantIds: Set<String>) {
        val allIds = _participants.value.map { it.id }.toSet()
        val toMute = allIds - allowedParticipantIds - currentUser.value.id
        _localMutedParticipantIds.value = toMute
    }

    fun moderatorMuteAllExceptHost() {
        val newStatus = !_isModeratorMuteAllActive.value
        _isModeratorMuteAllActive.value = newStatus
        if (newStatus) {
            _participants.value = _participants.value.map { p ->
                if (!p.isModerator && p.id != currentUser.value.id) p.copy(isMuted = true, isSpeaking = false) else p
            }
        } else {
            _participants.value = _participants.value.map { p ->
                if (!p.isModerator) p.copy(isMuted = false) else p
            }
        }
    }

    fun toggleSpotlight(participantId: String) {
        if (_forcedSpotlightId.value == participantId) {
            _forcedSpotlightId.value = null
        } else {
            _forcedSpotlightId.value = participantId
        }
    }

    fun triggerLiveTranslationCycle(targetLangName: String, scope: CoroutineScope) {
        val currentList = _participants.value
        scope.launch {
            val updated = currentList.map { p ->
                if (p.id != currentUser.value.id && p.currentSpeech.isNotBlank()) {
                    val translated = GeminiNetwork.translateInstant(p.currentSpeech, targetLangName)
                    p.copy(isTranslating = true, translatedSpeech = translated)
                } else p
            }
            _participants.value = updated
        }
    }

    fun sendMessage(text: String, isPrivate: Boolean, recipient: Participant?, scope: CoroutineScope) {
        val room = currentRoom.value ?: return
        val me = currentUser.value
        scope.launch {
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = "msg_${System.currentTimeMillis()}_${Math.random()}",
                    roomId = room.id,
                    senderId = me.id,
                    senderName = me.name,
                    senderAvatarRes = me.avatarRes,
                    messageText = text,
                    timestamp = System.currentTimeMillis(),
                    isPrivate = isPrivate,
                    recipientId = recipient?.id
                )
            )

            // If question is directed or room has AI teacher, let AI respond after 1.5 seconds
            if (text.contains("اسأل") || text.contains("ذكاء") || text.contains("سؤال") || text.contains("ما") || text.contains("how") || recipient?.isAI == true) {
                delay(1500)
                val aiAnswer = GeminiNetwork.askClassroomTeacher(text, room.topic, "العربية والإنجليزية")
                val ai = _participants.value.find { it.isAI } ?: return@launch
                chatDao.insertMessage(
                    ChatMessageEntity(
                        id = "msg_ai_${System.currentTimeMillis()}",
                        roomId = room.id,
                        senderId = ai.id,
                        senderName = ai.name,
                        senderAvatarRes = ai.avatarRes,
                        messageText = aiAnswer,
                        timestamp = System.currentTimeMillis(),
                        isPrivate = isPrivate,
                        recipientId = if (isPrivate) me.id else null
                    )
                )

                _participants.value = _participants.value.map { p ->
                    if (p.isAI) p.copy(isSpeaking = true, currentSpeech = aiAnswer, translatedSpeech = aiAnswer) else p
                }
            }
        }
    }
}
