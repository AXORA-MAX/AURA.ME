package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CountDownTimer(
    initialHours: Int = 8,
    initialMinutes: Int = 24,
    initialSeconds: Int = 36,
    modifier: Modifier = Modifier
) {
    var totalSeconds by remember {
        mutableStateOf(initialHours * 3600 + initialMinutes * 60 + initialSeconds)
    }

    LaunchedEffect(Unit) {
        while (totalSeconds > 0) {
            delay(1000L)
            totalSeconds--
        }
    }

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TimeBlock(value = String.format("%02d", hours), label = "HRS")
        Text(
            text = ":",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AuraWhite
        )
        TimeBlock(value = String.format("%02d", minutes), label = "MIN")
        Text(
            text = ":",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AuraWhite
        )
        TimeBlock(value = String.format("%02d", seconds), label = "SEC")
    }
}

@Composable
fun ProfessionalMonoCountDown(
    initialHours: Int = 8,
    initialMinutes: Int = 24,
    initialSeconds: Int = 36,
    modifier: Modifier = Modifier
) {
    var totalSeconds by remember {
        mutableStateOf(initialHours * 3600 + initialMinutes * 60 + initialSeconds)
    }

    LaunchedEffect(Unit) {
        while (totalSeconds > 0) {
            delay(1000L)
            totalSeconds--
        }
    }

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                letterSpacing = 1.sp,
                fontSize = 18.sp,
                lineHeight = 18.sp
            ),
            color = AuraWhite
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "HOURS : MINS : SECS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 7.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = AuraWhite.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun TimeBlock(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AuraNearBlack)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 18.sp
                ),
                color = AuraWhite
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = AuraTextSub
        )
    }
}
