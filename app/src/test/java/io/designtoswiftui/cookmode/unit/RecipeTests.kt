package io.designtoswiftui.cookmode.unit

import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.RecipeWithDetails
import io.designtoswiftui.cookmode.models.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTests {

    @Test
    fun `recipe creates with correct defaults`() {
        val recipe = Recipe(title = "Test Recipe")

        assertEquals(0L, recipe.id)
        assertEquals("Test Recipe", recipe.title)
        assertNull(recipe.imageUri)
        assertEquals(0, recipe.prepTime)
        assertEquals(1, recipe.servings)
        assertTrue(recipe.createdAt > 0)
    }

    @Test
    fun `recipe creates with all parameters`() {
        val timestamp = System.currentTimeMillis()
        val recipe = Recipe(
            id = 1L,
            title = "Full Recipe",
            imageUri = "content://image.jpg",
            prepTime = 30,
            servings = 4,
            createdAt = timestamp
        )

        assertEquals(1L, recipe.id)
        assertEquals("Full Recipe", recipe.title)
        assertEquals("content://image.jpg", recipe.imageUri)
        assertEquals(30, recipe.prepTime)
        assertEquals(4, recipe.servings)
        assertEquals(timestamp, recipe.createdAt)
    }

    @Test
    fun `ingredients sort by orderIndex`() {
        val recipe = Recipe(id = 1L, title = "Test")
        val ingredients = listOf(
            Ingredient(id = 1, recipeId = 1L, amount = "1", unit = "cup", name = "flour", orderIndex = 2),
            Ingredient(id = 2, recipeId = 1L, amount = "2", unit = "tbsp", name = "sugar", orderIndex = 0),
            Ingredient(id = 3, recipeId = 1L, amount = "1", unit = "tsp", name = "salt", orderIndex = 1)
        )
        val steps = emptyList<Step>()

        val recipeWithDetails = RecipeWithDetails(recipe, ingredients, steps)

        val sorted = recipeWithDetails.sortedIngredients
        assertEquals("sugar", sorted[0].name)
        assertEquals("salt", sorted[1].name)
        assertEquals("flour", sorted[2].name)
    }

    @Test
    fun `steps sort by orderIndex`() {
        val recipe = Recipe(id = 1L, title = "Test")
        val ingredients = emptyList<Ingredient>()
        val steps = listOf(
            Step(id = 1, recipeId = 1L, instruction = "Third step", orderIndex = 2),
            Step(id = 2, recipeId = 1L, instruction = "First step", orderIndex = 0),
            Step(id = 3, recipeId = 1L, instruction = "Second step", orderIndex = 1)
        )

        val recipeWithDetails = RecipeWithDetails(recipe, ingredients, steps)

        val sorted = recipeWithDetails.sortedSteps
        assertEquals("First step", sorted[0].instruction)
        assertEquals("Second step", sorted[1].instruction)
        assertEquals("Third step", sorted[2].instruction)
    }

    @Test
    fun `ingredient creates with correct defaults`() {
        val ingredient = Ingredient(
            recipeId = 1L,
            amount = "2",
            unit = "cups",
            name = "flour"
        )

        assertEquals(0L, ingredient.id)
        assertEquals(1L, ingredient.recipeId)
        assertEquals("2", ingredient.amount)
        assertEquals("cups", ingredient.unit)
        assertEquals("flour", ingredient.name)
        assertEquals(0, ingredient.orderIndex)
    }

    @Test
    fun `step creates with nullable timer`() {
        val stepWithTimer = Step(
            recipeId = 1L,
            instruction = "Bake for 10 minutes",
            timerSeconds = 600
        )

        val stepWithoutTimer = Step(
            recipeId = 1L,
            instruction = "Mix ingredients"
        )

        assertEquals(600, stepWithTimer.timerSeconds)
        assertNull(stepWithoutTimer.timerSeconds)
    }

    @Test
    fun `step creates with correct defaults`() {
        val step = Step(
            recipeId = 1L,
            instruction = "Mix well"
        )

        assertEquals(0L, step.id)
        assertEquals(1L, step.recipeId)
        assertEquals("Mix well", step.instruction)
        assertNull(step.timerSeconds)
        assertEquals(0, step.orderIndex)
    }
}
