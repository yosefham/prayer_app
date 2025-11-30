package com.example.prayertime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertime.R
import com.example.prayertime.model.PrayerData

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment

@Composable
fun MonthlyView(
    monthlyData: List<PrayerData>,
    monthDisplay: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                }
                Text(text = monthDisplay, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                }
            }
        }

        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                TableCell(text = stringResource(R.string.date), weight = 1.5f, isHeader = true)
                TableCell(text = stringResource(R.string.fajr), weight = 1f, isHeader = true)
                TableCell(text = stringResource(R.string.dhuhr), weight = 1f, isHeader = true)
                TableCell(text = stringResource(R.string.asr), weight = 1f, isHeader = true)
                TableCell(text = stringResource(R.string.maghrib), weight = 1f, isHeader = true)
                TableCell(text = stringResource(R.string.isha), weight = 1f, isHeader = true)
            }
        }

        // Rows
        items(monthlyData) { data ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                // Format date to be shorter, e.g., "01 Nov"
                val shortDate = data.date.readable.substringBeforeLast(" ")
                
                TableCell(text = shortDate, weight = 1.5f)
                TableCell(text = cleanTime(data.timings.Fajr), weight = 1f)
                TableCell(text = cleanTime(data.timings.Dhuhr), weight = 1f)
                TableCell(text = cleanTime(data.timings.Asr), weight = 1f)
                TableCell(text = cleanTime(data.timings.Maghrib), weight = 1f)
                TableCell(text = cleanTime(data.timings.Isha), weight = 1f)
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(4.dp),
        style = if (isHeader) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        fontSize = 10.sp,
        maxLines = 1
    )
}

fun cleanTime(time: String): String {
    return time.trim()
}
