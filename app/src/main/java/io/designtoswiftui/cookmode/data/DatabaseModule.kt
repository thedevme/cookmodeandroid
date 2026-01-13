package io.designtoswiftui.cookmode.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cookmode_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideRecipeDao(database: AppDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideIngredientDao(database: AppDatabase): IngredientDao {
        return database.ingredientDao()
    }

    @Provides
    fun provideStepDao(database: AppDatabase): StepDao {
        return database.stepDao()
    }
}
