package io.designtoswiftui.cookmode.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.cookmode.data.repository.RecipeRepository
import io.designtoswiftui.cookmode.ui.cooking.CookingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CookingViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookingState())
    val uiState: StateFlow<CookingState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val recipeWithDetails = repository.getRecipeWithDetails(recipeId)
                if (recipeWithDetails != null) {
                    val sortedSteps = recipeWithDetails.sortedSteps
                    _uiState.update {
                        it.copy(
                            recipeTitle = recipeWithDetails.recipe.title,
                            steps = sortedSteps,
                            currentStepIndex = 0,
                            isLoading = false,
                            timerSecondsRemaining = sortedSteps.firstOrNull()?.timerSeconds
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Recipe not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load recipe"
                    )
                }
            }
        }
    }

    fun nextStep() {
        val currentState = _uiState.value
        if (!currentState.isLastStep) {
            stopTimer()
            val newIndex = currentState.currentStepIndex + 1
            val newStep = currentState.steps.getOrNull(newIndex)
            _uiState.update {
                it.copy(
                    currentStepIndex = newIndex,
                    isTimerRunning = false,
                    timerSecondsRemaining = newStep?.timerSeconds
                )
            }
        }
    }

    fun previousStep() {
        val currentState = _uiState.value
        if (!currentState.isFirstStep) {
            stopTimer()
            val newIndex = currentState.currentStepIndex - 1
            val newStep = currentState.steps.getOrNull(newIndex)
            _uiState.update {
                it.copy(
                    currentStepIndex = newIndex,
                    isTimerRunning = false,
                    timerSecondsRemaining = newStep?.timerSeconds
                )
            }
        }
    }

    fun goToStep(index: Int) {
        val currentState = _uiState.value
        if (index in currentState.steps.indices) {
            stopTimer()
            val newStep = currentState.steps.getOrNull(index)
            _uiState.update {
                it.copy(
                    currentStepIndex = index,
                    isTimerRunning = false,
                    timerSecondsRemaining = newStep?.timerSeconds
                )
            }
        }
    }

    fun startTimer() {
        val currentState = _uiState.value
        if (!currentState.hasTimer) return

        val remainingSeconds = currentState.timerSecondsRemaining
            ?: currentState.currentStepTimerSeconds
            ?: return

        if (remainingSeconds <= 0) return

        _uiState.update { it.copy(isTimerRunning = true) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = remainingSeconds
            while (seconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000L)
                seconds--
                _uiState.update { it.copy(timerSecondsRemaining = seconds) }
            }

            if (seconds == 0) {
                _uiState.update { it.copy(isTimerRunning = false) }
                onTimerComplete()
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        stopTimer()
        val originalDuration = _uiState.value.currentStepTimerSeconds
        _uiState.update {
            it.copy(
                timerSecondsRemaining = originalDuration,
                isTimerRunning = false
            )
        }
    }

    fun toggleTimer() {
        if (_uiState.value.isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    private fun onTimerComplete() {
        // Timer completed - notification will be handled by TimerService in Phase 5
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
