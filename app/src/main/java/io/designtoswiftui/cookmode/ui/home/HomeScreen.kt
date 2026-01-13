package io.designtoswiftui.cookmode.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.viewmodels.HomeViewModel

// Color palette - Industrial warmth (matching CookingScreen)
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val SurfaceLight = Color(0xFF2A2A2A)
private val AccentAmber = Color(0xFFE8A449)
private val AccentAmberDim = Color(0xFF6B4D23)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)
private val DangerRed = Color(0xFFCF6679)

// Recipe emoji mapping based on title keywords
private fun getRecipeEmoji(title: String): String {
    val lowerTitle = title.lowercase()
    return when {
        lowerTitle.contains("pasta") || lowerTitle.contains("spaghetti") -> "🍝"
        lowerTitle.contains("pizza") -> "🍕"
        lowerTitle.contains("salad") -> "🥗"
        lowerTitle.contains("soup") -> "🍲"
        lowerTitle.contains("cake") || lowerTitle.contains("dessert") -> "🍰"
        lowerTitle.contains("cookie") -> "🍪"
        lowerTitle.contains("bread") -> "🍞"
        lowerTitle.contains("chicken") -> "🍗"
        lowerTitle.contains("fish") || lowerTitle.contains("salmon") -> "🐟"
        lowerTitle.contains("steak") || lowerTitle.contains("beef") -> "🥩"
        lowerTitle.contains("burger") -> "🍔"
        lowerTitle.contains("taco") || lowerTitle.contains("mexican") -> "🌮"
        lowerTitle.contains("sushi") || lowerTitle.contains("japanese") -> "🍣"
        lowerTitle.contains("curry") || lowerTitle.contains("indian") -> "🍛"
        lowerTitle.contains("rice") -> "🍚"
        lowerTitle.contains("egg") || lowerTitle.contains("breakfast") -> "🍳"
        lowerTitle.contains("smoothie") || lowerTitle.contains("drink") -> "🥤"
        lowerTitle.contains("coffee") -> "☕"
        lowerTitle.contains("pie") -> "🥧"
        lowerTitle.contains("sandwich") -> "🥪"
        else -> "🍽️"
    }
}

@Composable
fun HomeScreen(
    onRecipeClick: (Long) -> Unit,
    onAddRecipe: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recipeCount by viewModel.recipeCount.collectAsState()

    val isEmpty = recipes.isEmpty() && searchQuery.isEmpty()
    val hasNoResults = recipes.isEmpty() && searchQuery.isNotEmpty()

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
            // Header
            Header(recipeCount = recipeCount)

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onClear = { viewModel.clearSearch() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            when {
                isEmpty -> {
                    EmptyState(onAddRecipe = onAddRecipe)
                }
                hasNoResults -> {
                    NoResultsState(query = searchQuery)
                }
                else -> {
                    RecipeList(
                        recipes = recipes,
                        onRecipeClick = onRecipeClick,
                        onEditRecipe = onEditRecipe,
                        onDeleteRecipe = { viewModel.deleteRecipe(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // FAB
        AnimatedVisibility(
            visible = !isEmpty,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = onAddRecipe,
                containerColor = AccentAmber,
                contentColor = BackgroundDark,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add recipe",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun Header(recipeCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "CookMode",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (recipeCount == 0) "No recipes yet"
            else "$recipeCount recipe${if (recipeCount != 1) "s" else ""}",
            color = TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search recipes...",
                        color = TextMuted,
                        fontSize = 16.sp
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AccentAmber),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    onDeleteRecipe: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = recipes,
            key = { _, recipe -> recipe.id }
        ) { index, recipe ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteRecipe(recipe.id)
                        true
                    } else {
                        false
                    }
                }
            )

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50
                    )
                )
            ) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DangerRed.copy(alpha = 0.2f))
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = DangerRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete",
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true
                ) {
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onLongClick = { onEditRecipe(recipe.id) }
                    )
                }
            }
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getRecipeEmoji(recipe.title),
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Recipe info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recipe.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recipe.prepTime > 0) {
                    Text(
                        text = "${recipe.prepTime} min",
                        color = AccentAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (recipe.servings > 0) {
                        Text(
                            text = " · ",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                if (recipe.servings > 0) {
                    Text(
                        text = "${recipe.servings} serving${if (recipe.servings != 1) "s" else ""}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                if (recipe.prepTime == 0 && recipe.servings == 0) {
                    Text(
                        text = "Tap to cook",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun EmptyState(onAddRecipe: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large cooking pot emoji
        Text(
            text = "🍳",
            fontSize = 72.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Add your first recipe",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your recipes will appear here.\nTap the button below to get started.",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Add recipe button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AccentAmber)
                .clickable(onClick = onAddRecipe)
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADD RECIPE",
                    color = BackgroundDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun NoResultsState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            fontSize = 56.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No recipes found",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No recipes match \"$query\"",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
