package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * RevenueCatManager manages initialization of the RevenueCat Purchases SDK
 * using the API key loaded from .env via Secrets Gradle plugin / BuildConfig,
 * and handles fetching available offerings and product catalogs.
 */
class RevenueCatManager(private val context: Context? = null) {

    companion object {
        private const val TAG = "RevenueCatManager"

        @Volatile
        private var instance: RevenueCatManager? = null

        /**
         * Get or create the singleton instance of RevenueCatManager.
         */
        fun getInstance(context: Context? = null): RevenueCatManager {
            return instance ?: synchronized(this) {
                instance ?: RevenueCatManager(context?.applicationContext).also {
                    instance = it
                }
            }
        }

        /**
         * Initialize the Purchases SDK with the API key from .env (via BuildConfig.REVENUECAT_API_KEY).
         * Can be called directly from MainActivity or Application onCreate.
         */
        fun initialize(
            context: Context,
            apiKey: String = getApiKeyFromBuildConfig()
        ): RevenueCatManager {
            val manager = getInstance(context)
            manager.initPurchases(context.applicationContext, apiKey)
            return manager
        }

        /**
         * Retrieve the RevenueCat API Key injected from .env / .env.example
         * via the Secrets Gradle Plugin into BuildConfig.
         */
        fun getApiKeyFromBuildConfig(): String {
            return try {
                // BuildConfig.REVENUECAT_API_KEY is generated from .env file
                BuildConfig.REVENUECAT_API_KEY.ifBlank {
                    Log.w(TAG, "BuildConfig.REVENUECAT_API_KEY is blank in .env")
                    ""
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to read REVENUECAT_API_KEY from BuildConfig: ${e.message}")
                ""
            }
        }
    }

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _currentOffering = MutableStateFlow<Offering?>(null)
    val currentOffering: StateFlow<Offering?> = _currentOffering.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    /**
     * Initialize Purchases SDK using the provided context and API key.
     * Uses the API key from .env (injected into BuildConfig) if not specified.
     */
    fun initPurchases(
        appContext: Context,
        apiKey: String = getApiKeyFromBuildConfig()
    ) {
        if (Purchases.isConfigured) {
            Log.d(TAG, "RevenueCat Purchases SDK is already configured.")
            _isInitialized.value = true
            // Pre-fetch available offerings once configured
            fetchAvailableOfferings()
            return
        }

        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            val msg = "RevenueCat API key is empty. Please set REVENUECAT_API_KEY in your .env file."
            Log.e(TAG, msg)
            _lastError.value = msg
            return
        }

        try {
            // Enable detailed logs in debug mode
            Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO

            // Configure Purchases SDK with API key from .env
            val configuration = PurchasesConfiguration.Builder(appContext.applicationContext, trimmedKey)
                .build()

            Purchases.configure(configuration)
            _isInitialized.value = true
            _lastError.value = null
            Log.i(TAG, "RevenueCat Purchases SDK configured successfully with key from .env")

            // Automatically fetch initial available offerings
            fetchAvailableOfferings()
        } catch (e: Exception) {
            val errorMsg = "Exception configuring RevenueCat Purchases SDK: ${e.message}"
            Log.e(TAG, errorMsg, e)
            _lastError.value = errorMsg
        }
    }

    /**
     * Asynchronously fetch available offerings with callbacks.
     *
     * @param onSuccess Callback invoked when offerings are successfully retrieved.
     * @param onError Callback invoked when an error occurs fetching offerings.
     */
    fun fetchAvailableOfferings(
        onSuccess: ((Offerings) -> Unit)? = null,
        onError: ((PurchasesError) -> Unit)? = null
    ) {
        if (!Purchases.isConfigured) {
            val errorMsg = "Cannot fetch offerings: Purchases SDK is not configured yet."
            Log.w(TAG, errorMsg)
            _lastError.value = errorMsg
            onError?.invoke(
                PurchasesError(
                    com.revenuecat.purchases.PurchasesErrorCode.UnknownError,
                    errorMsg
                )
            )
            return
        }

        try {
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onReceived(offerings: Offerings) {
                    Log.i(TAG, "Offerings fetched successfully. Current offering: ${offerings.current?.identifier}, Total offerings: ${offerings.all.size}")
                    _offerings.value = offerings
                    _currentOffering.value = offerings.current
                    _lastError.value = null
                    onSuccess?.invoke(offerings)
                }

                override fun onError(error: PurchasesError) {
                    Log.e(TAG, "Error fetching offerings: ${error.code} - ${error.message}")
                    _lastError.value = error.message
                    onError?.invoke(error)
                }
            })
        } catch (e: Exception) {
            val msg = "Unexpected exception in fetchAvailableOfferings: ${e.message}"
            Log.e(TAG, msg, e)
            _lastError.value = msg
            onError?.invoke(
                PurchasesError(
                    com.revenuecat.purchases.PurchasesErrorCode.UnknownError,
                    msg
                )
            )
        }
    }

    /**
     * Suspending coroutine function to fetch available offerings.
     *
     * @return Result containing [Offerings] on success, or exception on failure.
     */
    suspend fun fetchAvailableOfferingsSuspend(): Result<Offerings> = suspendCancellableCoroutine { continuation ->
        fetchAvailableOfferings(
            onSuccess = { offerings ->
                if (continuation.isActive) {
                    continuation.resume(Result.success(offerings))
                }
            },
            onError = { error ->
                if (continuation.isActive) {
                    continuation.resume(Result.failure(Exception("${error.code}: ${error.message}")))
                }
            }
        )
    }

    /**
     * Helper function to get the current default offering if loaded.
     */
    fun getCurrentOffering(): Offering? {
        return _currentOffering.value ?: _offerings.value?.current
    }
}
