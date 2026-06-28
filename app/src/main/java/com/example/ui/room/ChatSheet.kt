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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSheet(
    isAr: Boolean,
    messages: List<ChatMessage>,
    currentUserId: String,
    recipient: Participant?,
    onDismiss: () -> Unit,
    onSend: (text: String, isPrivate: Boolean) -> Unit,
    onSwitchRecipient: (Participant?) -> Unit
) {
    var inputMsg by remember { mutableStateOf("") }
    val isPrivateMode = recipient != null

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
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubble, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isPrivateMode) (if (isAr) "دردشة خاصة مع: ${recipient?.name}" else "Private DM: ${recipient?.name}") else (if (isAr) "دردشة الغرفة العامة (Room Chat)" else "Public Room Chat"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isPrivateMode) {
                            Text(
                                text = if (isAr) "الرسائل مشفرة وتظهر لكما فقط 🔒" else "Messages are private & encrypted 🔒",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isPrivateMode) {
                    TextButton(onClick = { onSwitchRecipient(null) }) {
                        Text(if (isAr) "عودة للعالمي" else "Public Chat")
                    }
                }
            }

            HorizontalDivider()

            // Filtered Messages List
            val displayedMessages = if (isPrivateMode) {
                messages.filter { msg ->
                    (msg.senderId == currentUserId && msg.recipientId == recipient?.id) ||
                    (msg.senderId == recipient?.id && (msg.recipientId == currentUserId || msg.isPrivate))
                }
            } else {
                messages.filter { !it.isPrivate }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (displayedMessages.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isAr) "لا توجد رسائل بعد. ابدأ المراسلة أو اسأل المساعد الذكي!" else "No messages yet. Say hi or ask the AI assistant!",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                items(displayedMessages, key = { it.id }) { msg ->
                    ChatMessageBubble(msg = msg, isMe = msg.senderId == currentUserId, isAr = isAr)
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputMsg,
                        onValueChange = { inputMsg = it },
                        placeholder = {
                            Text(
                                if (isPrivateMode) (if (isAr) "رسالة خاصة لـ ${recipient?.name}..." else "Private message...") else (if (isAr) "رسالة للجميع أو اسأل الذكاء الاصطناعي..." else "Message everyone...")
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (inputMsg.isNotBlank()) {
                                onSend(inputMsg, isPrivateMode)
                                inputMsg = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(msg: ChatMessage, isMe: Boolean, isAr: Boolean) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = remember(msg.timestamp) { formatter.format(Date(msg.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Image(
                painter = painterResource(msg.senderAvatar),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start, modifier = Modifier.widthIn(max = 280.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isMe) (if (isAr) "أنت" else "You") else msg.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                if (msg.isPrivate) {
                    Spacer(Modifier.width(6.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                        Text("DM 🔒", fontSize = 9.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = msg.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Text(
                text = timeString,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
