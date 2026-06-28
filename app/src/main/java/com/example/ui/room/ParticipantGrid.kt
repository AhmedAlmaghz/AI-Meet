package com.example.ui.room

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Participant

@Composable
fun ParticipantGrid(
    participants: List<Participant>,
    currentUserId: String,
    forcedSpotlightId: String?,
    localMutedIds: Set<String>,
    isAr: Boolean,
    onToggleSpotlight: (String) -> Unit,
    onToggleLocalMute: (String) -> Unit,
    onOpenDM: (Participant) -> Unit,
    onMuteAllExceptThis: (String) -> Unit
) {
    // If there is an active spotlight or forced screen share, render Spotlight layout
    val spotlightParticipant = if (forcedSpotlightId != null) {
        participants.find { it.id == forcedSpotlightId }
    } else {
        participants.find { it.isScreenSharing }
    }

    if (spotlightParticipant != null) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Hero Spotlight Stream (80% Height)
            Box(modifier = Modifier.weight(0.75f).fillMaxWidth()) {
                ParticipantVideoCard(
                    p = spotlightParticipant,
                    isMe = spotlightParticipant.id == currentUserId,
                    isSpotlighted = true,
                    isLocalMuted = localMutedIds.contains(spotlightParticipant.id),
                    isAr = isAr,
                    onToggleSpotlight = { onToggleSpotlight(spotlightParticipant.id) },
                    onToggleLocalMute = { onToggleLocalMute(spotlightParticipant.id) },
                    onOpenDM = { onOpenDM(spotlightParticipant) },
                    onMuteAllExceptThis = { onMuteAllExceptThis(spotlightParticipant.id) },
                    modifier = Modifier.fillMaxSize()
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                ) {
                    Text(if (isAr) "تسليط الضوء الإجبارى 🌟" else "Forced Spotlight View 🌟", modifier = Modifier.padding(6.dp))
                }
            }

            // Bottom Ribbon of remaining participants
            val remaining = participants.filter { it.id != spotlightParticipant.id }
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (remaining.size > 2) 3 else 2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(0.25f).fillMaxWidth()
            ) {
                items(remaining, key = { it.id }) { p ->
                    ParticipantVideoCard(
                        p = p,
                        isMe = p.id == currentUserId,
                        isSpotlighted = false,
                        isLocalMuted = localMutedIds.contains(p.id),
                        isAr = isAr,
                        onToggleSpotlight = { onToggleSpotlight(p.id) },
                        onToggleLocalMute = { onToggleLocalMute(p.id) },
                        onOpenDM = { onOpenDM(p) },
                        onMuteAllExceptThis = { onMuteAllExceptThis(p.id) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    } else {
        // Standard Balanced Grid
        val columnsCount = when {
            participants.size <= 2 -> 1
            participants.size <= 4 -> 2
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(participants, key = { it.id }) { p ->
                val cardHeight = if (participants.size <= 2) 280.dp else 200.dp
                ParticipantVideoCard(
                    p = p,
                    isMe = p.id == currentUserId,
                    isSpotlighted = false,
                    isLocalMuted = localMutedIds.contains(p.id),
                    isAr = isAr,
                    onToggleSpotlight = { onToggleSpotlight(p.id) },
                    onToggleLocalMute = { onToggleLocalMute(p.id) },
                    onOpenDM = { onOpenDM(p) },
                    onMuteAllExceptThis = { onMuteAllExceptThis(p.id) },
                    modifier = Modifier.height(cardHeight)
                )
            }
        }
    }
}

@Composable
fun ParticipantVideoCard(
    p: Participant,
    isMe: Boolean,
    isSpotlighted: Boolean,
    isLocalMuted: Boolean,
    isAr: Boolean,
    onToggleSpotlight: () -> Unit,
    onToggleLocalMute: () -> Unit,
    onOpenDM: () -> Unit,
    onMuteAllExceptThis: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val borderColor = when {
        p.isSpeaking -> MaterialTheme.colorScheme.primary
        isSpotlighted -> MaterialTheme.colorScheme.tertiary
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(if (p.isSpeaking) 8.dp else 2.dp),
        modifier = modifier
            .animateContentSize()
            .border(if (p.isSpeaking || isSpotlighted) 3.dp else 0.dp, borderColor, RoundedCornerShape(20.dp))
            .testTag("video_card_${p.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Video Stream Content
            if (p.isScreenSharing) {
                // Render Mock Shared Screen
                Image(
                    painter = painterResource(R.drawable.img_hero_banner),
                    contentDescription = "Shared Screen",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            } else if (p.isVideoOn) {
                // Simulate Video feed with subtle gradient + Avatar portrait
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(p.avatarRes),
                        contentDescription = "Video Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(if (isSpotlighted) 140.dp else 90.dp)
                            .clip(CircleShape)
                            .border(3.dp, if (p.isSpeaking) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            } else {
                // Camera Off View
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = Color(0xFF313244), modifier = Modifier.size(64.dp)) {
                            Icon(Icons.Default.VideocamOff, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(16.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(if (isAr) "الكاميرا مغلقة" else "Camera Off", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }

            // Top Status Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (p.isAI) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                            Text("🤖 AI Teacher", modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                        }
                    }
                    if (p.isModerator && !p.isAI) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("👑 " + if (isAr) "مدير" else "Host", modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                        }
                    }
                    if (isLocalMuted) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("🔇 " + if (isAr) "مكتم شخصياً" else "Local Muted", modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                        }
                    }
                }

                // Individual Control Meatball Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("card_menu_${p.id}")
                    ) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isSpotlighted) (if (isAr) "إلغاء التثبيت" else "Unpin View") else (if (isAr) "🌟 تثبيت بالشاشة إجبارياً" else "🌟 Spotlight View")) },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) },
                            onClick = {
                                onToggleSpotlight()
                                showMenu = false
                            }
                        )
                        if (!isMe) {
                            DropdownMenuItem(
                                text = { Text(if (isAr) "💬 مراسلة خاصة (DM)" else "💬 Private Chat DM") },
                                leadingIcon = { Icon(Icons.Default.Mail, null) },
                                onClick = {
                                    onOpenDM()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isLocalMuted) (if (isAr) "🔊 إلغاء الكتم الشخصي" else "🔊 Unmute Personally") else (if (isAr) "🔇 كتم صوت هذا المشارك لي فقط" else "🔇 Mute Personally")) },
                                leadingIcon = { Icon(if (isLocalMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff, null) },
                                onClick = {
                                    onToggleLocalMute()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isAr) "🎯 كتم الجميع عدا هذا المشارك" else "🎯 Mute All Except This") },
                                leadingIcon = { Icon(Icons.Default.FilterAlt, null) },
                                onClick = {
                                    onMuteAllExceptThis()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Name Bar
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(topEnd = 14.dp),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (p.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (p.isMuted) Color(0xFFFF5555) else Color(0xFF55FF77),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = p.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (p.originalLanguage.isNotBlank()) {
                        Text(
                            text = " [${p.originalLanguage.uppercase()}]",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
