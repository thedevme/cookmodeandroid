package io.designtoswiftui.cookmode.models

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithDetails(
    @Embedded
    val recipe: Recipe,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<Ingredient>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val steps: List<Step>
) {
    val sortedIngredients: List<Ingredient>
        get() = ingredients.sortedBy { it.orderIndex }

    val sortedSteps: List<Step>
        get() = steps.sortedBy { it.orderIndex }
}
