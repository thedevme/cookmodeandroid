package io.designtoswiftui.cookmode.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color palette - Industrial warmth
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val SurfaceLight = Color(0xFF2A2A2A)
private val AccentAmber = Color(0xFFE8A449)
private val AccentAmberDim = Color(0xFF6B4D23)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)

@Composable
fun AddRecipeMethodScreen(
    isPro: Boolean,
    onBackClick: () -> Unit,
    onManualEntry: () -> Unit,
    onPasteFromClipboard: () -> Unit,
    onShowPaywall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with centered title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Add Recipe",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Spacer for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Method cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Manual Entry Card
                MethodCard(
                    icon = Icons.Outlined.EditNote,
                    title = "Manual Entry",
                    description = "Type steps yourself",
                    showChevron = true,
                    onClick = onManualEntry
                )

                // Paste from Clipboard Card
                MethodCard(
                    icon = Icons.Outlined.ContentPaste,
                    title = "Paste from Clipboard",
                    description = "Paste recipe text and auto-split steps",
                    isPro = true,
                    isLocked = !isPro,
                    onClick = {
                        if (isPro) {
                            onPasteFromClipboard()
                        } else {
                            onShowPaywall()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Recipe button at bottom
            Button(
                onClick = onManualEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber
                )
            ) {
                Text(
                    text = "Save Recipe",
                    color = BackgroundDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MethodCard(
    icon: ImageVector,
    title: String,
    description: String,
    isPro: Boolean = false,
    isLocked: Boolean = false,
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    val cardBackground = if (isPro && isLocked) {
        SurfaceDark
    } else {
        SurfaceDark
    }

    val borderModifier = if (!isPro) {
        Modifier.border(1.dp, SurfaceLight, RoundedCornerShape(16.dp))
    } else {
        Modifier.border(1.dp, SurfaceLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(borderModifier)
            .background(cardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLocked) SurfaceLight else AccentAmberDim),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isLocked) TextMuted else AccentAmber,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (isLocked) TextSecondary else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (isPro) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ProBadge()
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        // Chevron or Lock icon
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        } else if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AccentAmber.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "PRO",
            color = AccentAmber,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
