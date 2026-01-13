package io.designtoswiftui.cookmode.models

import androidx.annotation.DrawableRes
import io.designtoswiftui.cookmode.R

enum class RecipeIcon(
    val key: String,
    val label: String,
    @DrawableRes val drawableRes: Int
) {
    HOT_SOUP("hot_soup", "Soup", R.drawable.hot_soup),
    POT("pot", "Pot", R.drawable.pot),
    MIXING_BOWL("mixing_bowl", "Mixing Bowl", R.drawable.mixing_bowl),
    NOODLES_BOWL("noodles_bowl", "Noodles", R.drawable.noodles_bowl),
    CHICKEN("chicken", "Chicken", R.drawable.chicken),
    PAN("pan", "Pan", R.drawable.pan),
    FISH("fish", "Fish", R.drawable.fish),
    LEAF("leaf", "Vegetarian", R.drawable.leaf),
    CHEESE("cheese", "Cheese", R.drawable.cheese),
    BOLT("bolt", "Quick", R.drawable.bolt),
    CLOCK("clock", "Slow Cook", R.drawable.clock),
    SALAD_BOWL("salad_bowl", "Salad", R.drawable.salad_bowl),
    SNOWFLAKE("snowflake", "Cold", R.drawable.snowflake),
    FLAME("flame", "Grill", R.drawable.flame),
    PASTA_BOWL("pasta_bowl", "Pasta", R.drawable.pasta_bowl),
    STEAK("steak", "Meat", R.drawable.steak);

    companion object {
        fun fromKey(key: String): RecipeIcon {
            return entries.find { it.key == key } ?: HOT_SOUP
        }

        fun all(): List<RecipeIcon> = entries.toList()
    }
}
