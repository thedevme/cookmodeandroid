package io.designtoswiftui.cookmode.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.cookmode.models.RecipeIcon

// Color palette - Industrial warmth
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val AccentAmber = Color(0xFFE8A449)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextMuted = Color(0xFF616161)

@Composable
fun SelectIconScreen(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val icons = RecipeIcon.all()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Color(0xFF0A0A0A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Select Icon",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Spacer for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AccentAmber.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Icon grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(icons) { icon ->
                    val isSelected = icon.key == selectedIconKey
                    IconItem(
                        icon = icon,
                        isSelected = isSelected,
                        onClick = {
                            onIconSelected(icon.key)
                            onNavigateBack()
                        }
                    )
                }
            }

            // Help text
            Text(
                text = "TAP AN ICON TO SELECT",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }
    }
}

@Composable
private fun IconItem(
    icon: RecipeIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        AccentAmber,
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon.drawableRes),
            contentDescription = icon.label,
            colorFilter = ColorFilter.tint(AccentAmber),
            modifier = Modifier.size(32.dp)
        )
    }
}
