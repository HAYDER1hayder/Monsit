package com.eloueduniv.monsit.presentation.call.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDetailScreen(
    onBack: () -> Unit,
    viewModel: CallDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE1F5FE)
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            uiState.call?.let { call ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Header: Profile and Name
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Call with ${call.contactName}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Metadata: Duration, Time, Date
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetadataItem("Duration:", formatDuration(call.duration))
                            MetadataItem("Time:", formatTime(call.startTime))
                            MetadataItem("Date:", formatDate(call.startTime))
                        }
                    }

                    // Audio Player Section
                    item {
                        AudioPlayerSection(
                            isPlaying = uiState.isPlaying,
                            onPlayPause = { viewModel.onAction(CallDetailUiAction.OnPlayPause) },
                            currentPosition = uiState.currentPosition,
                            onSeek = { viewModel.onAction(CallDetailUiAction.OnSeek(it)) }
                        )
                    }

                    // Summary Section
                    item {
                        SectionTitle("SUMMARY")
                        SummaryCard(call.summary)
                    }

                    // Transcript Section
                    item {
                        SectionTitle("TRANSCRIPT")
                    }

                    val transcriptLines = call.transcript.split("\n").filter { it.isNotBlank() }
                    items(transcriptLines) { line ->
                        TranscriptBubble(line, call.contactName)
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun MetadataItem(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun AudioPlayerSection(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    currentPosition: Float,
    onSeek: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Waveform with Progress
            WaveformView(
                progress = currentPosition,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) { Icon(Icons.Default.Refresh, contentDescription = "Loop") }
                IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous") }
                
                Surface(
                    onClick = onPlayPause,
                    shape = CircleShape,
                    color = Color(0xFF1A237E),
                    contentColor = Color.White
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.padding(12.dp).size(32.dp)
                    )
                }

                IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, contentDescription = "Next") }
                IconButton(onClick = {}) { Icon(Icons.Default.Shuffle, contentDescription = "Shuffle") }
            }
        }
    }
}

@Composable
fun WaveformView(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = 40
        val barWidth = width / (barCount * 2)
        val random = Random(42)

        for (i in 0 until barCount) {
            val barHeight = (0.2f + random.nextFloat() * 0.8f) * height
            val x = i * barWidth * 2 + barWidth
            val yStart = (height - barHeight) / 2
            
            // Color based on progress
            val isPlayed = (i.toFloat() / barCount) < progress
            
            drawRect(
                color = if (isPlayed) Color(0xFF1A237E) else Color.Gray.copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(x, yStart),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            summary.split("\n").forEach { line ->
                if (line.isNotBlank()) {
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold)
                        Text(line.trim().removePrefix("•").trim())
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptBubble(line: String, contactName: String) {
    val isUser = line.startsWith("User:", ignoreCase = true) || line.startsWith("You:", ignoreCase = true)
    val speaker = if (isUser) "You" else contactName
    val content = line.substringAfter(":").trim()
    
    val bgColor = if (isUser) Color(0xFFBBDEFB) else Color(0xFFE0E0E0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "$speaker:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = content, fontSize = 14.sp)
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val minutes = (durationMillis / 1000) / 60
    val seconds = (durationMillis / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}

fun formatTime(startTime: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(startTime))
}

fun formatDate(startTime: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(startTime))
}
