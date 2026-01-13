package io.designtoswiftui.cookmode.ui.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MergeType
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.viewmodels.ParsedStep
import io.designtoswiftui.cookmode.viewmodels.PasteRecipeViewModel

// Color palette - Industrial warmth
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val SurfaceLight = Color(0xFF2A2A2A)
private val SurfaceLighter = Color(0xFF333333)
private val AccentAmber = Color(0xFFE8A449)
private val AccentAmberDim = Color(0xFF6B4D23)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)
private val DangerRed = Color(0xFFCF6679)
private val ProGold = Color(0xFFFFD700)

@Composable
fun PasteRecipeScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: PasteRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

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
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopBar(
                onBackClick = onBackClick,
                canSave = uiState.canSave,
                isSaving = uiState.isSaving,
                onSave = { viewModel.saveRecipe() }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    HeaderSection()
                }

                // Title input (appears after parsing)
                if (uiState.hasParsed && uiState.parsedSteps.isNotEmpty()) {
                    item {
                        TitleInput(
                            title = uiState.title,
                            onTitleChange = { viewModel.updateTitle(it) }
                        )
                    }
                }

                // Source text input
                item {
                    SourceTextInput(
                        text = uiState.sourceText,
                        onTextChange = { viewModel.updateSourceText(it) },
                        onParse = { viewModel.parseText() },
                        hasParsed = uiState.hasParsed,
                        isParsing = uiState.isParsing
                    )
                }

                // Pro tip
                if (!uiState.hasParsed) {
                    item {
                        ProTip()
                    }
                }

                // Parsed steps header
                if (uiState.hasParsed && uiState.parsedSteps.isNotEmpty()) {
                    item {
                        ParsedStepsHeader(stepCount = uiState.stepCount)
                    }
                }

                // Parsed steps
                if (uiState.hasParsed) {
                    itemsIndexed(
                        items = uiState.parsedSteps,
                        key = { _, step -> step.id }
                    ) { index, step ->
                        ParsedStepCard(
                            step = step,
                            stepNumber = index + 1,
                            isFirst = index == 0,
                            isLast = index == uiState.parsedSteps.lastIndex,
                            onEdit = { viewModel.setStepEditing(step.id, true) },
                            onSaveEdit = { newText -> viewModel.editStep(step.id, newText) },
                            onCancelEdit = { viewModel.setStepEditing(step.id, false) },
                            onMergeWithNext = { viewModel.mergeSteps(step.id, withNextStep = true) },
                            onDelete = { viewModel.deleteStep(step.id) }
                        )
                    }

                    // Add step manually button
                    item {
                        AddStepButton(onClick = { viewModel.addManualStep() })
                    }
                }

                // Empty parse result
                if (uiState.hasParsed && uiState.parsedSteps.isEmpty()) {
                    item {
                        EmptyParseResult()
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Loading overlay
        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = AccentAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Saving recipe...",
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save button
        AnimatedVisibility(
            visible = canSave,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentAmber)
                    .clickable(enabled = !isSaving, onClick = onSave)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "SAVE",
                    color = BackgroundDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(
            text = "Paste Recipe",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Paste your recipe text below and we'll automatically parse it into steps",
            color = TextSecondary,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun TitleInput(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Recipe Title",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            if (title.isEmpty()) {
                Text(
                    text = "Enter recipe title...",
                    color = TextMuted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SourceTextInput(
    text: String,
    onTextChange: (String) -> Unit,
    onParse: () -> Unit,
    hasParsed: Boolean,
    isParsing: Boolean
) {
    Column {
        Text(
            text = if (hasParsed) "Source Text" else "Recipe Text",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (hasParsed) 120.dp else 200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    text = "Paste your recipe instructions here...\n\nExample:\n1. Preheat oven to 350°F\n2. Mix flour and sugar\n3. Bake for 30 minutes",
                    color = TextMuted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Parse button
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (text.isNotEmpty()) AccentAmber
                    else AccentAmberDim.copy(alpha = 0.5f)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = text.isNotEmpty() && !isParsing,
                    onClick = onParse
                )
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isParsing) {
                CircularProgressIndicator(
                    color = BackgroundDark,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (hasParsed) "RE-PARSE TEXT" else "PARSE TEXT",
                    color = if (text.isNotEmpty()) BackgroundDark else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ProTip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentAmberDim.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pro Tip",
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The parser works best with numbered steps (1, 2, 3) or \"Step X:\" format. You can also paste plain paragraphs and we'll split them automatically.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ParsedStepsHeader(stepCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Parsed Steps",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ProGold)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PRO",
                    color = BackgroundDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Text(
            text = "$stepCount step${if (stepCount != 1) "s" else ""}",
            color = TextMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ParsedStepCard(
    step: ParsedStep,
    stepNumber: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onMergeWithNext: () -> Unit,
    onDelete: () -> Unit
) {
    var editText by remember(step.id, step.isEditing) {
        mutableStateOf(step.instruction)
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(step.isEditing) {
        if (step.isEditing) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Column {
            // Step header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentAmberDim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        color = AccentAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action buttons
                if (!step.isEditing) {
                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (!isLast) {
                            IconButton(
                                onClick = onMergeWithNext,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MergeType,
                                    contentDescription = "Merge with next",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = DangerRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step content
            if (step.isEditing) {
                BasicTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(AccentAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceLight)
                        .padding(12.dp)
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceLighter)
                            .clickable(onClick = onCancelEdit)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentAmber)
                            .clickable(onClick = { onSaveEdit(editText) })
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = BackgroundDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = step.instruction,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun AddStepButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Step Manually",
                color = TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyParseResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📝",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No steps found",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try formatting your recipe with numbered steps\nor add steps manually below.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        AddStepButton(onClick = {})
    }
}
