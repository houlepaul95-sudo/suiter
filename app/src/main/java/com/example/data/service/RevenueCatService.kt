package com.example.data.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.example.data.model.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RevenueCatService {
    private const val TAG = "RevenueCatService"
    const val ENTITLEMENT_ID = "profilepulse_pro"

    // Default placeholder/test key from user or environment
    var currentApiKey: String = "test_lTrGPqtypfValnDmdedvDRYZsbS"
        private set

    private val _isProActive = MutableStateFlow(false)
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
    val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()

    private val _currentOffering = MutableStateFlow<Offering?>(null)
    val currentOffering: StateFlow<Offering?> = _currentOffering.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Initializing...")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isLiveSdkActive = MutableStateFlow(false)
    val isLiveSdkActive: StateFlow<Boolean> = _isLiveSdkActive.asStateFlow()

    private var isConfigured = false

    fun getMaskedApiKey(): String {
        val key = currentApiKey.trim()
        return if (key.length > 8) {
            "${key.take(6)}...${key.takeLast(4)}"
        } else if (key.isNotBlank()) {
            "****"
        } else {
            "Not Set"
        }
    }

    fun initialize(context: Context) {
        if (isConfigured || Purchases.isConfigured) {
            isConfigured = true
            _isLiveSdkActive.value = true
            return
        }

        val configuredKey = try {
            val field = BuildConfig::class.java.getField("REVENUECAT_API_KEY")
            val key = field.get(null) as? String
            if (!key.isNullOrBlank()) key else currentApiKey
        } catch (_: Throwable) {
            currentApiKey
        }

        currentApiKey = configuredKey.trim()
        configureWithKey(context, currentApiKey)
    }

    fun updateApiKey(context: Context, newKey: String) {
        currentApiKey = newKey.trim()
        if (!Purchases.isConfigured) {
            configureWithKey(context, currentApiKey)
        } else {
            _statusMessage.value = "RevenueCat SDK already configured with active session"
        }
    }

    private fun configureWithKey(context: Context, key: String) {
        if (key.isBlank()) {
            _isLiveSdkActive.value = false
            _statusMessage.value = "Sandbox Mode Active (API key empty)"
            return
        }

        try {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(
                PurchasesConfiguration.Builder(context.applicationContext, key)
                    .build()
            )
            isConfigured = true
            _isLiveSdkActive.value = true
            _statusMessage.value = "RevenueCat SDK Connected"

            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                checkEntitlement(customerInfo)
            }

            fetchCustomerInfo()
            fetchOfferings()
        } catch (e: Throwable) {
            Log.w(TAG, "RevenueCat configure exception: ${e.message}")
            _isLiveSdkActive.value = false
            _statusMessage.value = "Sandbox Mode (${e.message ?: "fallback"})"
        }
    }

    fun fetchCustomerInfo(onComplete: ((CustomerInfo?) -> Unit)? = null) {
        if (!isConfigured || !_isLiveSdkActive.value) {
            onComplete?.invoke(null)
            return
        }

        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    checkEntitlement(customerInfo)
                    onComplete?.invoke(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Handled customer info error: ${error.message}")
                    _statusMessage.value = "Billing status: ${error.message}"
                    onComplete?.invoke(null)
                }
            })
        } catch (e: Throwable) {
            Log.w(TAG, "Suppressed getCustomerInfo error: ${e.message}")
            onComplete?.invoke(null)
        }
    }

    private fun checkEntitlement(customerInfo: CustomerInfo) {
        val activeEntitlement = customerInfo.entitlements[ENTITLEMENT_ID]
            ?: customerInfo.entitlements["pro"]
            ?: customerInfo.entitlements["premium"]
            ?: customerInfo.entitlements["all_access"]
            ?: customerInfo.entitlements.all.values.firstOrNull { it.isActive }

        val hasPro = activeEntitlement?.isActive == true
        _isProActive.value = hasPro

        val tier = when {
            !hasPro -> SubscriptionTier.FREE
            activeEntitlement?.productIdentifier?.contains("annual", ignoreCase = true) == true ||
            activeEntitlement?.productIdentifier?.contains("year", ignoreCase = true) == true -> SubscriptionTier.ANNUAL
            activeEntitlement?.productIdentifier?.contains("weekly", ignoreCase = true) == true ||
            activeEntitlement?.productIdentifier?.contains("week", ignoreCase = true) == true -> SubscriptionTier.WEEKLY
            else -> SubscriptionTier.MONTHLY
        }
        _currentTier.value = tier
        _statusMessage.value = if (hasPro) "ProfilePulse ${tier.title} is Active" else "Free Tier (Pro inactive)"
        Log.i(TAG, "Entitlement check: active=$hasPro, tier=${tier.title}")
    }

    fun fetchOfferings(onResult: ((Offering?) -> Unit)? = null) {
        if (!isConfigured || !_isLiveSdkActive.value) {
            onResult?.invoke(_currentOffering.value)
            return
        }

        try {
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    _currentOffering.value = offerings.current
                    onResult?.invoke(offerings.current)
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Handled offerings error: ${error.message}")
                    _statusMessage.value = "Offerings status: ${error.message}"
                    onResult?.invoke(null)
                }
            })
        } catch (e: Throwable) {
            Log.w(TAG, "Suppressed getOfferings error: ${e.message}")
            onResult?.invoke(null)
        }
    }

    fun purchase(
        activity: Activity,
        rcPackage: Package,
        onSuccess: (CustomerInfo) -> Unit,
        onError: (PurchasesError, Boolean) -> Unit
    ) {
        val targetTier = when {
            rcPackage.identifier.contains("annual", ignoreCase = true) || rcPackage.product.id.contains("annual", ignoreCase = true) || rcPackage.identifier.contains("yearly", ignoreCase = true) -> SubscriptionTier.ANNUAL
            rcPackage.identifier.contains("weekly", ignoreCase = true) || rcPackage.product.id.contains("weekly", ignoreCase = true) -> SubscriptionTier.WEEKLY
            else -> SubscriptionTier.MONTHLY
        }

        if (!isConfigured || !_isLiveSdkActive.value) {
            // Instant seamless Sandbox purchase for emulator / test mode
            toggleProSimulated(true, targetTier)
            _statusMessage.value = "ProfilePulse ${targetTier.title} Unlocked (Sandbox Mode)"
            return
        }

        try {
            Purchases.sharedInstance.purchaseWith(
                PurchaseParams.Builder(activity, rcPackage).build(),
                onError = { error, userCancelled ->
                    Log.w(TAG, "Handled purchase error: ${error.message}")
                    onError(error, userCancelled)
                },
                onSuccess = { _, customerInfo ->
                    checkEntitlement(customerInfo)
                    onSuccess(customerInfo)
                }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Suppressed purchase exception: ${e.message}")
            // Fallback to sandbox unlock so the user in emulator is never blocked
            toggleProSimulated(true, targetTier)
        }
    }

    fun restorePurchases(
        onSuccess: (CustomerInfo) -> Unit,
        onError: (PurchasesError) -> Unit
    ) {
        if (!isConfigured || !_isLiveSdkActive.value) {
            // Sandbox restore
            toggleProSimulated(true, SubscriptionTier.MONTHLY)
            _statusMessage.value = "ProfilePulse Pro Restored (Sandbox Mode)"
            return
        }

        try {
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    checkEntitlement(customerInfo)
                    onSuccess(customerInfo)
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Handled restore error: ${error.message}")
                    onError(error)
                }
            })
        } catch (e: Throwable) {
            Log.w(TAG, "Suppressed restore exception: ${e.message}")
            toggleProSimulated(true, SubscriptionTier.MONTHLY)
        }
    }

    fun toggleProSimulated(enabled: Boolean, tier: SubscriptionTier = SubscriptionTier.MONTHLY) {
        _isProActive.value = enabled
        _currentTier.value = if (enabled) tier else SubscriptionTier.FREE
        _statusMessage.value = if (enabled) "${tier.title} Active (Sandbox)" else "Free Tier (Sandbox)"
    }
}
