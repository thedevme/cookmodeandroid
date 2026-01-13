package io.designtoswiftui.cookmode

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CookModeApplication : Application() {

    companion object {
        // TODO: Replace with your RevenueCat API key
        private const val REVENUECAT_API_KEY = "YOUR_REVENUECAT_API_KEY"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize RevenueCat
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(this, REVENUECAT_API_KEY)
                .build()
        )
    }
}
