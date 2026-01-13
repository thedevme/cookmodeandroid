package io.designtoswiftui.cookmode.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.designtoswiftui.cookmode.models.Step

@Dao
interface StepDao {

    @Insert
    suspend fun insert(step: Step): Long

    @Insert
    suspend fun insertAll(steps: List<Step>)

    @Update
    suspend fun update(step: Step)

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY orderIndex")
    suspend fun getStepsForRecipe(recipeId: Long): List<Step>

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)

    @Query("DELETE FROM steps WHERE id = :id")
    suspend fun deleteById(id: Long)
}
