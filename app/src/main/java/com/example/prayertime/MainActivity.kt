package com.example.prayertime

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prayertime.ui.DailyView
import com.example.prayertime.ui.MonthlyView
import com.example.prayertime.ui.PrayerUiState
import com.example.prayertime.ui.PrayerViewModel

import androidx.compose.ui.text.style.TextAlign
import com.example.prayertime.utils.LanguageHelper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Context

class MainActivity : ComponentActivity() {
    private val viewModel: PrayerViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrayerApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerApp(viewModel: PrayerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onLanguageClick = {
                showSettingsDialog = false
                showLanguageDialog = true
            },
            onSearchLocationClick = {
                showSettingsDialog = false
                showSearchDialog = true
            },
            onUseCurrentLocationClick = {
                showSettingsDialog = false
                viewModel.fetchLocationAndData()
            }
        )
    }

    if (showSearchDialog) {
        SearchLocationDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { query ->
                viewModel.searchLocation(query)
                showSearchDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                LanguageHelper.setLanguage(context, languageCode)
                if (context is Activity) {
                    context.recreate()
                }
                showLanguageDialog = false
            }
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.fetchLocationAndData()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("daily") {
                            popUpTo("daily") { inclusive = true }
                        }
                    },
                    label = { Text(stringResource(R.string.daily_view)) },
                    icon = { }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("monthly") {
                            popUpTo("daily")
                        }
                    },
                    label = { Text(stringResource(R.string.monthly_view)) },
                    icon = { }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is PrayerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PrayerUiState.Success -> {
                    NavHost(navController = navController, startDestination = "daily") {
                        composable("daily") {
                            state.daily?.let { DailyView(it) }
                        }
                        composable("monthly") {
                            state.monthly?.let { monthlyData ->
                                MonthlyView(
                                    monthlyData = monthlyData,
                                    monthDisplay = state.monthDisplay,
                                    onPrevious = { viewModel.changeMonth(-1) },
                                    onNext = { viewModel.changeMonth(1) }
                                )
                            }
                        }
                    }
                }
                is PrayerUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message)
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                is PrayerUiState.LocationMissing -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.location_permission_rationale))
                            Button(onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }) {
                                Text(stringResource(R.string.grant_permission))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onLanguageClick: () -> Unit,
    onSearchLocationClick: () -> Unit,
    onUseCurrentLocationClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column {
                SettingsItem(stringResource(R.string.change_language), onLanguageClick)
                SettingsItem(stringResource(R.string.change_location), onSearchLocationClick)
                SettingsItem(stringResource(R.string.use_current_location), onUseCurrentLocationClick)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsItem(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }
}

@Composable
fun SearchLocationDialog(onDismiss: () -> Unit, onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.change_location)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.enter_city)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSearch(text) }) {
                Text(stringResource(R.string.search))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.change_language)) },
        text = {
            Column {
                LanguageItem(stringResource(R.string.english), "en", onLanguageSelected)
                LanguageItem(stringResource(R.string.spanish), "es", onLanguageSelected)
                LanguageItem(stringResource(R.string.arabic), "ar", onLanguageSelected)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun LanguageItem(name: String, code: String, onClick: (String) -> Unit) {
    TextButton(
        onClick = { onClick(code) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(name)
    }
}
