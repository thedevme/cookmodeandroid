package io.designtoswiftui.cookmode.ui.recipe

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.models.RecipeIcon
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
    selectedIconFromPicker: String? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onSelectIcon: (String) -> Unit = {},
    viewModel: EditRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Load recipe if editing
    LaunchedEffect(recipeId) {
        recipeId?.let { viewModel.loadRecipe(it) }
    }

    // Handle icon selection from picker
    LaunchedEffect(selectedIconFromPicker) {
        if (selectedIconFromPicker != null) {
            viewModel.updateIconName(selectedIconFromPicker)
        }
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
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopBar(
                isEditMode = uiState.isEditMode,
                canSave = uiState.canSave,
                isSaving = uiState.isSaving,
                onClose = { showDiscardDialog = true },
                onSave = { viewModel.saveRecipe() }
            )

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Image section
                item {
                    ImageSection(
                        imageUri = uiState.imageUri,
                        isEditMode = uiState.isEditMode,
                        onAddPhoto = { /* TODO: Image picker */ },
                        onReplacePhoto = { /* TODO: Image picker */ }
                    )
                }

                // Icon selector row
                item {
                    IconSelectorRow(
                        selectedIconKey = uiState.iconName,
                        isEditMode = uiState.isEditMode,
                        onClick = { onSelectIcon(uiState.iconName) }
                    )
                }

                // Recipe title
                item {
                    RecipeTitleSection(
                        title = uiState.title,
                        onTitleChange = { viewModel.updateTitle(it) }
                    )
                }

                // General Information
                item {
                    GeneralInfoSection(
                        prepTime = uiState.prepTimeMinutes,
                        servings = uiState.servings,
                        onPrepTimeChange = { viewModel.updatePrepTime(it) },
                        onServingsChange = { viewModel.updateServings(it) }
                    )
                }

                // Ingredients section
                item {
                    IngredientsHeader(onAdd = { viewModel.addIngredient() })
                }

                itemsIndexed(
                    items = uiState.ingredients,
                    key = { _, ingredient -> ingredient.id }
                ) { index, ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onAmountChange = { viewModel.updateIngredient(ingredient.id, amount = it) },
                        onNameChange = { viewModel.updateIngredient(ingredient.id, name = it) },
                        onRemove = { viewModel.removeIngredient(ingredient.id) },
                        showRemove = uiState.ingredients.size > 1
                    )
                }

                // Steps section
                item {
                    StepsHeader()
                }

                itemsIndexed(
                    items = uiState.steps,
                    key = { _, step -> step.id }
                ) { index, step ->
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

                // Add step button
                item {
                    AddStepButton(onClick = { viewModel.addStep() })
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
    canSave: Boolean,
    isSaving: Boolean,
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextSecondary
            )
        }

        Text(
            text = if (isEditMode) "Edit Recipe" else "New Recipe",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        TextButton(
            onClick = onSave,
            enabled = canSave && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AccentAmber,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isEditMode) "Save" else "Create",
                    color = if (canSave) AccentAmber else TextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ImageSection(
    imageUri: String?,
    isEditMode: Boolean,
    onAddPhoto: () -> Unit,
    onReplacePhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(SurfaceDark)
    ) {
        // TODO: Add image display when Coil is added as a dependency
        // Centered placeholder content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No image added",
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        // Add Photo button at bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AccentAmber)
                .clickable(onClick = onAddPhoto)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Photo",
                    color = BackgroundDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun IconSelectorRow(
    selectedIconKey: String,
    isEditMode: Boolean,
    onClick: () -> Unit
) {
    val selectedIcon = RecipeIcon.fromKey(selectedIconKey)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = selectedIcon.drawableRes),
                contentDescription = selectedIcon.label,
                colorFilter = ColorFilter.tint(AccentAmber),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "RECIPE ICON",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isEditMode) "Change Icon" else "Choose Icon",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Select",
            tint = TextMuted
        )
    }
}

@Composable
private fun RecipeTitleSection(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "RECIPE TITLE",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            if (title.isEmpty()) {
                Text(
                    text = "e.g. Grandma's Apple Pie",
                    color = AccentAmber.copy(alpha = 0.5f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 24.sp,
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
private fun GeneralInfoSection(
    prepTime: Int,
    servings: Int,
    onPrepTimeChange: (Int) -> Unit,
    onServingsChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "General Information",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prep Time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prep Time",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(16.dp)
                ) {
                    if (prepTime == 0) {
                        Text(
                            text = "e.g. 20 min",
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = if (prepTime == 0) "" else "$prepTime min",
                        onValueChange = { text ->
                            val number = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                            onPrepTimeChange(number)
                        },
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

            // Servings
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Servings",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(16.dp)
                ) {
                    if (servings == 1) {
                        Text(
                            text = "e.g. 4 people",
                            color = TextMuted,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = if (servings == 1) "" else "$servings people",
                        onValueChange = { text ->
                            val number = text.filter { it.isDigit() }.toIntOrNull() ?: 1
                            onServingsChange(number.coerceAtLeast(1))
                        },
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
    }
}

@Composable
private fun IngredientsHeader(onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ingredients",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier
                .clickable(onClick = onAdd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = AccentAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Add",
                color = AccentAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: EditableIngredient,
    onAmountChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Drag handle
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = "Reorder",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Quantity
        Box(
            modifier = Modifier.width(60.dp)
        ) {
            if (ingredient.amount.isEmpty()) {
                Text(
                    text = "Qty",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = ingredient.amount,
                onValueChange = onAmountChange,
                textStyle = TextStyle(
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name
        Box(modifier = Modifier.weight(1f)) {
            if (ingredient.name.isEmpty()) {
                Text(
                    text = "Ingredient name",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = ingredient.name,
                onValueChange = onNameChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Remove button
        if (showRemove) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
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
private fun StepsHeader() {
    Text(
        text = "Preparation Steps",
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 12.dp)
    )
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Step header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentAmberDim)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STEP $stepNumber",
                    color = AccentAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Row {
                // Drag handle
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Reorder",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )

                if (showRemove) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete step",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Instruction text area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .padding(16.dp)
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

        // Timer section (simplified for now)
        if (step.hasTimer) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentAmberDim.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timer: ${step.timerMinutes}m ${step.timerSeconds}s",
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = AccentAmber.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = AccentAmber,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Step",
                color = AccentAmber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
