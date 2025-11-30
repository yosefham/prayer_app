package com.example.prayertime.ui

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertime.model.PrayerData
import com.example.prayertime.network.RetrofitClient
import com.example.prayertime.utils.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.text.DateFormatSymbols

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(
        val daily: PrayerData?, 
        val monthly: List<PrayerData>?,
        val monthDisplay: String
    ) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
    object LocationMissing : PrayerUiState()
}


class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val locationManager = LocationManager(application)
    private val sharedPreferences = application.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
    private val geocoder = Geocoder(application, Locale.getDefault())
    
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    
    private val _uiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        loadSavedLocation()
    }

    private fun loadSavedLocation() {
        val latBits = sharedPreferences.getLong("lat", 0L)
        val longBits = sharedPreferences.getLong("long", 0L)

        if (latBits != 0L && longBits != 0L) {
            val lat = Double.fromBits(latBits)
            val long = Double.fromBits(longBits)
            fetchData(lat, long)
        } else {
            // Wait for explicit permission grant or call
            _uiState.value = PrayerUiState.LocationMissing
        }
    }

    fun fetchLocationAndData() {
        viewModelScope.launch {
            _uiState.value = PrayerUiState.Loading
            val location = locationManager.getCurrentLocation()
            if (location != null) {
                saveLocation(location)
                fetchData(location.latitude, location.longitude)
            } else {
                // If we have a saved location, use it, otherwise show missing
                val latBits = sharedPreferences.getLong("lat", 0L)
                if (latBits != 0L) {
                     val longBits = sharedPreferences.getLong("long", 0L)
                     fetchData(Double.fromBits(latBits), Double.fromBits(longBits))
                } else {
                    _uiState.value = PrayerUiState.LocationMissing
                }
            }
        }
    }

    fun searchLocation(query: String) {
        viewModelScope.launch {
            _uiState.value = PrayerUiState.Loading
            try {
                val addresses = withContext(Dispatchers.IO) {
                    try {
                        geocoder.getFromLocationName(query, 1)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val location = Location("manual").apply {
                        latitude = address.latitude
                        longitude = address.longitude
                    }
                    saveLocation(location)
                    fetchData(location.latitude, location.longitude)
                } else {
                    _uiState.value = PrayerUiState.Error("City not found")
                }
            } catch (e: Exception) {
                _uiState.value = PrayerUiState.Error(e.message ?: "Error searching location")
            }
        }
    }

    private fun saveLocation(location: Location) {
        sharedPreferences.edit()
            .putLong("lat", location.latitude.toBits())
            .putLong("long", location.longitude.toBits())
            .apply()
    }

    fun retry() {
        fetchLocationAndData()
    }

    fun changeMonth(amount: Int) {
        selectedMonth += amount
        if (selectedMonth > 12) {
            selectedMonth = 1
            selectedYear++
        } else if (selectedMonth < 1) {
            selectedMonth = 12
            selectedYear--
        }
        
        val latBits = sharedPreferences.getLong("lat", 0L)
        val longBits = sharedPreferences.getLong("long", 0L)
        if (latBits != 0L && longBits != 0L) {
            fetchData(Double.fromBits(latBits), Double.fromBits(longBits))
        }
    }

    private fun fetchData(lat: Double, long: Double) {
        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val dateStr = dateFormat.format(Date())
                
                val dailyResponse = RetrofitClient.apiService.getDailyTimings(dateStr, lat, long)
                val monthlyResponse = RetrofitClient.apiService.getMonthlyTimings(
                    lat, long, 
                    month = selectedMonth, 
                    year = selectedYear
                )

                val monthName = DateFormatSymbols().months[selectedMonth - 1]
                val monthDisplay = "$monthName $selectedYear"

                _uiState.value = PrayerUiState.Success(
                    daily = dailyResponse.data,
                    monthly = monthlyResponse.data,
                    monthDisplay = monthDisplay
                )
            } catch (e: Exception) {
                _uiState.value = PrayerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
