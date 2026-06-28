package com.example.ui.room

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
import com.example.model.Participant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantListSheet(
    isAr: Boolean,
    participants: List<Participant>,
    currentUserId: String,
    isModeratorMuteAll: Boolean,
    localMutedIds: Set<String>,
    onDismiss: () -> Unit,
    onToggleSpotlight: (String) -> Unit,
    onToggleLocalMute: (String) -> Unit,
    onOpenDM: (Participant) -> Unit,
    onModeratorMuteAllExceptHost: () -> Unit,
    onMuteAllExceptThis: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PeopleAlt, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isAr) "المشاركون في الغرفة (${participants.size})" else "Room Participants (${participants.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(if (isAr) "إغلاق" else "Close")
                }
            }

            // Moderator Global Card Control
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isModeratorMuteAll) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isModeratorMuteAll) (if (isAr) "🔒 الكتم العام الإجباري مفعل" else "🔒 Global Mute Active") else (if (isAr) "👑 صلاحيات مدير الغرفة" else "👑 Host Moderator Controls"),
                            fontWeight = FontWeight.Bold,
                            color = if (isModeratorMuteAll) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (isAr) "كتم كل أصوات المشاركين عدا المعلم/المدير والذكاء الاصطناعي" else "Mute everyone except teacher/host & AI agent",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isModeratorMuteAll) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = onModeratorMuteAllExceptHost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isModeratorMuteAll) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("mod_mute_all_button")
                    ) {
                        Text(if (isModeratorMuteAll) (if (isAr) "فك الكتم" else "Unmute All") else (if (isAr) "كتم الجميع" else "Mute All"))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // List of Participants
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(participants, key = { it.id }) { p ->
                    ParticipantRowItem(
                        p = p,
                        isMe = p.id == currentUserId,
                        isLocalMuted = localMutedIds.contains(p.id),
                        isAr = isAr,
                        onSpotlight = { onToggleSpotlight(p.id) },
                        onDM = { onOpenDM(p) },
                        onLocalMute = { onToggleLocalMute(p.id) },
                        onMuteAllExceptThis = { onMuteAllExceptThis(p.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantRowItem(
    p: Participant,
    isMe: Boolean,
    isLocalMuted: Boolean,
    isAr: Boolean,
    onSpotlight: () -> Unit,
    onDM: () -> Unit,
    onLocalMute: () -> Unit,
    onMuteAllExceptThis: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(p.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (p.isAI) {
                        Spacer(Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) { Text("AI") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = if (p.isSpeaking) (if (isAr) "يتحدث الآن 🔊" else "Speaking 🔊") else (if (p.isMuted) (if (isAr) "مكتم 🔇" else "Muted 🔇") else (if (isAr) "متصل 🟢" else "Online 🟢")),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (p.isSpeaking) MaterialTheme.colorScheme.primary else (if (p.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (p.isScreenSharing) {
                        Text("• " + if (isAr) "يشارك الشاشة 🖥️" else "Sharing Screen 🖥️", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            // Quick Actions
            if (!isMe) {
                IconButton(onClick = onDM, modifier = Modifier.testTag("row_dm_${p.id}")) {
                    Icon(Icons.Default.MailOutline, contentDescription = "DM", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isAr) "🌟 تثبيت بالشاشة إجبارياً" else "🌟 Spotlight View") },
                        onClick = {
                            onSpotlight()
                            showMenu = false
                        }
                    )
                    if (!isMe) {
                        DropdownMenuItem(
                            text = { Text(if (isLocalMuted) (if (isAr) "🔊 إلغاء الكتم الشخصي" else "🔊 Unmute Locally") else (if (isAr) "🔇 كتم صوت هذا الشخص عندي فقط" else "🔇 Mute Locally")) },
                            onClick = {
                                onLocalMute()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isAr) "🎯 كتم الجميع عدا هذا المشارك" else "🎯 Mute All Except This") },
                            onClick = {
                                onMuteAllExceptThis()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
