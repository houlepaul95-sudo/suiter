package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenState {
    ONBOARDING,
    PHOTO_UPLOAD,
    ANALYZING,
    SCORE_REVEAL,
    BIO_REWRITER,
    COPILOT,
    PAYWALL,
    SHARE_CARD,
    SETTINGS,
    COMPATIBLE_APPS
}

class ProfileViewModel(
    val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _screenState = MutableStateFlow(ScreenState.ONBOARDING)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _screenBackStack = mutableListOf<ScreenState>()

    val isAgeConfirmed = repository.isAgeConfirmed
    val isTermsAccepted = repository.isTermsAccepted
    val isDisclosureAccepted = repository.isDisclosureAccepted
    val selectedPlatform = repository.selectedPlatform
    val userGender = repository.userGender
    val userPreference = repository.userPreference
    val userPhotos = repository.userPhotos
    val userBio = repository.userBio
    val isAnalyzing = repository.isAnalyzing
    val analysisStep = repository.analysisStep
    val auditResult = repository.auditResult
    val bioRewrites = repository.bioRewrites
    val hingePrompts = repository.hingePrompts
    val copilotReplies = repository.copilotReplies
    val isPremium = repository.isPremiumUser
    val subscriptionTier = repository.subscriptionTier
    val reports = repository.reports
    val userBiography = repository.userBiography
    val hasSeenRateMe = repository.hasSeenRateMe
    val hasCompletedBiography = repository.hasCompletedBiography
    val themeMode = repository.themeMode

    fun setThemeMode(mode: AppThemeMode, context: android.content.Context? = null) {
        repository.setThemeMode(mode, context)
    }

    // Dialog & Flow States
    private val _showRateMeDialog = MutableStateFlow(false)
    val showRateMeDialog: StateFlow<Boolean> = _showRateMeDialog.asStateFlow()

    private val _showBiographyDialog = MutableStateFlow(false)
    val showBiographyDialog: StateFlow<Boolean> = _showBiographyDialog.asStateFlow()

    private val _showDatingAppMatchDialog = MutableStateFlow(false)
    val showDatingAppMatchDialog: StateFlow<Boolean> = _showDatingAppMatchDialog.asStateFlow()

    private val _recommendedApp = MutableStateFlow<DatingAppRecommendation?>(null)
    val recommendedApp: StateFlow<DatingAppRecommendation?> = _recommendedApp.asStateFlow()

    private val _showProfileCreationModal = MutableStateFlow(false)
    val showProfileCreationModal: StateFlow<Boolean> = _showProfileCreationModal.asStateFlow()

    private val _copiedContentSnippet = MutableStateFlow("")
    val copiedContentSnippet: StateFlow<String> = _copiedContentSnippet.asStateFlow()

    private val _copiedContentTitle = MutableStateFlow("Bio Rewrite")
    val copiedContentTitle: StateFlow<String> = _copiedContentTitle.asStateFlow()

    private val _underAgeError = MutableStateFlow<String?>(null)
    val underAgeError: StateFlow<String?> = _underAgeError.asStateFlow()

    init {
        // Synchronize RevenueCat entitlement and subscription tier
        viewModelScope.launch {
            com.example.data.service.RevenueCatService.currentTier.collect { tier ->
                repository.setSubscriptionTier(tier)
            }
        }

        // If already consented, start at Photo Upload
        if (isAgeConfirmed.value && isTermsAccepted.value && isDisclosureAccepted.value) {
            _screenState.value = ScreenState.PHOTO_UPLOAD
        }
    }

    fun initContext(context: android.content.Context) {
        repository.initContext(context)
        // Check startup dialog flow: If user hasn't seen Rate Me dialog yet, show it on opening!
        if (!repository.hasSeenRateMe.value) {
            _showRateMeDialog.value = true
        } else if (!repository.hasCompletedBiography.value) {
            _showBiographyDialog.value = true
        }
    }

    fun navigateTo(screen: ScreenState) {
        if (_screenState.value != screen) {
            _screenBackStack.add(_screenState.value)
            _screenState.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (_screenBackStack.isNotEmpty()) {
            _screenState.value = _screenBackStack.removeAt(_screenBackStack.size - 1)
            true
        } else if (_screenState.value != ScreenState.PHOTO_UPLOAD && _screenState.value != ScreenState.ONBOARDING) {
            _screenState.value = ScreenState.PHOTO_UPLOAD
            true
        } else {
            false
        }
    }

    fun completeOnboarding(
        platform: DatingPlatform,
        gender: UserGender = UserGender.MAN,
        preference: InterestedIn = InterestedIn.WOMEN
    ) {
        repository.confirmAge(true)
        repository.acceptTerms(true)
        repository.acceptDisclosure(true)
        repository.setPlatform(platform)
        repository.setUserGender(gender)
        repository.setUserPreference(preference)
        _screenBackStack.clear()
        _screenState.value = ScreenState.PHOTO_UPLOAD
    }

    fun setUserGender(gender: UserGender) {
        repository.setUserGender(gender)
    }

    fun setUserPreference(preference: InterestedIn) {
        repository.setUserPreference(preference)
    }

    fun setPlatform(platform: DatingPlatform) {
        repository.setPlatform(platform)
    }

    fun addPhotos(uris: List<String>) {
        repository.addPhotos(uris)
    }

    fun removePhoto(id: String) {
        repository.removePhoto(id)
    }

    fun updatePhotoEdits(photoId: String, edits: PhotoEdits) {
        repository.updatePhotoEdits(photoId, edits)
    }

    fun applyEditsToAllPhotos(edits: PhotoEdits) {
        repository.applyEditsToAllPhotos(edits)
    }

    fun updateBio(bio: String) {
        repository.setUserBio(bio)
    }

    fun startProfileAnalysis() {
        navigateTo(ScreenState.ANALYZING)
        viewModelScope.launch {
            repository.runProfileAudit()
        }
    }

    fun submitPhotosToProfile() {
        viewModelScope.launch {
            repository.refreshAuditForEditedPhotos()
            _screenBackStack.clear()
            _screenState.value = ScreenState.SCORE_REVEAL
        }
    }

    fun generateBioRewrites() {
        viewModelScope.launch {
            repository.generateBioRewrites()
        }
    }

    fun generateHingePrompts() {
        viewModelScope.launch {
            repository.generateHingePrompts()
        }
    }

    fun askCopilot(context: String, isReplyMode: Boolean) {
        viewModelScope.launch {
            repository.askCopilot(context, isReplyMode)
        }
    }

    fun submitReport(targetType: String, contentSnippet: String, category: String) {
        repository.submitReport(targetType, contentSnippet, category)
    }

    fun setPremium(isPrem: Boolean) {
        val tier = if (isPrem) SubscriptionTier.MONTHLY else SubscriptionTier.FREE
        com.example.data.service.RevenueCatService.toggleProSimulated(isPrem, tier)
        repository.setSubscriptionTier(tier)
    }

    fun setSubscriptionTier(tier: SubscriptionTier) {
        com.example.data.service.RevenueCatService.toggleProSimulated(tier != SubscriptionTier.FREE, tier)
        repository.setSubscriptionTier(tier)
    }

    var pendingAiDirectAfterProfile: Boolean = false
        private set

    fun onSubscriptionPurchased(context: android.content.Context) {
        setPremium(true)
        val bio = userBiography.value
        val isProfileFilled = bio.isCompleted || (bio.jobTitle.isNotBlank() || bio.hobbies.isNotBlank() || bio.datingGoals.isNotBlank())

        if (isProfileFilled) {
            // Profile is already filled: Immediately have A.I. direct them with sleek tap button to gathered app!
            val recommendation = repository.getRecommendedDatingSite()
            _recommendedApp.value = recommendation
            _showDatingAppMatchDialog.value = true
            pendingAiDirectAfterProfile = false
        } else {
            // User paid but hasn't completed profile dossier yet: prompt them to fill profile immediately
            pendingAiDirectAfterProfile = true
            _showBiographyDialog.value = true
        }
    }

    fun openAiDirectAppDialog() {
        val recommendation = repository.getRecommendedDatingSite()
        _recommendedApp.value = recommendation
        _showDatingAppMatchDialog.value = true
    }

    fun deletePhotos() {
        repository.deleteUserPhotos()
        _screenBackStack.clear()
        _screenState.value = ScreenState.PHOTO_UPLOAD
    }

    fun dismissRateMe(context: android.content.Context) {
        repository.setHasSeenRateMe(true, context)
        _showRateMeDialog.value = false
        // Next in flow: Personal biography modal pops up!
        if (!repository.hasCompletedBiography.value) {
            _showBiographyDialog.value = true
        }
    }

    fun openBiographyDialog() {
        _showBiographyDialog.value = true
    }

    fun dismissBiographyDialog() {
        _showBiographyDialog.value = false
    }

    fun submitBiography(bio: UserBiography, context: android.content.Context): Boolean {
        _underAgeError.value = null
        val success = repository.saveUserBiography(bio, context)
        if (!success) {
            _underAgeError.value = "Suiter is strictly for adults 18+. Based on your birth year, you are not eligible to create a profile."
            return false
        }
        _showBiographyDialog.value = false
        // Calculate tailored dating app match from A.I. and show sleek directing dialog!
        val recommendation = repository.getRecommendedDatingSite()
        _recommendedApp.value = recommendation
        _showDatingAppMatchDialog.value = true
        pendingAiDirectAfterProfile = false
        return true
    }

    fun dismissDatingAppMatch() {
        _showDatingAppMatchDialog.value = false
    }

    fun applyRecommendedDatingApp(platform: DatingPlatform) {
        repository.setPlatform(platform)
        _showDatingAppMatchDialog.value = false
    }

    fun triggerProfileCreationModal(snippet: String, title: String = "Generated Output") {
        _copiedContentSnippet.value = snippet
        _copiedContentTitle.value = title
        _showProfileCreationModal.value = true
    }

    fun dismissProfileCreationModal() {
        _showProfileCreationModal.value = false
    }

    fun deleteEntireAccount() {
        repository.deleteEntireAccountAndData()
        _screenBackStack.clear()
        _screenState.value = ScreenState.ONBOARDING
        _showRateMeDialog.value = false
        _showBiographyDialog.value = false
        _showProfileCreationModal.value = false
    }
}
