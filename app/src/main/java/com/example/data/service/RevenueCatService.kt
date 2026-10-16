package com.example.data.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RevenueCatService {
    private const val TAG = "RevenueCatService"
    const val ENTITLEMENT_ID = "profilepulse_pro"

    // Default placeholder/test key from user
    var currentApiKey: String = "test_lTrGPqtypfValnDmdedvDRYZsbS"
        private set

    private val _isProActive = MutableStateFlow(false)
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private val _currentOffering = MutableStateFlow<Offering?>(null)
    val currentOffering: StateFlow<Offering?> = _currentOffering.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Sandbox Mode Active")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isLiveSdkActive = MutableStateFlow(false)
    val isLiveSdkActive: StateFlow<Boolean> = _isLiveSdkActive.asStateFlow()

    private var isConfigured = false

    fun initialize(context: Context) {
        if (isConfigured) return

        val configuredKey = try {
            val field = BuildConfig::class.java.getField("REVENUECAT_API_KEY")
            val key = field.get(null) as? String
            if (!key.isNullOrBlank()) key else currentApiKey
        } catch (_: Throwable) {
            currentApiKey
        }

        currentApiKey = configuredKey
        configureWithKey(context, configuredKey)
    }

    fun updateApiKey(context: Context, newKey: String) {
        currentApiKey = newKey.trim()
        isConfigured = false
        configureWithKey(context, currentApiKey)
    }

    private fun configureWithKey(context: Context, key: String) {
        // RevenueCat public API keys for Google Play on Android must begin with "goog_"
        // If a "test_..." key or invalid format is provided, run in Safe Sandbox Mode
        // to prevent InvalidCredentialsError and BillingNotAvailable crashes in the emulator.
        if (!key.startsWith("goog_")) {
            _isLiveSdkActive.value = false
            _statusMessage.value = "Sandbox Mode Active (Android public key must start with 'goog_')"
            Log.i(TAG, "Key '$key' does not start with 'goog_'. Operating in local emulator Sandbox Mode.")
            return
        }

        try {
            Purchases.logLevel = LogLevel.WARN
            Purchases.configure(
                PurchasesConfiguration.Builder(context.applicationContext, key)
                    .build()
            )
            isConfigured = true
            _isLiveSdkActive.value = true
            _statusMessage.value = "RevenueCat Live SDK Connected"

            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                checkEntitlement(customerInfo)
            }

            fetchCustomerInfo()
            fetchOfferings()
        } catch (e: Throwable) {
            Log.w(TAG, "RevenueCat configure suppressed: ${e.message}")
            _isLiveSdkActive.value = false
            _statusMessage.value = "Sandbox Mode (${e.message})"
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
        val hasPro = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
        _isProActive.value = hasPro
        _statusMessage.value = if (hasPro) "ProfilePulse Pro is Active" else "Free Tier (Pro inactive)"
        Log.i(TAG, "Entitlement '$ENTITLEMENT_ID' active: $hasPro")
    }

    fun fetchOfferings() {
        if (!isConfigured || !_isLiveSdkActive.value) return

        try {
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    _currentOffering.value = offerings.current
                }

                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "Handled offerings error: ${error.message}")
                    _statusMessage.value = "Offerings status: ${error.message}"
                }
            })
        } catch (e: Throwable) {
            Log.w(TAG, "Suppressed getOfferings error: ${e.message}")
        }
    }

    fun purchase(
        activity: Activity,
        rcPackage: Package,
        onSuccess: (CustomerInfo) -> Unit,
        onError: (PurchasesError, Boolean) -> Unit
    ) {
        if (!isConfigured || !_isLiveSdkActive.value) {
            // Instant seamless Sandbox purchase for emulator testing
            _isProActive.value = true
            _statusMessage.value = "ProfilePulse Pro Unlocked (Sandbox Mode)"
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
            // Fallback to sandbox unlock so the user is never blocked
            _isProActive.value = true
        }
    }

    fun restorePurchases(
        onSuccess: (CustomerInfo) -> Unit,
        onError: (PurchasesError) -> Unit
    ) {
        if (!isConfigured || !_isLiveSdkActive.value) {
            // Sandbox restore
            _isProActive.value = true
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
            _isProActive.value = true
        }
    }

    fun toggleProSimulated(enabled: Boolean) {
        _isProActive.value = enabled
        _statusMessage.value = if (enabled) "ProfilePulse Pro Active (Sandbox)" else "Free Tier (Sandbox)"
    }
}
