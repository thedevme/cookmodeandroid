package io.designtoswiftui.cookmode.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.RecipeIcon
import io.designtoswiftui.cookmode.viewmodels.HomeViewModel

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
fun HomeScreen(
    onRecipeClick: (Long) -> Unit,
    onAddRecipe: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    onShowPaywall: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recipeCount by viewModel.recipeCount.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    var isSearching by remember { mutableStateOf(false) }

    val isEmpty = recipes.isEmpty() && searchQuery.isEmpty()
    val hasNoResults = recipes.isEmpty() && searchQuery.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopBar(
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchClick = { isSearching = true },
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onCloseSearch = {
                    isSearching = false
                    viewModel.clearSearch()
                },
                onDebugAddRecipes = { viewModel.debugAddTestRecipes() }
            )

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceLight.copy(alpha = 0.5f))
            )

            // Content
            when {
                isEmpty -> {
                    EmptyState()
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

        // FAB - always visible
        FloatingActionButton(
            onClick = {
                if (viewModel.canAddRecipe()) {
                    onAddRecipe()
                } else {
                    onShowPaywall()
                }
            },
            containerColor = AccentAmber,
            contentColor = BackgroundDark,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add recipe",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopBar(
    isSearching: Boolean,
    searchQuery: String,
    onSearchClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onDebugAddRecipes: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearching) {
            // Search mode
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search",
                    tint = TextPrimary
                )
            }

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 18.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(AccentAmber),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search recipes...",
                                color = TextMuted,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        } else {
            // Normal mode
            Spacer(modifier = Modifier.width(48.dp))

            Text(
                text = "My Recipes",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onDebugAddRecipes
                    ),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPrimary
                )
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 100.dp)
    ) {
        // Section header
        item {
            Text(
                text = "RECENT RECIPES",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            )
        }

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

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = RecipeIcon.fromKey(recipe.iconName).drawableRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(AccentAmber),
                modifier = Modifier.size(28.dp)
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
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (recipe.prepTime > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.prepTime} mins",
                    color = TextMuted,
                    fontSize = 14.sp
                )
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
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            tint = AccentAmberDim,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No recipes yet",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap the + button to add your first recipe",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
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
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(56.dp)
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
