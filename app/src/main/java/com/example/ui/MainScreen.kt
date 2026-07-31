package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ExportBankScreen
import com.example.ui.screens.PlayStudioScreen
import com.example.ui.screens.PresetOverviewScreen
import com.example.ui.screens.SoundEngineControlsScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.PresetViewModel

sealed class AppTab(val route: String, val title: String, val icon: ImageVector) {
    object Overview : AppTab("overview", "Preset Spec", Icons.Default.Tune)
    object Engine : AppTab("engine", "Engine Rack", Icons.Default.Equalizer)
    object Play : AppTab("play", "Play Studio", Icons.Default.MusicNote)
    object Bank : AppTab("bank", "Export Bank", Icons.Default.Download)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PresetViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf<AppTab>(AppTab.Overview) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CONCERT GRAND C7",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Gold60
                            )
                            Text(
                                text = "Steinway/Yamaha Sound Engine Workstation",
                                fontSize = 11.sp,
                                color = Color(0xFFB0A498)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MahoganyPanel,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MahoganyPanel,
                contentColor = Gold80
            ) {
                val tabs = listOf(AppTab.Overview, AppTab.Engine, AppTab.Play, AppTab.Bank)
                tabs.forEach { tab ->
                    val isSelected = selectedTab.route == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Gold80,
                            indicatorColor = BrassAccent,
                            unselectedIconColor = Color(0xFF8C7E72),
                            unselectedTextColor = Color(0xFF8C7E72)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MahoganyDark)
        ) {
            when (selectedTab) {
                AppTab.Overview -> PresetOverviewScreen(
                    viewModel = viewModel,
                    onNavigateToEngineControls = { selectedTab = AppTab.Engine },
                    onNavigateToPlayStudio = { selectedTab = AppTab.Play }
                )
                AppTab.Engine -> SoundEngineControlsScreen(
                    viewModel = viewModel
                )
                AppTab.Play -> PlayStudioScreen(
                    viewModel = viewModel
                )
                AppTab.Bank -> ExportBankScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
