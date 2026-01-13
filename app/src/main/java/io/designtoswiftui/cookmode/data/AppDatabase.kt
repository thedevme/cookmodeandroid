package io.designtoswiftui.cookmode.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.designtoswiftui.cookmode.models.Ingredient
import io.designtoswiftui.cookmode.models.Recipe
import io.designtoswiftui.cookmode.models.Step

@Database(
    entities = [Recipe::class, Ingredient::class, Step::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipes ADD COLUMN iconName TEXT NOT NULL DEFAULT 'default'")
            }
        }
    }
}
