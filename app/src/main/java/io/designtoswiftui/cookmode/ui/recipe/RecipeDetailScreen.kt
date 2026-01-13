package io.designtoswiftui.cookmode.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.RecipeIcon
import io.designtoswiftui.cookmode.viewmodels.RecipeDetailViewModel

// Color palette - Industrial warmth
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val AccentAmber = Color(0xFFE8A449)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    onNavigateBack: () -> Unit,
    onStartCooking: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = AccentAmber,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.recipe != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Hero image with overlay buttons
                    item {
                        HeroSection(
                            imageUri = uiState.recipe!!.imageUri,
                            iconName = uiState.recipe!!.iconName,
                            onBack = onNavigateBack,
                            onEdit = { onEditRecipe(recipeId) }
                        )
                    }

                    // Recipe title
                    item {
                        Text(
                            text = uiState.recipe!!.title,
                            color = TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 20.dp)
                        )
                    }

                    // Meta info (prep time + servings)
                    item {
                        MetaInfoRow(
                            prepTime = uiState.recipe!!.prepTime,
                            servings = uiState.recipe!!.servings
                        )
                    }

                    // Ingredients section
                    if (uiState.ingredients.isNotEmpty()) {
                        item {
                            SectionHeader(title = "INGREDIENTS")
                        }

                        item {
                            IngredientsSection(ingredients = uiState.ingredients)
                        }
                    }

                    // Preparation section
                    if (uiState.steps.isNotEmpty()) {
                        item {
                            SectionHeader(title = "PREPARATION")
                        }

                        itemsIndexed(uiState.steps) { index, step ->
                            StepItem(
                                stepNumber = index + 1,
                                instruction = step.instruction
                            )
                        }

                        // Bottom padding
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Start Cooking button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDark)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { onStartCooking(recipeId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentAmber
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Cooking",
                            color = BackgroundDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    imageUri: String?,
    iconName: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val recipeIcon = RecipeIcon.fromKey(iconName)
    val iconColor = Color(0xFF313131)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
            )

            // Show recipe icon when no image - centered
            if (imageUri == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = recipeIcon.drawableRes),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(iconColor),
                        modifier = Modifier.size(72.dp)
                    )
                }
            } else {
                // TODO: Add actual image loading with Coil when available
                // For now, show a placeholder gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SurfaceDark,
                                    BackgroundDark
                                )
                            )
                        )
                )

                // Gradient overlay at bottom for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    BackgroundDark
                                )
                            )
                        )
                )
            }

            // Top bar with back and edit buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Bottom border when no image
        if (imageUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceDark)
            )
        }
    }
}

@Composable
private fun MetaInfoRow(
    prepTime: Int,
    servings: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prepTime > 0) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$prepTime mins",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        if (prepTime > 0 && servings > 0) {
            Spacer(modifier = Modifier.width(20.dp))
        }

        if (servings > 0) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$servings servings",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 16.dp)
    )
}

@Composable
private fun IngredientsSection(ingredients: List<Ingredient>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ingredients.forEach { ingredient ->
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Amber bullet
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentAmber)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Row {
                    Text(
                        text = ingredient.amount,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ingredient.name,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    instruction: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step number in amber
        Text(
            text = String.format("%02d", stepNumber),
            color = AccentAmber,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = instruction,
            color = TextPrimary,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
