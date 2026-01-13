package io.designtoswiftui.cookmode.unit

import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.RecipeWithDetails
import io.designtoswiftui.cookmode.models.Step
import io.designtoswiftui.cookmode.viewmodels.CookingViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class CookingViewModelTests {

    private lateinit var repository: RecipeRepository
    private lateinit var viewModel: CookingViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testRecipe = Recipe(id = 1L, title = "Test Recipe")
    private val testSteps = listOf(
        Step(id = 1, recipeId = 1L, instruction = "First step", timerSeconds = null, orderIndex = 0),
        Step(id = 2, recipeId = 1L, instruction = "Second step", timerSeconds = 60, orderIndex = 1),
        Step(id = 3, recipeId = 1L, instruction = "Third step", timerSeconds = 120, orderIndex = 2)
    )
    private val testRecipeWithDetails = RecipeWithDetails(
        recipe = testRecipe,
        ingredients = emptyList(),
        steps = testSteps
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = CookingViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is step 0`() = runTest {
        assertEquals(0, viewModel.uiState.value.currentStepIndex)
    }

    @Test
    fun `loadRecipe populates steps in order`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.steps.size)
        assertEquals("First step", state.steps[0].instruction)
        assertEquals("Second step", state.steps[1].instruction)
        assertEquals("Third step", state.steps[2].instruction)
        assertEquals("Test Recipe", state.recipeTitle)
    }

    @Test
    fun `nextStep increments index`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        viewModel.nextStep()

        assertEquals(1, viewModel.uiState.value.currentStepIndex)
    }

    @Test
    fun `nextStep does nothing on last step`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        // Navigate to last step
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.currentStepIndex)

        // Try to go past last step
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.currentStepIndex)
    }

    @Test
    fun `previousStep decrements index`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentStepIndex)

        viewModel.previousStep()
        assertEquals(0, viewModel.uiState.value.currentStepIndex)
    }

    @Test
    fun `previousStep does nothing on first step`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.currentStepIndex)
        viewModel.previousStep()
        assertEquals(0, viewModel.uiState.value.currentStepIndex)
    }

    @Test
    fun `isFirstStep computed correctly`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFirstStep)
        assertFalse(viewModel.uiState.value.isLastStep)

        viewModel.nextStep()
        assertFalse(viewModel.uiState.value.isFirstStep)
        assertFalse(viewModel.uiState.value.isLastStep)
    }

    @Test
    fun `isLastStep computed correctly`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        viewModel.nextStep()
        viewModel.nextStep()

        assertFalse(viewModel.uiState.value.isFirstStep)
        assertTrue(viewModel.uiState.value.isLastStep)
    }

    @Test
    fun `timer starts with correct duration`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        // Go to step with timer (60 seconds)
        viewModel.nextStep()

        assertEquals(60, viewModel.uiState.value.timerSecondsRemaining)

        viewModel.startTimer()
        assertTrue(viewModel.uiState.value.isTimerRunning)
    }

    @Test
    fun `timer pause preserves remaining time`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        // Go to step with timer
        viewModel.nextStep()
        viewModel.startTimer()

        // Let 5 seconds pass
        advanceTimeBy(5000L)

        viewModel.pauseTimer()

        assertFalse(viewModel.uiState.value.isTimerRunning)
        assertEquals(55, viewModel.uiState.value.timerSecondsRemaining)
    }

    @Test
    fun `timer reset restores original duration`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        // Go to step with timer (60 seconds)
        viewModel.nextStep()
        viewModel.startTimer()

        // Let some time pass
        advanceTimeBy(10000L)

        viewModel.resetTimer()

        assertFalse(viewModel.uiState.value.isTimerRunning)
        assertEquals(60, viewModel.uiState.value.timerSecondsRemaining)
    }

    @Test
    fun `goToStep navigates correctly`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        viewModel.goToStep(2)
        assertEquals(2, viewModel.uiState.value.currentStepIndex)
        assertEquals("Third step", viewModel.uiState.value.currentStep?.instruction)
    }

    @Test
    fun `progress calculated correctly`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        assertEquals(1f / 3f, viewModel.uiState.value.progress, 0.001f)
        assertEquals("1 of 3", viewModel.uiState.value.stepProgress)

        viewModel.nextStep()
        assertEquals(2f / 3f, viewModel.uiState.value.progress, 0.001f)
        assertEquals("2 of 3", viewModel.uiState.value.stepProgress)

        viewModel.nextStep()
        assertEquals(1f, viewModel.uiState.value.progress, 0.001f)
        assertEquals("3 of 3", viewModel.uiState.value.stepProgress)
    }

    @Test
    fun `timer display formats correctly`() = runTest {
        val stepsWithLongTimer = listOf(
            Step(id = 1, recipeId = 1L, instruction = "Step", timerSeconds = 125, orderIndex = 0)
        )
        val recipeWithLongTimer = RecipeWithDetails(
            recipe = testRecipe,
            ingredients = emptyList(),
            steps = stepsWithLongTimer
        )
        coEvery { repository.getRecipeWithDetails(1L) } returns recipeWithLongTimer

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        assertEquals("2:05", viewModel.uiState.value.timerDisplay)
    }

    @Test
    fun `step without timer has no timer display`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        // First step has no timer
        assertFalse(viewModel.uiState.value.hasTimer)
        assertEquals("", viewModel.uiState.value.timerDisplay)
    }

    @Test
    fun `navigating steps resets timer`() = runTest {
        coEvery { repository.getRecipeWithDetails(1L) } returns testRecipeWithDetails

        viewModel.loadRecipe(1L)
        advanceUntilIdle()

        // Go to step with 60 second timer
        viewModel.nextStep()
        viewModel.startTimer()

        // Let some time pass
        advanceTimeBy(10000L)
        assertEquals(50, viewModel.uiState.value.timerSecondsRemaining)

        // Go to next step (120 second timer)
        viewModel.nextStep()

        // Timer should be reset to the new step's duration
        assertFalse(viewModel.uiState.value.isTimerRunning)
        assertEquals(120, viewModel.uiState.value.timerSecondsRemaining)
    }
}
