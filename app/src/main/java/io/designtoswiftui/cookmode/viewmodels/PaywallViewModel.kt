package io.designtoswiftui.cookmode.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.cookmode.data.PremiumManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val error: String? = null,
    val product: StoreProduct? = null
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {

    companion object {
        const val PRODUCT_ID = "cookmode_pro"
    }

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            try {
                Purchases.sharedInstance.getProducts(
                    listOf(PRODUCT_ID),
                    callback = object : com.revenuecat.purchases.interfaces.GetStoreProductsCallback {
                        override fun onReceived(storeProducts: List<StoreProduct>) {
                            val product = storeProducts.firstOrNull()
                            _uiState.update { it.copy(product = product) }
                        }

                        override fun onError(error: PurchasesError) {
                            _uiState.update { it.copy(error = "Failed to load product") }
                        }
                    }
                )
            } catch (e: Exception) {
                // RevenueCat not initialized
            }
        }
    }

    fun purchase(activity: Activity, onSuccess: () -> Unit) {
        val product = _uiState.value.product
        if (product == null) {
            _uiState.update { it.copy(error = "Product not available") }
            return
        }

        _uiState.update { it.copy(isPurchasing = true, error = null) }

        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, product).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    _uiState.update { it.copy(isPurchasing = false) }
                    premiumManager.onPurchaseComplete(customerInfo)
                    onSuccess()
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            error = if (userCancelled) null else error.message
                        )
                    }
                }
            }
        )
    }

    fun restorePurchases(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isRestoring = true, error = null) }

        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _uiState.update { it.copy(isRestoring = false) }

                val hasPro = customerInfo.entitlements[PremiumManager.PRO_ENTITLEMENT_ID]?.isActive == true
                if (hasPro) {
                    premiumManager.onPurchaseComplete(customerInfo)
                    onSuccess()
                } else {
                    _uiState.update { it.copy(error = "No purchases to restore") }
                }
            }

            override fun onError(error: PurchasesError) {
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        error = error.message
                    )
                }
            }
        })
    }
}
