package io.designtoswiftui.cookmode.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.designtoswiftui.cookmode.ui.cooking.CookingScreen
import io.designtoswiftui.cookmode.ui.home.HomeScreen
import io.designtoswiftui.cookmode.ui.recipe.AddRecipeMethodScreen
import io.designtoswiftui.cookmode.ui.recipe.EditRecipeScreen
import io.designtoswiftui.cookmode.ui.paywall.PaywallScreen
import io.designtoswiftui.cookmode.ui.recipe.PasteRecipeScreen
import io.designtoswiftui.cookmode.ui.recipe.RecipeDetailScreen
import io.designtoswiftui.cookmode.ui.recipe.SelectIconScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddMethod : Screen("add_method")
    data object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: Long): String = "recipe_detail/$recipeId"
    }
    data object EditRecipe : Screen("edit_recipe?recipeId={recipeId}") {
        fun createRoute(recipeId: Long? = null): String {
            return if (recipeId != null) "edit_recipe?recipeId=$recipeId" else "edit_recipe"
        }
    }
    data object SelectIcon : Screen("select_icon/{currentIcon}") {
        fun createRoute(currentIcon: String): String = "select_icon/$currentIcon"
    }
    data object PasteRecipe : Screen("paste_recipe")
    data object Cooking : Screen("cooking/{recipeId}") {
        fun createRoute(recipeId: Long): String = "cooking/$recipeId"
    }
    data object Paywall : Screen("paywall")
}

@Composable
fun CookModeNavGraph(
    navController: NavHostController = rememberNavController(),
    isPro: Boolean = false // TODO: Get from PremiumManager in Phase 7
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onAddRecipe = {
                    navController.navigate(Screen.AddMethod.route)
                },
                onEditRecipe = { recipeId ->
                    navController.navigate(Screen.EditRecipe.createRoute(recipeId))
                },
                onShowPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        // Recipe Detail Screen
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onStartCooking = { id ->
                    navController.navigate(Screen.Cooking.createRoute(id))
                },
                onEditRecipe = { id ->
                    navController.navigate(Screen.EditRecipe.createRoute(id))
                }
            )
        }

        // Add Recipe Method Chooser
        composable(Screen.AddMethod.route) {
            AddRecipeMethodScreen(
                isPro = isPro,
                onBackClick = { navController.popBackStack() },
                onManualEntry = {
                    navController.navigate(Screen.EditRecipe.createRoute()) {
                        popUpTo(Screen.AddMethod.route) { inclusive = true }
                    }
                },
                onPasteFromClipboard = {
                    navController.navigate(Screen.PasteRecipe.route) {
                        popUpTo(Screen.AddMethod.route) { inclusive = true }
                    }
                },
                onShowPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        // Edit/Add Recipe Screen
        composable(
            route = Screen.EditRecipe.route,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: -1L
            // Get selected icon from savedStateHandle (returned from SelectIconScreen)
            val selectedIcon by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selected_icon", null)
                .collectAsStateWithLifecycle()

            EditRecipeScreen(
                recipeId = if (recipeId == -1L) null else recipeId,
                selectedIconFromPicker = selectedIcon,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                onDeleteSuccess = { navController.popBackStack() },
                onSelectIcon = { currentIcon ->
                    navController.navigate(Screen.SelectIcon.createRoute(currentIcon))
                }
            )
        }

        // Select Icon Screen
        composable(
            route = Screen.SelectIcon.route,
            arguments = listOf(
                navArgument("currentIcon") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val currentIcon = backStackEntry.arguments?.getString("currentIcon") ?: "hot_soup"
            SelectIconScreen(
                selectedIconKey = currentIcon,
                onIconSelected = { iconKey ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_icon", iconKey)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Paste Recipe Screen
        composable(Screen.PasteRecipe.route) {
            PasteRecipeScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Cooking Mode Screen
        composable(
            route = Screen.Cooking.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            CookingScreen(
                recipeId = recipeId,
                onExit = { navController.popBackStack() }
            )
        }

        // Paywall Screen
        composable(Screen.Paywall.route) {
            PaywallScreen(
                onClose = { navController.popBackStack() },
                onPurchaseSuccess = { navController.popBackStack() }
            )
        }
    }
}
