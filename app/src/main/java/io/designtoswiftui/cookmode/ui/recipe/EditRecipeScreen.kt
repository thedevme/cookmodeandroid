package io.designtoswiftui.cookmode.ui.recipe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.viewmodels.EditRecipeViewModel
import io.designtoswiftui.cookmode.viewmodels.EditableIngredient
import io.designtoswiftui.cookmode.viewmodels.EditableStep

// Color palette - Industrial warmth
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val SurfaceLight = Color(0xFF2A2A2A)
private val AccentAmber = Color(0xFFE8A449)
private val AccentAmberDim = Color(0xFF6B4D23)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)
private val DangerRed = Color(0xFFCF6679)

@Composable
fun EditRecipeScreen(
    recipeId: Long? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: EditRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Load recipe if editing
    LaunchedEffect(recipeId) {
        recipeId?.let { viewModel.loadRecipe(it) }
    }

    // Handle save/delete success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaveSuccess()
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) onDeleteSuccess()
    }

    BackHandler {
        showDiscardDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Color(0xFF0A0A0A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopBar(
                isEditMode = uiState.isEditMode,
                onBack = { showDiscardDialog = true },
                onDelete = if (uiState.isEditMode) {{ showDeleteDialog = true }} else null
            )

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title section
                item {
                    SectionCard(title = "RECIPE DETAILS") {
                        // Title field
                        InputField(
                            label = "Recipe Title",
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            placeholder = "Enter recipe name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Prep time and servings row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NumberField(
                                label = "Prep Time",
                                value = uiState.prepTimeMinutes,
                                onValueChange = { viewModel.updatePrepTime(it) },
                                suffix = "min",
                                modifier = Modifier.weight(1f)
                            )

                            NumberField(
                                label = "Servings",
                                value = uiState.servings,
                                onValueChange = { viewModel.updateServings(it) },
                                suffix = "",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Ingredients section
                item {
                    SectionCard(
                        title = "INGREDIENTS",
                        onAdd = { viewModel.addIngredient() }
                    ) {
                        Column(
                            modifier = Modifier.animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.ingredients.forEachIndexed { index, ingredient ->
                                IngredientRow(
                                    ingredient = ingredient,
                                    onAmountChange = { viewModel.updateIngredient(ingredient.id, amount = it) },
                                    onUnitChange = { viewModel.updateIngredient(ingredient.id, unit = it) },
                                    onNameChange = { viewModel.updateIngredient(ingredient.id, name = it) },
                                    onRemove = { viewModel.removeIngredient(ingredient.id) },
                                    showRemove = uiState.ingredients.size > 1
                                )
                            }
                        }
                    }
                }

                // Steps section
                item {
                    SectionCard(
                        title = "STEPS",
                        onAdd = { viewModel.addStep() }
                    ) {
                        Column(
                            modifier = Modifier.animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            uiState.steps.forEachIndexed { index, step ->
                                StepRow(
                                    stepNumber = index + 1,
                                    step = step,
                                    onInstructionChange = { viewModel.updateStep(step.id, instruction = it) },
                                    onTimerToggle = { viewModel.updateStep(step.id, hasTimer = it) },
                                    onTimerMinutesChange = { viewModel.updateStep(step.id, timerMinutes = it) },
                                    onTimerSecondsChange = { viewModel.updateStep(step.id, timerSeconds = it) },
                                    onRemove = { viewModel.removeStep(step.id) },
                                    showRemove = uiState.steps.size > 1
                                )
                            }
                        }
                    }
                }

                // Validation errors
                item {
                    AnimatedVisibility(
                        visible = uiState.validationErrors.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DangerRed.copy(alpha = 0.1f))
                                .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            uiState.validationErrors.forEach { error ->
                                Text(
                                    text = "• $error",
                                    color = DangerRed,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, BackgroundDark)
                        )
                    )
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { viewModel.saveRecipe() },
                    enabled = uiState.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentAmber,
                        disabledContainerColor = AccentAmberDim
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BackgroundDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isEditMode) "UPDATE RECIPE" else "SAVE RECIPE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = BackgroundDark
                        )
                    }
                }
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentAmber)
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Delete Recipe?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecipe()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Discard Changes?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Any unsaved changes will be lost.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("DISCARD", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text("KEEP EDITING", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    isEditMode: Boolean,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextSecondary
            )
        }

        Text(
            text = if (isEditMode) "Edit Recipe" else "New Recipe",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = DangerRed
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    onAdd: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            if (onAdd != null) {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentAmberDim)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = AccentAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = TextMuted,
                    fontSize = 16.sp
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrease button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onValueChange((value - 1).coerceAtLeast(0)) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    color = TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Value display
            Text(
                text = if (suffix.isNotEmpty()) "$value $suffix" else value.toString(),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Increase button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onValueChange(value + 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = AccentAmber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: EditableIngredient,
    onAmountChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Amount
        SmallTextField(
            value = ingredient.amount,
            onValueChange = onAmountChange,
            placeholder = "Qty",
            modifier = Modifier.width(56.dp),
            keyboardType = KeyboardType.Number
        )

        // Unit
        SmallTextField(
            value = ingredient.unit,
            onValueChange = onUnitChange,
            placeholder = "Unit",
            modifier = Modifier.width(64.dp)
        )

        // Name
        SmallTextField(
            value = ingredient.name,
            onValueChange = onNameChange,
            placeholder = "Ingredient name",
            modifier = Modifier.weight(1f)
        )

        // Remove button
        if (showRemove) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceLight)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 14.sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(AccentAmber),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StepRow(
    stepNumber: Int,
    step: EditableStep,
    onInstructionChange: (String) -> Unit,
    onTimerToggle: (Boolean) -> Unit,
    onTimerMinutesChange: (Int) -> Unit,
    onTimerSecondsChange: (Int) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLight)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AccentAmberDim),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove step",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Instruction field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark)
                .padding(12.dp)
        ) {
            if (step.instruction.isEmpty()) {
                Text(
                    text = "Describe this step...",
                    color = TextMuted,
                    fontSize = 15.sp
                )
            }

            BasicTextField(
                value = step.instruction,
                onValueChange = onInstructionChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timer toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Timer",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Switch(
                checked = step.hasTimer,
                onCheckedChange = onTimerToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentAmber,
                    checkedTrackColor = AccentAmberDim,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceDark
                )
            )
        }

        // Timer duration picker
        AnimatedVisibility(
            visible = step.hasTimer,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Minutes
                    TimerPickerField(
                        label = "Minutes",
                        value = step.timerMinutes,
                        onValueChange = onTimerMinutesChange,
                        maxValue = 120,
                        modifier = Modifier.weight(1f)
                    )

                    // Seconds
                    TimerPickerField(
                        label = "Seconds",
                        value = step.timerSeconds,
                        onValueChange = onTimerSecondsChange,
                        maxValue = 59,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerPickerField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceDark),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onValueChange((value - 1).coerceAtLeast(0)) },
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "%02d".format(value),
                color = AccentAmber,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onValueChange((value + 1).coerceAtMost(maxValue)) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = AccentAmber, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
