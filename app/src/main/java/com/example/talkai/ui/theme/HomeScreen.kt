package com.example.talkai.ui

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HomeScreen(
    isListening: Boolean,
    isSpeaking: Boolean,
    isNavigating: Boolean,
    emotion: String,
    aiReply: String,
    statusText: String,
    previewView: PreviewView,
    onTalkClicked: () -> Unit,
    onNavigationToggle: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Title
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Talk AI",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your AI Companion",
                color = Color(0xFF888888),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Emotion + Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Emotion chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1E2E)
                ) {
                    Text(
                        text = "Feeling: $emotion",
                        color = Color(0xFFBB86FC),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Navigation chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isNavigating) Color(0xFF1B5E20) else Color(0xFF1A1A1A),
                    onClick = onNavigationToggle
                ) {
                    Text(
                        text = if (isNavigating) "🧭 Navigating" else "🧭 Navigate",
                        color = if (isNavigating) Color.Green else Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Reply Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (aiReply.isEmpty()) "Tap the button and speak to me..." else aiReply,
                        color = if (aiReply.isEmpty()) Color(0xFF555555) else Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status text
            Text(
                text = statusText,
                color = Color(0xFF888888),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Big Talk Button
            Button(
                onClick = onTalkClicked,
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isListening -> Color(0xFFEF5350)
                        isSpeaking -> Color(0xFF42A5F5)
                        else -> Color(0xFF6200EE)
                    }
                ),
                elevation = ButtonDefaults.buttonElevation(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when {
                            isListening -> "🎤"
                            isSpeaking -> "🔊"
                            else -> "💬"
                        },
                        fontSize = 32.sp
                    )
                    Text(
                        text = when {
                            isListening -> "Listening"
                            isSpeaking -> "Speaking"
                            else -> "Tap to Talk"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hint commands
            Text(
                text = "Say: \"Find nearby hospital\" • \"What is around me?\" • \"Start navigation\"",
                color = Color(0xFF444444),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}