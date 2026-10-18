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

    init {
        // Synchronize RevenueCat entitlement and subscription tier
        viewModelScope.launch {
            com.example.data.service.RevenueCatService.currentTier.collect { tier ->
                repository.setSubscriptionTier(tier)
            }
        }

        // If already consented (e.g. state preserved), start at Photo Upload
        if (isAgeConfirmed.value && isTermsAccepted.value && isDisclosureAccepted.value) {
            _screenState.value = ScreenState.PHOTO_UPLOAD
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

    fun updateBio(bio: String) {
        repository.setUserBio(bio)
    }

    fun startProfileAnalysis() {
        navigateTo(ScreenState.ANALYZING)
        viewModelScope.launch {
            repository.runProfileAudit()
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

    fun deletePhotos() {
        repository.deleteUserPhotos()
        _screenBackStack.clear()
        _screenState.value = ScreenState.PHOTO_UPLOAD
    }

    fun deleteEntireAccount() {
        repository.deleteEntireAccountAndData()
        _screenBackStack.clear()
        _screenState.value = ScreenState.ONBOARDING
    }
}
