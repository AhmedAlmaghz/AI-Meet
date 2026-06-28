package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User,
    currentLanguage: AppLanguage,
    currentPalette: ThemePalette,
    publicRooms: List<MeetingRoom>,
    onJoinRoom: (MeetingRoom) -> Unit,
    onCreateRoom: (title: String, topic: String, isClassroom: Boolean) -> Unit,
    onLanguageToggle: () -> Unit,
    onPaletteSelect: (ThemePalette) -> Unit,
    onLogout: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val isAr = currentLanguage == AppLanguage.ARABIC

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VideoCall, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "منصة اجتماعات AI Meet" else "AI Meet Platform",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onLanguageToggle() }, modifier = Modifier.testTag("home_lang_toggle")) {
                        Text(currentLanguage.flag, fontSize = 20.sp)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showThemeMenu = true }, modifier = Modifier.testTag("home_theme_button")) {
                            Icon(Icons.Default.Palette, contentDescription = "Themes", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            ThemePalette.entries.forEach { palette ->
                                DropdownMenuItem(
                                    text = { Text("${palette.icon} ${palette.displayName}") },
                                    onClick = {
                                        onPaletteSelect(palette)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onLogout, modifier = Modifier.testTag("home_logout_button")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("home_create_fab")
                    .sizeIn(minWidth = 56.dp, minHeight = 56.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isAr) "غرفة اجتماع جديدة" else "New Meeting Room", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // User Welcome Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(currentUser.avatarRes),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "أهلاً بك، ${currentUser.name}" else "Welcome, ${currentUser.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentUser.isGuest) (if (isAr) "وضع الضيف الآمن • Gemini Live" else "Secure Guest Mode • Gemini Live") else currentUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                // Hero Banner Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(R.drawable.img_hero_banner),
                            contentDescription = "Hero Illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(if (isAr) "⚡ الترجمة الفورية بالذكاء الاصطناعي" else "⚡ AI Instant Voice Translation", modifier = Modifier.padding(4.dp))
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isAr) "تحدث بلغتك وافهم العالم بكل اللغات فوراً" else "Speak your language, understand the world instantly",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isAr) "🔥 الغرف والفصول الافتراضية النشطة" else "🔥 Active Rooms & Virtual Classrooms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(publicRooms, key = { it.id }) { room ->
                RoomCardItem(room = room, isAr = isAr, onClick = { onJoinRoom(room) })
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateRoomDialog(
            isAr = isAr,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, topic, isClass ->
                onCreateRoom(title, topic, isClass)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun RoomCardItem(room: MeetingRoom, isAr: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("room_card_${room.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (room.isClassroom) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (room.isClassroom) "📚 فصول ذكية" else "👥 دردشة جماعية",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (room.isClassroom) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(room.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.width(4.dp))
                    Text("${room.participantCount}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = room.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = room.topic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "المدير: ${room.hostName}" else "Host: ${room.hostName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if (isAr) "انضمام للغرفة" else "Join Room", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateRoomDialog(
    isAr: Boolean,
    onDismiss: () -> Unit,
    onCreate: (title: String, topic: String, isClassroom: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var isClassroom by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isAr) "إنشاء غرفة اجتماع جديدة" else "Create New Meeting Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isAr) "عنوان الغرفة" else "Room Title") },
                    placeholder = { Text("مثال: تعلم تصميم واجهات كوتلن") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("create_room_title")
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text(if (isAr) "وصف وموضوع الغرفة" else "Topic Description") },
                    placeholder = { Text("مثال: مناقشة مفتوحة مع ترجمة فورية") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("create_room_topic")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isClassroom = !isClassroom }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = isClassroom, onCheckedChange = { isClassroom = it })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(if (isAr) "تفعيل وضع الصف الافتراضي والذكاء الاصطناعي" else "Enable Virtual Classroom & AI Agent mode", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isAr) "يتضمن مساعد معلم ذكي يجيب على أسئلة الطلاب فوراً" else "Includes AI Teacher assistant answering student questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title.ifBlank { if (isAr) "غرفة محادثة عامة" else "General Chat Room" }, topic.ifBlank { "اجتماع فيديو وصوت مع ترجمة فورية" }, isClassroom) },
                modifier = Modifier.testTag("create_room_submit")
            ) {
                Text(if (isAr) "إنشاء ودخول" else "Create & Enter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isAr) "إلغاء" else "Cancel")
            }
        }
    )
}
