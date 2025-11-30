package com.example.prayertime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.prayertime.R
import com.example.prayertime.model.PrayerData

@Composable
fun DailyView(prayerData: PrayerData) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = prayerData.date.readable,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        PrayerItem(stringResource(R.string.fajr), prayerData.timings.Fajr)
        PrayerItem(stringResource(R.string.sunrise), prayerData.timings.Sunrise)
        PrayerItem(stringResource(R.string.dhuhr), prayerData.timings.Dhuhr)
        PrayerItem(stringResource(R.string.asr), prayerData.timings.Asr)
        PrayerItem(stringResource(R.string.maghrib), prayerData.timings.Maghrib)
        PrayerItem(stringResource(R.string.isha), prayerData.timings.Isha)
    }
}

@Composable
fun PrayerItem(name: String, time: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(text = time, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
