package io.designtoswiftui.cookmode.unit

import io.designtoswiftui.cookmode.data.IngredientDao
import io.designtoswiftui.cookmode.data.RecipeDao
import io.designtoswiftui.cookmode.data.StepDao
import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.RecipeWithDetails
import io.designtoswiftui.cookmode.models.Step
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RecipeRepositoryTests {

    private lateinit var recipeDao: RecipeDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var stepDao: StepDao
    private lateinit var repository: RecipeRepository

    @Before
    fun setup() {
        recipeDao = mockk(relaxed = true)
        ingredientDao = mockk(relaxed = true)
        stepDao = mockk(relaxed = true)
        repository = RecipeRepository(recipeDao, ingredientDao, stepDao)
    }

    @Test
    fun `save and retrieve recipe`() = runTest {
        val recipe = Recipe(title = "Test Recipe")
        val ingredients = listOf(
            Ingredient(recipeId = 0, amount = "1", unit = "cup", name = "flour")
        )
        val steps = listOf(
            Step(recipeId = 0, instruction = "Mix ingredients")
        )

        coEvery { recipeDao.insert(any()) } returns 1L

        val recipeId = repository.saveRecipe(recipe, ingredients, steps)

        assertEquals(1L, recipeId)
        coVerify { recipeDao.insert(recipe) }

        val ingredientsSlot = slot<List<Ingredient>>()
        coVerify { ingredientDao.insertAll(capture(ingredientsSlot)) }
        assertEquals(1L, ingredientsSlot.captured[0].recipeId)
        assertEquals(0, ingredientsSlot.captured[0].orderIndex)

        val stepsSlot = slot<List<Step>>()
        coVerify { stepDao.insertAll(capture(stepsSlot)) }
        assertEquals(1L, stepsSlot.captured[0].recipeId)
        assertEquals(0, stepsSlot.captured[0].orderIndex)
    }

    @Test
    fun `update existing recipe clears old ingredients and steps`() = runTest {
        val recipe = Recipe(id = 1L, title = "Updated Recipe")
        val ingredients = listOf(
            Ingredient(recipeId = 1L, amount = "2", unit = "cups", name = "sugar")
        )
        val steps = listOf(
            Step(recipeId = 1L, instruction = "Updated instruction")
        )

        val recipeId = repository.saveRecipe(recipe, ingredients, steps)

        assertEquals(1L, recipeId)
        coVerify { recipeDao.update(recipe) }
        coVerify { ingredientDao.deleteByRecipeId(1L) }
        coVerify { stepDao.deleteByRecipeId(1L) }
        coVerify { ingredientDao.insertAll(any()) }
        coVerify { stepDao.insertAll(any()) }
    }

    @Test
    fun `delete removes recipe`() = runTest {
        repository.deleteRecipe(1L)

        coVerify { recipeDao.deleteById(1L) }
    }

    @Test
    fun `recipe count returns correct value`() = runTest {
        every { recipeDao.getRecipeCount() } returns flowOf(5)

        val count = repository.getRecipeCount().first()

        assertEquals(5, count)
    }

    @Test
    fun `get all recipes returns flow`() = runTest {
        val recipes = listOf(
            Recipe(id = 1L, title = "Recipe 1"),
            Recipe(id = 2L, title = "Recipe 2")
        )
        every { recipeDao.getAllRecipes() } returns flowOf(recipes)

        val result = repository.getAllRecipes().first()

        assertEquals(2, result.size)
        assertEquals("Recipe 1", result[0].title)
        assertEquals("Recipe 2", result[1].title)
    }

    @Test
    fun `get recipe with details returns full data`() = runTest {
        val recipe = Recipe(id = 1L, title = "Test Recipe")
        val ingredients = listOf(
            Ingredient(id = 1, recipeId = 1L, amount = "1", unit = "cup", name = "flour", orderIndex = 0)
        )
        val steps = listOf(
            Step(id = 1, recipeId = 1L, instruction = "Mix", orderIndex = 0)
        )
        val recipeWithDetails = RecipeWithDetails(recipe, ingredients, steps)

        coEvery { recipeDao.getRecipeWithDetails(1L) } returns recipeWithDetails

        val result = repository.getRecipeWithDetails(1L)

        assertEquals("Test Recipe", result?.recipe?.title)
        assertEquals(1, result?.ingredients?.size)
        assertEquals(1, result?.steps?.size)
    }

    @Test
    fun `get recipe with details returns null for non-existent recipe`() = runTest {
        coEvery { recipeDao.getRecipeWithDetails(999L) } returns null

        val result = repository.getRecipeWithDetails(999L)

        assertNull(result)
    }

    @Test
    fun `search recipes returns filtered results`() = runTest {
        val recipes = listOf(
            Recipe(id = 1L, title = "Chocolate Cake")
        )
        every { recipeDao.searchRecipes("Chocolate") } returns flowOf(recipes)

        val result = repository.searchRecipes("Chocolate").first()

        assertEquals(1, result.size)
        assertEquals("Chocolate Cake", result[0].title)
    }

    @Test
    fun `save recipe assigns correct order indices`() = runTest {
        val recipe = Recipe(title = "Test Recipe")
        val ingredients = listOf(
            Ingredient(recipeId = 0, amount = "1", unit = "cup", name = "flour"),
            Ingredient(recipeId = 0, amount = "2", unit = "tbsp", name = "sugar"),
            Ingredient(recipeId = 0, amount = "1", unit = "tsp", name = "salt")
        )
        val steps = listOf(
            Step(recipeId = 0, instruction = "First"),
            Step(recipeId = 0, instruction = "Second")
        )

        coEvery { recipeDao.insert(any()) } returns 1L

        repository.saveRecipe(recipe, ingredients, steps)

        val ingredientsSlot = slot<List<Ingredient>>()
        coVerify { ingredientDao.insertAll(capture(ingredientsSlot)) }
        assertEquals(0, ingredientsSlot.captured[0].orderIndex)
        assertEquals(1, ingredientsSlot.captured[1].orderIndex)
        assertEquals(2, ingredientsSlot.captured[2].orderIndex)

        val stepsSlot = slot<List<Step>>()
        coVerify { stepDao.insertAll(capture(stepsSlot)) }
        assertEquals(0, stepsSlot.captured[0].orderIndex)
        assertEquals(1, stepsSlot.captured[1].orderIndex)
    }
}
