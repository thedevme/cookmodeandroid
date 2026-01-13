package io.designtoswiftui.cookmode.data.repository

import io.designtoswiftui.cookmode.data.IngredientDao
import io.designtoswiftui.cookmode.data.RecipeDao
import io.designtoswiftui.cookmode.data.StepDao
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.RecipeWithDetails
import io.designtoswiftui.cookmode.models.Step
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val stepDao: StepDao
) {

    suspend fun saveRecipe(
        recipe: Recipe,
        ingredients: List<Ingredient>,
        steps: List<Step>
    ): Long {
        val recipeId = if (recipe.id == 0L) {
            recipeDao.insert(recipe)
        } else {
            recipeDao.update(recipe)
            ingredientDao.deleteByRecipeId(recipe.id)
            stepDao.deleteByRecipeId(recipe.id)
            recipe.id
        }

        val ingredientsWithRecipeId = ingredients.mapIndexed { index, ingredient ->
            ingredient.copy(recipeId = recipeId, orderIndex = index)
        }
        ingredientDao.insertAll(ingredientsWithRecipeId)

        val stepsWithRecipeId = steps.mapIndexed { index, step ->
            step.copy(recipeId = recipeId, orderIndex = index)
        }
        stepDao.insertAll(stepsWithRecipeId)

        return recipeId
    }

    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
    }

    suspend fun getRecipeWithDetails(id: Long): RecipeWithDetails? {
        return recipeDao.getRecipeWithDetails(id)
    }

    suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteById(id)
    }

    fun getRecipeCount(): Flow<Int> {
        return recipeDao.getRecipeCount()
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query)
    }
}
