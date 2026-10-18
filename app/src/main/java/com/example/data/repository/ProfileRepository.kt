package com.example.data.repository

import com.example.data.model.*
import com.example.data.service.GeminiOptimizerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ProfileRepository(
    private val geminiService: GeminiOptimizerService = GeminiOptimizerService()
) {
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
            platform = _selectedPlatform.value
        )

        _auditResult.value = result
        _isAnalyzing.value = false
    }

    suspend fun generateBioRewrites(bioText: String = _userBio.value) {
        if (bioText.isNotBlank()) {
            _userBio.value = bioText
        }
        val rewrites = geminiService.generateBioRewrites(_userBio.value, _selectedPlatform.value)
        _bioRewrites.value = rewrites
    }

    suspend fun generateHingePrompts() {
        val prompts = geminiService.generateHingePrompts(_userBio.value)
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
    }
}
