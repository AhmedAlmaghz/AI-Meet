package com.aistudio.meetingplatform.feature.meeting.presentation.room

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect

import com.aistudio.meetingplatform.feature.chat.presentation.ChatOverlay
import com.aistudio.meetingplatform.feature.translation.presentation.SubtitlesOverlay
import com.aistudio.meetingplatform.feature.meeting.domain.model.Participant

@Composable
fun LocalCameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocalCameraPreviewWithPermission(modifier: Modifier = Modifier) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(key1 = cameraPermissionState.status) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        LocalCameraPreview(modifier = modifier)
    } else {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = "No Permission",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "صلاحية الكاميرا مطلوبة",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ParticipantCameraFeed(
    participant: Participant,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "video_feed")

    // Pulsing outline if speaking
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_glow"
    )

    val isSpeaking = participant.isSpeaking
    val isCameraOn = participant.isCameraOn

    val speakingBorderModifier = if (isSpeaking) {
        Modifier.border(
            width = 3.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
            shape = MaterialTheme.shapes.medium
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = MaterialTheme.shapes.medium
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .then(speakingBorderModifier)
            .background(
                if (isCameraOn) {
                    Brush.linearGradient(
                        colors = if (participant.id == "ai_agent") {
                            listOf(
                                Color(0xFF0F2027),
                                Color(0xFF203A43),
                                Color(0xFF2C5364)
                            )
                        } else if (participant.id == "local_user") {
                            listOf(
                                Color(0xFF1F1C2C),
                                Color(0xFF928DAB)
                            )
                        } else if (participant.id == "p_alex") {
                            listOf(
                                Color(0xFF0D2B45),
                                Color(0xFF203C56),
                                Color(0xFF543D5C)
                            )
                        } else if (participant.id == "p_satoshi") {
                            listOf(
                                Color(0xFF1D976C),
                                Color(0xFF93F9B9)
                            )
                        } else {
                            listOf(
                                Color(0xFF1F1C2C),
                                Color(0xFF928DAB)
                            )
                        }
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCameraOn) {
            if (participant.id == "local_user") {
                // Real front camera preview
                Box(modifier = Modifier.fillMaxSize()) {
                    LocalCameraPreviewWithPermission(modifier = Modifier.fillMaxSize())

                    // Camera overlay (Viewfinder effect)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val margin = 20.dp.toPx()
                        val length = 15.dp.toPx()
                        val stroke = 2.dp.toPx()
                        val color = Color(0xFF00F2FE).copy(alpha = 0.8f)

                        // Top-Left corner
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(margin, margin), end = androidx.compose.ui.geometry.Offset(margin + length, margin), strokeWidth = stroke)
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(margin, margin), end = androidx.compose.ui.geometry.Offset(margin, margin + length), strokeWidth = stroke)

                        // Top-Right corner
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width - margin, margin), end = androidx.compose.ui.geometry.Offset(size.width - margin - length, margin), strokeWidth = stroke)
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width - margin, margin), end = androidx.compose.ui.geometry.Offset(size.width - margin, margin + length), strokeWidth = stroke)

                        // Bottom-Left corner
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(margin, size.height - margin), end = androidx.compose.ui.geometry.Offset(margin + length, size.height - margin), strokeWidth = stroke)
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(margin, size.height - margin), end = androidx.compose.ui.geometry.Offset(margin, size.height - margin - length), strokeWidth = stroke)

                        // Bottom-Right corner
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width - margin, size.height - margin), end = androidx.compose.ui.geometry.Offset(size.width - margin - length, size.height - margin), strokeWidth = stroke)
                        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width - margin, size.height - margin), end = androidx.compose.ui.geometry.Offset(size.width - margin, size.height - margin - length), strokeWidth = stroke)
                    }

                    // Recording indicator
                    val recPulse by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "rec_blink"
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.65f), MaterialTheme.shapes.small)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (recPulse > 0.4f) Color.Red else Color.Transparent,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "REC",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            } else if (participant.id == "ai_agent") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridCount = 8
                        val stepX = size.width / gridCount
                        val stepY = size.height / gridCount
                        for (i in 1 until gridCount) {
                            drawLine(
                                color = Color.Cyan.copy(alpha = 0.08f),
                                start = androidx.compose.ui.geometry.Offset(i * stepX, 0f),
                                end = androidx.compose.ui.geometry.Offset(i * stepX, size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.Cyan.copy(alpha = 0.08f),
                                start = androidx.compose.ui.geometry.Offset(0f, i * stepY),
                                end = androidx.compose.ui.geometry.Offset(size.width, i * stepY),
                                strokeWidth = 1f
                            )
                        }
                    }

                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = EaseInOutQuad),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "orb_pulse"
                    )

                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(8000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "orb_rotation"
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(if (isSpeaking) scale * 1.1f else scale)
                            .rotate(rotation)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF00F2FE),
                                        Color(0xFF4FACFE),
                                        Color(0xFF00F2FE)
                                    )
                                ),
                                CircleShape
                             ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF0F2027), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Core",
                                tint = Color(0xFF00F2FE),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (isSpeaking) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .border(2.dp, Color(0xFF00F2FE).copy(alpha = 0.5f), CircleShape)
                                .scale(scale * 1.2f)
                        )
                    }
                }
            } else {
                // Remote participant camera feed preview
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val breathScale by infiniteTransition.animateFloat(
                        initialValue = 0.96f,
                        targetValue = 1.04f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "human_breath"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(breathScale)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = participant.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            radius = size.minDimension * 0.42f,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 8f), 0f
                                )
                            )
                        )
                    }

                    val liveBlink by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "live_blink"
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (liveBlink > 0.5f) Color.Red else Color.Gray,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = "Camera Off",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    if (participant.isHost) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Host",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f), MaterialTheme.shapes.small)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (participant.isMicOn) Color.Black.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (participant.isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = if (participant.isMicOn) "Unmuted" else "Muted",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRoomScreen(
    onNavigateToHome: () -> Unit,
    viewModel: MeetingRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.room?.title ?: "Meeting Room") },
                actions = {
                    Button(
                        onClick = { viewModel.leaveRoom(onNavigateToHome) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Leave")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.participants) { participant ->
                            ParticipantCameraFeed(
                                participant = participant,
                                modifier = Modifier.aspectRatio(1.3f)
                            )
                        }
                    }
                    
                    SubtitlesOverlay()
                }

                // Bottom control bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = viewModel::toggleMic,
                            modifier = Modifier.background(
                                if (uiState.mediaState.isMicEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                        ) {
                            Icon(
                                if (uiState.mediaState.isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Mic",
                                tint = if (uiState.mediaState.isMicEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        IconButton(
                            onClick = viewModel::toggleCamera,
                            modifier = Modifier.background(
                                if (uiState.mediaState.isCameraEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                        ) {
                            Icon(
                                if (uiState.mediaState.isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle Camera",
                                tint = if (uiState.mediaState.isCameraEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        IconButton(
                            onClick = viewModel::toggleScreenShare,
                            modifier = Modifier.background(
                                if (uiState.mediaState.isScreenSharing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                        ) {
                            Icon(
                                Icons.Default.ScreenShare,
                                contentDescription = "Toggle Screen Share",
                                tint = if (uiState.mediaState.isScreenSharing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(
                            onClick = viewModel::toggleChat,
                            modifier = Modifier.background(
                                if (uiState.isChatVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, 
                                CircleShape
                            )
                        ) {
                            Icon(
                                Icons.Default.Chat, 
                                contentDescription = "Toggle Chat",
                                tint = if (uiState.isChatVisible) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = viewModel::toggleAIAssistant,
                            modifier = Modifier.background(
                                if (uiState.isAIAssistantVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, 
                                CircleShape
                            )
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome, 
                                contentDescription = "AI Assistant",
                                tint = if (uiState.isAIAssistantVisible) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (uiState.isChatVisible) {
                ChatOverlay(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                )
            }
        }
        
        if (uiState.isAIAssistantVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.BottomCenter
            ) {
                com.aistudio.meetingplatform.feature.ai.presentation.AIAssistantPanel(
                    onClose = { viewModel.toggleAIAssistant() }
                )
            }
        }
    }
}
