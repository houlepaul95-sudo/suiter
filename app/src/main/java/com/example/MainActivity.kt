package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ProfileViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.*
import com.example.ui.theme.ProfilePulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.service.RevenueCatService.initialize(this)
        setContent {
            ProfilePulseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProfilePulseApp()
                }
            }
        }
    }
}

@Composable
fun ProfilePulseApp(
    viewModel: ProfileViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isAgeConfirmed by viewModel.isAgeConfirmed.collectAsStateWithLifecycle()
    val isTermsAccepted by viewModel.isTermsAccepted.collectAsStateWithLifecycle()
    val isDisclosureAccepted by viewModel.isDisclosureAccepted.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val userGender by viewModel.userGender.collectAsStateWithLifecycle()
    val userPreference by viewModel.userPreference.collectAsStateWithLifecycle()
    val userPhotos by viewModel.userPhotos.collectAsStateWithLifecycle()
    val userBio by viewModel.userBio.collectAsStateWithLifecycle()
    val analysisStep by viewModel.analysisStep.collectAsStateWithLifecycle()
    val auditResult by viewModel.auditResult.collectAsStateWithLifecycle()
    val bioRewrites by viewModel.bioRewrites.collectAsStateWithLifecycle()
    val hingePrompts by viewModel.hingePrompts.collectAsStateWithLifecycle()
    val copilotReplies by viewModel.copilotReplies.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    // BackHandler for sub-screens
    if (screenState != ScreenState.ONBOARDING && screenState != ScreenState.PHOTO_UPLOAD) {
        BackHandler {
            viewModel.navigateBack()
        }
    }

    when (screenState) {
        ScreenState.ONBOARDING -> {
            OnboardingScreen(
                onCompleteOnboarding = { platform, gender, preference ->
                    viewModel.completeOnboarding(platform, gender, preference)
                },
                onOpenCompatibleApps = {
                    viewModel.navigateTo(ScreenState.COMPATIBLE_APPS)
                },
                onViewPrivacyPolicy = {
                    viewModel.navigateTo(ScreenState.SETTINGS)
                },
                onViewTerms = {
                    viewModel.navigateTo(ScreenState.SETTINGS)
                }
            )
        }

        ScreenState.PHOTO_UPLOAD -> {
            PhotoUploadScreen(
                platform = selectedPlatform,
                photos = userPhotos,
                bio = userBio,
                onPhotosSelected = { uris ->
                    viewModel.addPhotos(uris)
                },
                onPhotoRemoved = { id ->
                    viewModel.removePhoto(id)
                },
                onBioChanged = { bio ->
                    viewModel.updateBio(bio)
                },
                onStartAnalysis = {
                    viewModel.startProfileAnalysis()
                },
                onOpenSettings = {
                    viewModel.navigateTo(ScreenState.SETTINGS)
                },
                onOpenCompatibleApps = {
                    viewModel.navigateTo(ScreenState.COMPATIBLE_APPS)
                }
            )
        }

        ScreenState.ANALYZING -> {
            AnalyzingScreen(
                currentStepText = analysisStep
            )
        }

        ScreenState.SCORE_REVEAL -> {
            auditResult?.let { result ->
                ScoreRevealScreen(
                    audit = result,
                    photos = userPhotos,
                    isPremium = isPremium,
                    onOpenPaywall = {
                        viewModel.navigateTo(ScreenState.PAYWALL)
                    },
                    onOpenBioRewriter = {
                        viewModel.navigateTo(ScreenState.BIO_REWRITER)
                    },
                    onOpenCopilot = {
                        viewModel.navigateTo(ScreenState.COPILOT)
                    },
                    onOpenShareCard = {
                        viewModel.navigateTo(ScreenState.SHARE_CARD)
                    },
                    onOpenCompatibleApps = {
                        viewModel.navigateTo(ScreenState.COMPATIBLE_APPS)
                    },
                    onReportSubmitted = { targetType, snippet, category ->
                        viewModel.submitReport(targetType, snippet, category)
                    },
                    onRetestProfile = {
                        viewModel.navigateTo(ScreenState.PHOTO_UPLOAD)
                    }
                )
            } ?: run {
                // Fallback to upload if no audit yet
                viewModel.navigateTo(ScreenState.PHOTO_UPLOAD)
            }
        }

        ScreenState.BIO_REWRITER -> {
            BioRewriterScreen(
                platform = selectedPlatform,
                currentBio = userBio,
                rewrites = bioRewrites,
                hingePrompts = hingePrompts,
                isPremium = isPremium,
                onOpenPaywall = { viewModel.navigateTo(ScreenState.PAYWALL) },
                onGenerateRewrites = { viewModel.generateBioRewrites() },
                onGeneratePrompts = { viewModel.generateHingePrompts() },
                onBack = { viewModel.navigateBack() },
                onReportSubmitted = { targetType, snippet, category ->
                    viewModel.submitReport(targetType, snippet, category)
                }
            )
        }

        ScreenState.COPILOT -> {
            CopilotScreen(
                replies = copilotReplies,
                isPremium = isPremium,
                onOpenPaywall = { viewModel.navigateTo(ScreenState.PAYWALL) },
                onAskCopilot = { context, isReply ->
                    viewModel.askCopilot(context, isReply)
                },
                onBack = { viewModel.navigateBack() },
                onReportSubmitted = { targetType, snippet, category ->
                    viewModel.submitReport(targetType, snippet, category)
                }
            )
        }

        ScreenState.PAYWALL -> {
            PaywallScreen(
                isCurrentPremium = isPremium,
                onPurchaseCompleted = {
                    viewModel.setPremium(true)
                },
                onRestorePurchases = {
                    viewModel.setPremium(true)
                },
                onClose = {
                    viewModel.navigateBack()
                },
                onViewTerms = {
                    viewModel.navigateTo(ScreenState.SETTINGS)
                },
                onViewPrivacy = {
                    viewModel.navigateTo(ScreenState.SETTINGS)
                }
            )
        }

        ScreenState.SHARE_CARD -> {
            ShareCardScreen(
                audit = auditResult,
                photos = userPhotos,
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenState.SETTINGS -> {
            SettingsComplianceScreen(
                reports = reports,
                isPremium = isPremium,
                onTogglePremium = { viewModel.setPremium(it) },
                onDeletePhotos = { viewModel.deletePhotos() },
                onDeleteAccountAndData = { viewModel.deleteEntireAccount() },
                onOpenCompatibleApps = { viewModel.navigateTo(ScreenState.COMPATIBLE_APPS) },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenState.COMPATIBLE_APPS -> {
            CompatibleAppsScreen(
                currentGender = userGender,
                currentPreference = userPreference,
                currentPlatform = selectedPlatform,
                onGenderSelected = { viewModel.setUserGender(it) },
                onPreferenceSelected = { viewModel.setUserPreference(it) },
                onPlatformSelected = { viewModel.setPlatform(it) },
                onNavigateToUpload = { viewModel.navigateTo(ScreenState.PHOTO_UPLOAD) },
                onNavigateToBioRewriter = { viewModel.navigateTo(ScreenState.BIO_REWRITER) },
                onBack = { viewModel.navigateBack() }
            )
        }
    }
}
