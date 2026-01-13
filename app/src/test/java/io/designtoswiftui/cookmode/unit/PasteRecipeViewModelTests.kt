package io.designtoswiftui.cookmode.unit

import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.viewmodels.PasteRecipeViewModel
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
class PasteRecipeViewModelTests {

    private lateinit var repository: RecipeRepository
    private lateinit var viewModel: PasteRecipeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = PasteRecipeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== Numbered Pattern Tests =====

    @Test
    fun `parse numbered steps with dot pattern`() = runTest {
        val text = """
            1. Preheat oven to 350°F
            2. Mix flour and sugar
            3. Bake for 30 minutes
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertEquals("Preheat oven to 350°F", result[0])
        assertEquals("Mix flour and sugar", result[1])
        assertEquals("Bake for 30 minutes", result[2])
    }

    @Test
    fun `parse numbered steps with parenthesis pattern`() = runTest {
        val text = """
            1) Chop the onions
            2) Sauté in olive oil
            3) Add garlic and cook
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertEquals("Chop the onions", result[0])
        assertEquals("Sauté in olive oil", result[1])
        assertEquals("Add garlic and cook", result[2])
    }

    @Test
    fun `parse numbered steps with mixed whitespace`() = runTest {
        val text = """
            1.   First step with extra spaces
            2.Second step no space
            3.    Third step with tabs
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertEquals("First step with extra spaces", result[0])
        assertEquals("Second step no space", result[1])
        assertEquals("Third step with tabs", result[2])
    }

    // ===== Step X: Pattern Tests =====

    @Test
    fun `parse Step X colon format`() = runTest {
        val text = """
            Step 1: Gather all ingredients
            Step 2: Prepare the workstation
            Step 3: Begin cooking
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertEquals("Gather all ingredients", result[0])
        assertEquals("Prepare the workstation", result[1])
        assertEquals("Begin cooking", result[2])
    }

    @Test
    fun `parse step X dash format`() = runTest {
        val text = """
            Step 1 - Mix the dry ingredients
            Step 2 - Add wet ingredients
            Step 3 - Fold gently
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertEquals("Mix the dry ingredients", result[0])
        assertEquals("Add wet ingredients", result[1])
        assertEquals("Fold gently", result[2])
    }

    @Test
    fun `parse step format case insensitive`() = runTest {
        val text = """
            STEP 1: First action
            step 2: Second action
            Step 3: Third action
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertEquals("First action", result[0])
        assertEquals("Second action", result[1])
        assertEquals("Third action", result[2])
    }

    // ===== Paragraph Fallback Tests =====

    @Test
    fun `parse by paragraphs when no patterns found`() = runTest {
        val text = """
            First mix all the dry ingredients together in a large bowl until well combined.

            Then add the wet ingredients and stir until you have a smooth batter.

            Finally pour the batter into a greased pan and bake until golden brown.
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(3, result.size)
        assertTrue(result[0].contains("First mix"))
        assertTrue(result[1].contains("Then add"))
        assertTrue(result[2].contains("Finally pour"))
    }

    @Test
    fun `paragraph parsing handles single newlines within paragraph`() = runTest {
        val text = """
            Mix the flour
            and sugar together.

            Add the eggs
            and milk.
        """.trimIndent()

        val result = viewModel.smartParse(text)

        assertEquals(2, result.size)
        assertEquals("Mix the flour and sugar together.", result[0])
        assertEquals("Add the eggs and milk.", result[1])
    }

    // ===== Step Manipulation Tests =====

    @Test
    fun `merge adjacent steps combines instructions`() = runTest {
        viewModel.updateSourceText("1. First step\n2. Second step\n3. Third step")
        viewModel.parseText()

        val initialSteps = viewModel.uiState.value.parsedSteps
        assertEquals(3, initialSteps.size)

        val firstStepId = initialSteps[0].id
        viewModel.mergeSteps(firstStepId, withNextStep = true)

        val mergedSteps = viewModel.uiState.value.parsedSteps
        assertEquals(2, mergedSteps.size)
        assertTrue(mergedSteps[0].instruction.contains("First step"))
        assertTrue(mergedSteps[0].instruction.contains("Second step"))
    }

    @Test
    fun `delete step removes from list`() = runTest {
        viewModel.updateSourceText("1. Keep this\n2. Delete this\n3. Keep this too")
        viewModel.parseText()

        val initialSteps = viewModel.uiState.value.parsedSteps
        assertEquals(3, initialSteps.size)

        val secondStepId = initialSteps[1].id
        viewModel.deleteStep(secondStepId)

        val remainingSteps = viewModel.uiState.value.parsedSteps
        assertEquals(2, remainingSteps.size)
        assertEquals("Keep this", remainingSteps[0].instruction)
        assertEquals("Keep this too", remainingSteps[1].instruction)
    }

    @Test
    fun `edit step updates instruction`() = runTest {
        viewModel.updateSourceText("1. Original instruction")
        viewModel.parseText()

        val stepId = viewModel.uiState.value.parsedSteps[0].id
        viewModel.editStep(stepId, "Modified instruction")

        assertEquals("Modified instruction", viewModel.uiState.value.parsedSteps[0].instruction)
    }

    @Test
    fun `add manual step increases list`() = runTest {
        viewModel.updateSourceText("1. First step")
        viewModel.parseText()

        assertEquals(1, viewModel.uiState.value.parsedSteps.size)

        viewModel.addManualStep()

        assertEquals(2, viewModel.uiState.value.parsedSteps.size)
        assertTrue(viewModel.uiState.value.parsedSteps[1].isEditing)
    }

    @Test
    fun `reorder steps changes order`() = runTest {
        viewModel.updateSourceText("1. First\n2. Second\n3. Third")
        viewModel.parseText()

        viewModel.reorderSteps(0, 2)

        val reordered = viewModel.uiState.value.parsedSteps
        assertEquals("Second", reordered[0].instruction)
        assertEquals("Third", reordered[1].instruction)
        assertEquals("First", reordered[2].instruction)
    }

    // ===== State Tests =====

    @Test
    fun `canSave is false without title`() = runTest {
        viewModel.updateSourceText("1. A step")
        viewModel.parseText()

        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `canSave is false without steps`() = runTest {
        viewModel.updateTitle("My Recipe")

        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `canSave is true with title and steps`() = runTest {
        viewModel.updateTitle("My Recipe")
        viewModel.updateSourceText("1. A step")
        viewModel.parseText()

        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun `save recipe calls repository`() = runTest {
        viewModel.updateTitle("Test Recipe")
        viewModel.updateSourceText("1. Do this\n2. Do that")
        viewModel.parseText()

        coEvery { repository.saveRecipe(any(), any(), any()) } returns 1L

        viewModel.saveRecipe()
        advanceUntilIdle()

        coVerify { repository.saveRecipe(any(), any(), any()) }
        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `empty text produces empty steps`() = runTest {
        viewModel.updateSourceText("")
        viewModel.parseText()

        assertTrue(viewModel.uiState.value.parsedSteps.isEmpty())
        assertTrue(viewModel.uiState.value.hasParsed)
    }

    @Test
    fun `update source text clears hasParsed flag`() = runTest {
        viewModel.updateSourceText("1. Step")
        viewModel.parseText()
        assertTrue(viewModel.uiState.value.hasParsed)

        viewModel.updateSourceText("New text")
        assertFalse(viewModel.uiState.value.hasParsed)
    }

    @Test
    fun `stepCount reflects number of parsed steps`() = runTest {
        viewModel.updateSourceText("1. One\n2. Two\n3. Three\n4. Four")
        viewModel.parseText()

        assertEquals(4, viewModel.uiState.value.stepCount)
    }
}
