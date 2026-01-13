package io.designtoswiftui.cookmode.data

import androidx.room.Database
import androidx.room.RoomDatabase
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.Step

@Database(
    entities = [Recipe::class, Ingredient::class, Step::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
}
