package io.designtoswiftui.cookmode.unit

import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.RecipeWithDetails
import io.designtoswiftui.cookmode.models.Step
import io.designtoswiftui.cookmode.viewmodels.EditRecipeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditRecipeViewModelTests {

    private lateinit var repository: RecipeRepository
    private lateinit var viewModel: EditRecipeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = EditRecipeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty title fails validation`() = runTest {
        // Initial state has empty title
        val state = viewModel.uiState.value

        assertFalse(state.isValid)
        assertTrue(state.validationErrors.contains("Title is required"))
    }

    @Test
    fun `no steps fails validation`() = runTest {
        viewModel.updateTitle("Test Recipe")

        // Remove the default step by clearing its instruction
        val state = viewModel.uiState.value
        val stepId = state.steps.first().id
        viewModel.updateStep(stepId, instruction = "")

        assertFalse(viewModel.uiState.value.isValid)
        assertTrue(viewModel.uiState.value.validationErrors.contains("At least one step is required"))
    }

    @Test
    fun `valid recipe passes validation`() = runTest {
        viewModel.updateTitle("Test Recipe")

        val state = viewModel.uiState.value
        val stepId = state.steps.first().id
        viewModel.updateStep(stepId, instruction = "Mix ingredients")

        assertTrue(viewModel.uiState.value.isValid)
        assertTrue(viewModel.uiState.value.validationErrors.isEmpty())
    }

    @Test
    fun `edit mode loads existing data`() = runTest {
        val recipe = Recipe(id = 1L, title = "Existing Recipe", prepTime = 30, servings = 4)
        val ingredients = listOf(
            Ingredient(id = 1, recipeId = 1L, amount = "2", unit = "cups", name = "flour", orderIndex = 0),
            Ingredient(id = 2, recipeId = 1L, amount = "1", unit = "tbsp", name = "sugar", orderIndex = 1)
        )
        val steps = listOf(
            Step(id = 1, recipeId = 1L, instruction = "Mix dry ingredients", timerSeconds = null, orderIndex = 0),
            Step(id = 2, recipeId = 1L, instruction = "Bake", timerSeconds = 1800, orderIndex = 1)
        )
        val recipeWithDetails = RecipeWithDetails(recipe, ingredients, steps)

        coEvery { repository.getRecipeWithDetails(1L) } returns recipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Existing Recipe", state.title)
        assertEquals(30, state.prepTimeMinutes)
        assertEquals(4, state.servings)
        assertEquals(2, state.ingredients.size)
        assertEquals("flour", state.ingredients[0].name)
        assertEquals("sugar", state.ingredients[1].name)
        assertEquals(2, state.steps.size)
        assertEquals("Mix dry ingredients", state.steps[0].instruction)
        assertEquals("Bake", state.steps[1].instruction)
        assertTrue(state.steps[1].hasTimer)
        assertEquals(30, state.steps[1].timerMinutes)
        assertEquals(0, state.steps[1].timerSeconds)
        assertTrue(state.isEditMode)
    }

    @Test
    fun `add ingredient increases list size`() = runTest {
        val initialSize = viewModel.uiState.value.ingredients.size

        viewModel.addIngredient()

        assertEquals(initialSize + 1, viewModel.uiState.value.ingredients.size)
    }

    @Test
    fun `remove ingredient decreases list size`() = runTest {
        viewModel.addIngredient()
        val state = viewModel.uiState.value
        val ingredientToRemove = state.ingredients.last()

        viewModel.removeIngredient(ingredientToRemove.id)

        assertEquals(1, viewModel.uiState.value.ingredients.size)
    }

    @Test
    fun `ingredients reorder correctly`() = runTest {
        viewModel.addIngredient()
        viewModel.addIngredient()

        val state = viewModel.uiState.value
        viewModel.updateIngredient(state.ingredients[0].id, name = "First")
        viewModel.updateIngredient(state.ingredients[1].id, name = "Second")
        viewModel.updateIngredient(state.ingredients[2].id, name = "Third")

        viewModel.reorderIngredients(0, 2)

        val reorderedState = viewModel.uiState.value
        assertEquals("Second", reorderedState.ingredients[0].name)
        assertEquals("Third", reorderedState.ingredients[1].name)
        assertEquals("First", reorderedState.ingredients[2].name)
    }

    @Test
    fun `add step increases list size`() = runTest {
        val initialSize = viewModel.uiState.value.steps.size

        viewModel.addStep()

        assertEquals(initialSize + 1, viewModel.uiState.value.steps.size)
    }

    @Test
    fun `remove step decreases list size`() = runTest {
        viewModel.addStep()
        val state = viewModel.uiState.value
        val stepToRemove = state.steps.last()

        viewModel.removeStep(stepToRemove.id)

        assertEquals(1, viewModel.uiState.value.steps.size)
    }

    @Test
    fun `steps reorder correctly`() = runTest {
        viewModel.addStep()
        viewModel.addStep()

        val state = viewModel.uiState.value
        viewModel.updateStep(state.steps[0].id, instruction = "First")
        viewModel.updateStep(state.steps[1].id, instruction = "Second")
        viewModel.updateStep(state.steps[2].id, instruction = "Third")

        viewModel.reorderSteps(0, 2)

        val reorderedState = viewModel.uiState.value
        assertEquals("Second", reorderedState.steps[0].instruction)
        assertEquals("Third", reorderedState.steps[1].instruction)
        assertEquals("First", reorderedState.steps[2].instruction)
    }

    @Test
    fun `save recipe calls repository`() = runTest {
        viewModel.updateTitle("New Recipe")
        viewModel.updatePrepTime(15)
        viewModel.updateServings(2)

        val state = viewModel.uiState.value
        viewModel.updateStep(state.steps.first().id, instruction = "Do something")

        coEvery { repository.saveRecipe(any(), any(), any()) } returns 1L

        viewModel.saveRecipe()
        advanceUntilIdle()

        coVerify { repository.saveRecipe(any(), any(), any()) }
        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `delete recipe calls repository`() = runTest {
        val recipeWithDetails = RecipeWithDetails(
            recipe = Recipe(id = 1L, title = "Test"),
            ingredients = emptyList(),
            steps = listOf(Step(id = 1, recipeId = 1L, instruction = "Step", orderIndex = 0))
        )
        coEvery { repository.getRecipeWithDetails(1L) } returns recipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        viewModel.deleteRecipe()
        advanceUntilIdle()

        coVerify { repository.deleteRecipe(1L) }
        assertTrue(viewModel.uiState.value.deleteSuccess)
    }

    @Test
    fun `update title changes state`() = runTest {
        viewModel.updateTitle("My Recipe")

        assertEquals("My Recipe", viewModel.uiState.value.title)
    }

    @Test
    fun `update prep time changes state`() = runTest {
        viewModel.updatePrepTime(45)

        assertEquals(45, viewModel.uiState.value.prepTimeMinutes)
    }

    @Test
    fun `update servings changes state`() = runTest {
        viewModel.updateServings(6)

        assertEquals(6, viewModel.uiState.value.servings)
    }

    @Test
    fun `step timer calculates total seconds correctly`() = runTest {
        val state = viewModel.uiState.value
        val stepId = state.steps.first().id

        viewModel.updateStep(stepId, hasTimer = true, timerMinutes = 5, timerSeconds = 30)

        val step = viewModel.uiState.value.steps.first()
        assertEquals(330, step.timerTotalSeconds)
    }

    @Test
    fun `minimum one ingredient is maintained`() = runTest {
        val state = viewModel.uiState.value
        val ingredientId = state.ingredients.first().id

        viewModel.removeIngredient(ingredientId)

        assertEquals(1, viewModel.uiState.value.ingredients.size)
    }

    @Test
    fun `minimum one step is maintained`() = runTest {
        val state = viewModel.uiState.value
        val stepId = state.steps.first().id

        viewModel.removeStep(stepId)

        assertEquals(1, viewModel.uiState.value.steps.size)
    }

    @Test
    fun `canSave is false when invalid`() = runTest {
        // Empty title - invalid
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `canSave is true when valid`() = runTest {
        viewModel.updateTitle("Test")
        val stepId = viewModel.uiState.value.steps.first().id
        viewModel.updateStep(stepId, instruction = "Do this")

        assertTrue(viewModel.uiState.value.canSave)
    }
}
