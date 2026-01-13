package io.designtoswiftui.cookmode.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_settings")

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val IS_PREMIUM_KEY = booleanPreferencesKey("is_premium")
        const val PRO_ENTITLEMENT_ID = "pro"
        const val FREE_RECIPE_LIMIT = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Flow from DataStore for persistence
    val isPremiumFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PREMIUM_KEY] ?: false
    }

    init {
        // Check RevenueCat for current entitlement status
        refreshPremiumStatus()
    }

    fun refreshPremiumStatus() {
        _isLoading.value = true

        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val hasPro = customerInfo.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
                    _isPremium.value = hasPro
                    _isLoading.value = false

                    // Persist to DataStore
                    scope.launch {
                        updatePremiumStatus(hasPro)
                    }
                }

                override fun onError(error: PurchasesError) {
                    _isLoading.value = false
                    // Fall back to cached value from DataStore
                }
            })
        } catch (e: Exception) {
            // RevenueCat not initialized yet
            _isLoading.value = false
        }
    }

    private suspend fun updatePremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PREMIUM_KEY] = isPremium
        }
    }

    // Called after successful purchase
    fun onPurchaseComplete(customerInfo: CustomerInfo) {
        val hasPro = customerInfo.entitlements[PRO_ENTITLEMENT_ID]?.isActive == true
        _isPremium.value = hasPro

        scope.launch {
            updatePremiumStatus(hasPro)
        }
    }

    // For testing/debug - manually unlock pro
    suspend fun debugUnlock() {
        _isPremium.value = true
        updatePremiumStatus(true)
    }

    // Check if user can add more recipes
    fun canAddRecipe(currentRecipeCount: Int): Boolean {
        return _isPremium.value || currentRecipeCount < FREE_RECIPE_LIMIT
    }

    // Get remaining free recipes
    fun getRemainingFreeRecipes(currentRecipeCount: Int): Int {
        return (FREE_RECIPE_LIMIT - currentRecipeCount).coerceAtLeast(0)
    }
}
