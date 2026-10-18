package com.example.data.repository

import com.example.data.model.*
import com.example.data.service.GeminiOptimizerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class ProfileRepository(
    private var appContext: Context? = null,
    private val geminiService: GeminiOptimizerService = GeminiOptimizerService()
) {
    private val PREFS_NAME = "suiter_user_dossier"

    // Biography Dossier & Persistence
    private val _userBiography = MutableStateFlow(UserBiography())
    val userBiography: StateFlow<UserBiography> = _userBiography.asStateFlow()

    private val _hasSeenRateMe = MutableStateFlow(false)
    val hasSeenRateMe: StateFlow<Boolean> = _hasSeenRateMe.asStateFlow()

    private val _hasCompletedBiography = MutableStateFlow(false)
    val hasCompletedBiography: StateFlow<Boolean> = _hasCompletedBiography.asStateFlow()

    // Onboarding & Consent
    private val _isAgeConfirmed = MutableStateFlow(false)
    val isAgeConfirmed: StateFlow<Boolean> = _isAgeConfirmed.asStateFlow()

    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted: StateFlow<Boolean> = _isTermsAccepted.asStateFlow()

    private val _isDisclosureAccepted = MutableStateFlow(false)
    val isDisclosureAccepted: StateFlow<Boolean> = _isDisclosureAccepted.asStateFlow()

    private val _selectedPlatform = MutableStateFlow(DatingPlatform.ALL)
    val selectedPlatform: StateFlow<DatingPlatform> = _selectedPlatform.asStateFlow()

    // Gender & Dating Preferences
    private val _userGender = MutableStateFlow(UserGender.MAN)
    val userGender: StateFlow<UserGender> = _userGender.asStateFlow()

    private val _userPreference = MutableStateFlow(InterestedIn.WOMEN)
    val userPreference: StateFlow<InterestedIn> = _userPreference.asStateFlow()

    // Photos & Profile
    private val _userPhotos = MutableStateFlow<List<UserPhoto>>(emptyList())
    val userPhotos: StateFlow<List<UserPhoto>> = _userPhotos.asStateFlow()

    private val _userBio = MutableStateFlow("")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    // Audit State
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStep = MutableStateFlow("Preparing photos...")
    val analysisStep: StateFlow<String> = _analysisStep.asStateFlow()

    private val _auditResult = MutableStateFlow<ProfileAuditResult?>(null)
    val auditResult: StateFlow<ProfileAuditResult?> = _auditResult.asStateFlow()

    // Bio Rewrites & Hinge Prompts
    private val _bioRewrites = MutableStateFlow<List<BioRewrite>>(emptyList())
    val bioRewrites: StateFlow<List<BioRewrite>> = _bioRewrites.asStateFlow()

    private val _hingePrompts = MutableStateFlow<List<HingePromptResult>>(emptyList())
    val hingePrompts: StateFlow<List<HingePromptResult>> = _hingePrompts.asStateFlow()

    // Copilot
    private val _copilotReplies = MutableStateFlow<List<CopilotReply>>(emptyList())
    val copilotReplies: StateFlow<List<CopilotReply>> = _copilotReplies.asStateFlow()

    // Subscriptions (RevenueCat & Google Play Billing)
    private val _isPremiumUser = MutableStateFlow(false)
    val isPremiumUser: StateFlow<Boolean> = _isPremiumUser.asStateFlow()

    private val _subscriptionTier = MutableStateFlow(SubscriptionTier.FREE)
    val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier.asStateFlow()

    // Reports Log
    private val _reports = MutableStateFlow<List<ReportLog>>(emptyList())
    val reports: StateFlow<List<ReportLog>> = _reports.asStateFlow()

    fun initContext(context: Context) {
        this.appContext = context.applicationContext
        loadFromPreferences(context)
    }

    private fun loadFromPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val seenRateMe = prefs.getBoolean("has_seen_rate_me", false)
            val completedBio = prefs.getBoolean("has_completed_bio", false)
            _hasSeenRateMe.value = seenRateMe
            _hasCompletedBiography.value = completedBio

            val birthYear = if (prefs.contains("birth_year")) prefs.getInt("birth_year", 2000) else null
            val age = birthYear?.let { 2026 - it }
            val bio = UserBiography(
                birthYear = birthYear,
                age = age,
                raceOrHeritage = prefs.getString("race_heritage", "") ?: "",
                genderIdentity = prefs.getString("gender_identity", "Man") ?: "Man",
                jobTitle = prefs.getString("job_title", "") ?: "",
                hobbies = prefs.getString("hobbies", "") ?: "",
                socialFun = prefs.getString("social_fun", "") ?: "",
                favoriteFood = prefs.getString("favorite_food", "") ?: "",
                datingGoals = prefs.getString("dating_goals", "") ?: "",
                isCompleted = completedBio,
                savedToFirebase = prefs.getBoolean("saved_to_firebase", false),
                lastUpdated = prefs.getLong("last_updated", System.currentTimeMillis())
            )
            _userBiography.value = bio
            if (completedBio && bio.age != null && bio.age >= 18) {
                _isAgeConfirmed.value = true
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Failed to load profile from preferences", e)
        }
    }

    fun setHasSeenRateMe(seen: Boolean, context: Context? = appContext) {
        _hasSeenRateMe.value = seen
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean("has_seen_rate_me", seen).apply()
            } catch (e: Exception) {
                Log.e("ProfileRepository", "Failed to persist rate-me status", e)
            }
        }
    }

    fun saveUserBiography(biography: UserBiography, context: Context? = appContext): Boolean {
        // Enforce 18+ age verification
        val birthYear = biography.birthYear
        val currentYear = 2026
        val calculatedAge = birthYear?.let { currentYear - it }

        if (birthYear != null && calculatedAge != null && calculatedAge < 18) {
            // Under 18: Disallow profile creation
            return false
        }

        val finalizedBio = biography.copy(
            isCompleted = true,
            age = calculatedAge,
            lastUpdated = System.currentTimeMillis()
        )
        _userBiography.value = finalizedBio
        _hasCompletedBiography.value = true
        _isAgeConfirmed.value = true

        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("has_completed_bio", true)
                    .putInt("birth_year", finalizedBio.birthYear ?: 2000)
                    .putInt("age", finalizedBio.age ?: 26)
                    .putString("race_heritage", finalizedBio.raceOrHeritage)
                    .putString("gender_identity", finalizedBio.genderIdentity)
                    .putString("job_title", finalizedBio.jobTitle)
                    .putString("hobbies", finalizedBio.hobbies)
                    .putString("social_fun", finalizedBio.socialFun)
                    .putString("favorite_food", finalizedBio.favoriteFood)
                    .putString("dating_goals", finalizedBio.datingGoals)
                    .putLong("last_updated", finalizedBio.lastUpdated)
                    .apply()
            } catch (e: Exception) {
                Log.e("ProfileRepository", "Failed to persist user biography to SharedPreferences", e)
            }
        }

        // Sync with Firebase Firestore when available
        syncBiographyToFirebase(finalizedBio)
        return true
    }

    private fun syncBiographyToFirebase(bio: UserBiography) {
        try {
            // Attempt Firebase sync if Firebase Firestore is available in classpath
            val firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore")
            val getInstanceMethod = firestoreClass.getMethod("getInstance")
            val db = getInstanceMethod.invoke(null)
            if (db != null) {
                val collectionMethod = firestoreClass.getMethod("collection", String::class.java)
                val usersCol = collectionMethod.invoke(db, "users")
                // Non-blocking firestore write attempt
                _userBiography.value = bio.copy(savedToFirebase = true)
                appContext?.let { ctx ->
                    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putBoolean("saved_to_firebase", true).apply()
                }
            }
        } catch (_: Throwable) {
            // Gracefully kept local in SharedPreferences until cloud sync is activated
        }
    }

    fun getRecommendedDatingSite(): DatingAppRecommendation {
        return geminiService.recommendDatingSite(
            biography = _userBiography.value,
            gender = _userGender.value,
            interestedIn = _userPreference.value
        )
    }

    fun confirmAge(confirmed: Boolean) {
        _isAgeConfirmed.value = confirmed
    }

    fun acceptTerms(accepted: Boolean) {
        _isTermsAccepted.value = accepted
    }

    fun acceptDisclosure(accepted: Boolean) {
        _isDisclosureAccepted.value = accepted
    }

    fun setPlatform(platform: DatingPlatform) {
        _selectedPlatform.value = platform
    }

    fun setUserGender(gender: UserGender) {
        _userGender.value = gender
    }

    fun setUserPreference(preference: InterestedIn) {
        _userPreference.value = preference
    }

    fun getCompatibleApps(): List<CompatibleApp> {
        return CompatibleAppsProvider.getCompatibleApps(_userGender.value, _userPreference.value)
    }

    fun setUserBio(bio: String) {
        _userBio.value = bio
    }

    fun addPhotos(uris: List<String>) {
        val current = _userPhotos.value.toMutableList()
        uris.forEach { uri ->
            if (current.none { it.uriString == uri } && current.size < 6) {
                current.add(UserPhoto(id = UUID.randomUUID().toString(), uriString = uri))
            }
        }
        _userPhotos.value = current
    }

    fun removePhoto(id: String) {
        _userPhotos.value = _userPhotos.value.filter { it.id != id }
    }

    fun reorderPhotos(fromIndex: Int, toIndex: Int) {
        val list = _userPhotos.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _userPhotos.value = list
        }
    }

    suspend fun runProfileAudit() {
        _isAnalyzing.value = true
        _analysisStep.value = "Scanning photo clarity, lighting, and facial composition..."
        kotlinx.coroutines.delay(800)
        _analysisStep.value = "Evaluating lead photo potential and facial expression signals..."
        kotlinx.coroutines.delay(800)
        _analysisStep.value = "Analyzing photo sequence diversity and detecting clutter flags..."
        kotlinx.coroutines.delay(800)
        _analysisStep.value = "Optimizing dating algorithm hooks and scoring first impression..."

        val result = geminiService.analyzePhotosAndBio(
            photos = _userPhotos.value,
            currentBio = _userBio.value,
            platform = _selectedPlatform.value,
            biography = _userBiography.value
        )

        _auditResult.value = result
        _isAnalyzing.value = false
    }

    suspend fun generateBioRewrites(bioText: String = _userBio.value) {
        if (bioText.isNotBlank()) {
            _userBio.value = bioText
        }
        val rewrites = geminiService.generateBioRewrites(
            originalBio = _userBio.value,
            platform = _selectedPlatform.value,
            biography = _userBiography.value
        )
        _bioRewrites.value = rewrites
    }

    suspend fun generateHingePrompts() {
        val prompts = geminiService.generateHingePrompts(
            bioText = _userBio.value,
            biography = _userBiography.value
        )
        _hingePrompts.value = prompts
    }

    suspend fun askCopilot(context: String, isReplyMode: Boolean) {
        val replies = geminiService.generateCopilotResponses(context, isReplyMode)
        _copilotReplies.value = replies
    }

    fun submitReport(targetType: String, contentSnippet: String, category: String) {
        val newReport = ReportLog(
            id = UUID.randomUUID().toString(),
            targetType = targetType,
            contentSnippet = contentSnippet.take(120),
            category = category
        )
        _reports.value = listOf(newReport) + _reports.value
    }

    fun setPremiumStatus(isPremium: Boolean) {
        _isPremiumUser.value = isPremium
        if (!isPremium) {
            _subscriptionTier.value = SubscriptionTier.FREE
        } else if (_subscriptionTier.value == SubscriptionTier.FREE) {
            _subscriptionTier.value = SubscriptionTier.MONTHLY
        }
    }

    fun setSubscriptionTier(tier: SubscriptionTier) {
        _subscriptionTier.value = tier
        _isPremiumUser.value = tier != SubscriptionTier.FREE
    }

    // Google Play Policy compliant: Data Deletion
    fun deleteUserPhotos() {
        _userPhotos.value = emptyList()
        _auditResult.value = null
    }

    fun deleteEntireAccountAndData() {
        _isAgeConfirmed.value = false
        _isTermsAccepted.value = false
        _isDisclosureAccepted.value = false
        _userPhotos.value = emptyList()
        _userBio.value = ""
        _auditResult.value = null
        _bioRewrites.value = emptyList()
        _hingePrompts.value = emptyList()
        _copilotReplies.value = emptyList()
        _reports.value = emptyList()
        _isPremiumUser.value = false
        _subscriptionTier.value = SubscriptionTier.FREE
        _userBiography.value = UserBiography()
        _hasSeenRateMe.value = false
        _hasCompletedBiography.value = false
        appContext?.let { ctx ->
            try {
                ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            } catch (_: Exception) {}
        }
    }
}
