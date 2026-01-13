package io.designtoswiftui.cookmode.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.designtoswiftui.cookmode.models.Ingredient

@Dao
interface IngredientDao {

    @Insert
    suspend fun insert(ingredient: Ingredient): Long

    @Insert
    suspend fun insertAll(ingredients: List<Ingredient>)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY orderIndex")
    suspend fun getIngredientsForRecipe(recipeId: Long): List<Ingredient>

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)
}
