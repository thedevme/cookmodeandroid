package io.designtoswiftui.cookmode.ui.cooking

import android.Manifest
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.models.Step
import io.designtoswiftui.cookmode.viewmodels.CookingViewModel
import kotlinx.coroutines.launch

// Color palette - Industrial warmth
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val SurfaceLight = Color(0xFF2A2A2A)
private val AccentAmber = Color(0xFFE8A449)
private val AccentAmberDim = Color(0xFF6B4D23)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)
private val SuccessGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
    recipeId: Long,
    onExit: () -> Unit,
    viewModel: CookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    var showStepList by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var previousStepIndex by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Notification permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
    }

    // Check notification permission on launch
    LaunchedEffect(Unit) {
        viewModel.checkNotificationPermission()
    }

    // Show permission dialog if needed
    LaunchedEffect(uiState.needsNotificationPermission) {
        if (uiState.needsNotificationPermission) {
            showPermissionDialog = true
        }
    }

    // Keep screen on
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.loadRecipe(recipeId)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Handle back button
    BackHandler {
        showExitDialog = true
    }

    // Track direction for animation
    val slideDirection = if (uiState.currentStepIndex > previousStepIndex) 1 else -1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF0A0A0A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with exit and step list
            TopBar(
                stepProgress = uiState.stepProgress,
                onExitClick = { showExitDialog = true },
                onStepListClick = { showStepList = true }
            )

            // Progress indicator
            ProgressBar(progress = uiState.progress)

            // Main content area - Step instruction
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState.currentStepIndex,
                    transitionSpec = {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> slideDirection * fullWidth },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(animationSpec = tween(300)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -slideDirection * fullWidth },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) + fadeOut(animationSpec = tween(150))
                            )
                    },
                    label = "step_animation"
                ) { stepIndex ->
                    previousStepIndex = stepIndex
                    StepContent(
                        step = uiState.steps.getOrNull(stepIndex),
                        stepNumber = stepIndex + 1
                    )
                }
            }

            // Timer section
            AnimatedVisibility(
                visible = uiState.hasTimer,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                TimerSection(
                    timerDisplay = uiState.timerDisplay,
                    isRunning = uiState.isTimerRunning,
                    onToggle = { viewModel.toggleTimer() },
                    onReset = { viewModel.resetTimer() }
                )
            }

            // Navigation buttons
            NavigationButtons(
                isFirstStep = uiState.isFirstStep,
                isLastStep = uiState.isLastStep,
                onPrevious = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() },
                onFinish = onExit
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Timer completion overlay
    AnimatedVisibility(
        visible = uiState.timerComplete,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        TimerCompleteOverlay(
            onDismiss = { viewModel.dismissTimerCompletion() }
        )
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = onExit,
            onDismiss = { showExitDialog = false }
        )
    }

    // Notification permission dialog
    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onAllow = {
                showPermissionDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDeny = {
                showPermissionDialog = false
                viewModel.onNotificationPermissionResult(false)
            }
        )
    }

    // Step list bottom sheet
    if (showStepList) {
        ModalBottomSheet(
            onDismissRequest = { showStepList = false },
            sheetState = sheetState,
            containerColor = SurfaceDark,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted)
                )
            }
        ) {
            StepListSheet(
                steps = uiState.steps,
                currentStepIndex = uiState.currentStepIndex,
                onStepClick = { index ->
                    viewModel.goToStep(index)
                    scope.launch {
                        sheetState.hide()
                        showStepList = false
                    }
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    stepProgress: String,
    onExitClick: () -> Unit,
    onStepListClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exit button
        IconButton(
            onClick = onExitClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit cooking mode",
                tint = TextSecondary
            )
        }

        // Step progress
        Text(
            text = stepProgress,
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
        )

        // Step list button
        IconButton(
            onClick = onStepListClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "View all steps",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = AccentAmber,
        trackColor = SurfaceLight,
        strokeCap = StrokeCap.Round
    )
}

@Composable
private fun StepContent(
    step: Step?,
    stepNumber: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentAmberDim)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "STEP $stepNumber",
                color = AccentAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main instruction text
        Text(
            text = step?.instruction ?: "",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun TimerSection(
    timerDisplay: String,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    val timerAlpha by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0.7f,
        animationSpec = tween(300),
        label = "timer_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer display
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(
                    width = 2.dp,
                    color = if (isRunning) AccentAmber else SurfaceLight,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .alpha(timerAlpha)
        ) {
            Text(
                text = timerDisplay,
                color = if (isRunning) AccentAmber else TextPrimary,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceLight)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset timer",
                    tint = TextSecondary
                )
            }

            // Play/Pause button
            Button(
                onClick = onToggle,
                modifier = Modifier
                    .height(56.dp)
                    .width(140.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) SurfaceLight else AccentAmber
                )
            ) {
                Text(
                    text = if (isRunning) "PAUSE" else "START",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (isRunning) TextPrimary else BackgroundDark
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Previous button
        Button(
            onClick = onPrevious,
            enabled = !isFirstStep,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceLight,
                disabledContainerColor = SurfaceDark
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (isFirstStep) TextMuted else TextPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BACK",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (isFirstStep) TextMuted else TextPrimary
            )
        }

        // Next/Finish button
        Button(
            onClick = if (isLastStep) onFinish else onNext,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastStep) AccentAmber else SurfaceLight
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(
                text = if (isLastStep) "DONE" else "NEXT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (isLastStep) BackgroundDark else TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isLastStep) BackgroundDark else TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StepListSheet(
    steps: List<Step>,
    currentStepIndex: Int,
    onStepClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "ALL STEPS",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(steps) { index, step ->
                val isCurrentStep = index == currentStepIndex
                val isPastStep = index < currentStepIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCurrentStep) AccentAmberDim else Color.Transparent
                        )
                        .border(
                            width = if (isCurrentStep) 2.dp else 1.dp,
                            color = if (isCurrentStep) AccentAmber
                            else if (isPastStep) TextMuted
                            else SurfaceLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onStepClick(index) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step number
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrentStep) AccentAmber
                                else if (isPastStep) TextMuted
                                else SurfaceLight
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrentStep) BackgroundDark else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Step instruction
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.instruction,
                            color = if (isPastStep) TextMuted else TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (isCurrentStep) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        step.timerSeconds?.let { seconds ->
                            val minutes = seconds / 60
                            val secs = seconds % 60
                            Text(
                                text = "Timer: %d:%02d".format(minutes, secs),
                                color = AccentAmber.copy(alpha = if (isPastStep) 0.5f else 1f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Exit Cooking Mode?",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Your progress will be lost. Are you sure you want to exit?",
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AccentAmber
                )
            ) {
                Text(
                    text = "EXIT",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text(
                    text = "CANCEL",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    )
}

@Composable
private fun TimerCompleteOverlay(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Timer Complete!",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap anywhere to continue",
                color = TextSecondary,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .height(56.dp)
                    .width(200.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber
                )
            ) {
                Text(
                    text = "CONTINUE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = BackgroundDark
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        containerColor = SurfaceDark,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentAmberDim),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AccentAmber,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Enable Notifications",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Allow notifications so you'll know when your cooking timer is done, even if your screen is off or you switch apps.",
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onAllow,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AccentAmber
                )
            ) {
                Text(
                    text = "ALLOW",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDeny,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text(
                    text = "NOT NOW",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    )
}
