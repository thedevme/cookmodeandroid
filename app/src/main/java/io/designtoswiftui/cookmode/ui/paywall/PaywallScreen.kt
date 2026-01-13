package io.designtoswiftui.cookmode.ui.paywall

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.cookmode.viewmodels.PaywallViewModel

// Color palette
private val BackgroundDark = Color(0xFF0D0D0D)
private val SurfaceDark = Color(0xFF1A1A1A)
private val AccentAmber = Color(0xFFE8A449)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextMuted = Color(0xFF616161)

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            TopBar(
                onClose = onClose,
                onRestore = {
                    viewModel.restorePurchases {
                        onPurchaseSuccess()
                    }
                },
                isRestoring = uiState.isRestoring
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Hero section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unlock Your Full Kitchen",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "One purchase. Yours forever.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Features list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FeatureItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Unlimited Recipes",
                    description = "Free tier: 3 recipes"
                )

                FeatureItem(
                    icon = Icons.Default.ContentPaste,
                    title = "Paste from Clipboard",
                    description = "Auto-parse recipes"
                )

                FeatureItem(
                    icon = Icons.Default.Scale,
                    title = "Ingredient Scaling",
                    description = "½×, 2×, custom servings"
                )

                FeatureItem(
                    icon = Icons.Default.Timer,
                    title = "Multiple Timers per Step",
                    description = "Cook complex recipes easier"
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Purchase button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        activity?.let {
                            viewModel.purchase(it) {
                                onPurchaseSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentAmber
                    ),
                    enabled = !uiState.isPurchasing
                ) {
                    if (uiState.isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BackgroundDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Unlock Pro — $4.99",
                            color = BackgroundDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "One-time purchase. No subscription.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                // Error message
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.error!!,
                        color = Color(0xFFCF6679),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onClose: () -> Unit,
    onRestore: () -> Unit,
    isRestoring: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextPrimary
            )
        }

        TextButton(
            onClick = onRestore,
            enabled = !isRestoring
        ) {
            if (isRestoring) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = AccentAmber,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "RESTORE",
                    color = AccentAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentAmber,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}
