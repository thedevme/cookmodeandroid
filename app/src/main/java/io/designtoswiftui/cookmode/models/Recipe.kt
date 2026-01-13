package io.designtoswiftui.cookmode.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val imageUri: String? = null,
    val prepTime: Int = 0,
    val servings: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
