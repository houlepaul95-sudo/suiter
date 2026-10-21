package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ProfileViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.*
import com.example.ui.theme.SuiterTheme

import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.CoachDatingAppMatchDialog
import com.example.ui.components.PersonalBiographyModal
import com.example.ui.components.ProfileCreationModal
import com.example.ui.components.RateMeDialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.service.RevenueCatService.initialize(this)
        setContent {
            val viewModel: ProfileViewModel = viewModel()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                viewModel.initContext(context)
            }
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                com.example.data.model.AppThemeMode.LIGHT -> false
                com.example.data.model.AppThemeMode.DARK -> true
                com.example.data.model.AppThemeMode.SYSTEM -> systemInDark
            }
            com.example.ui.theme.SuiterTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SuiterApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SuiterApp(
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initContext(context)
    }

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isAgeConfirmed by viewModel.isAgeConfirmed.collectAsStateWithLifecycle()
    val isTermsAccepted by viewModel.isTermsAccepted.collectAsStateWithLifecycle()
    val isDisclosureAccepted by viewModel.isDisclosureAccepted.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatform.collectAsStateWithLifecycle()
    val userGender by viewModel.userGender.collectAsStateWithLifecycle()
    val userPreference by viewModel.userPreference.collectAsStateWithLifecycle()
    val userPhotos by viewModel.userPhotos.collectAsStateWithLifecycle()
    val userBio by viewModel.userBio.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisStep by viewModel.analysisStep.collectAsStateWithLifecycle()
    val auditResult by viewModel.auditResult.collectAsStateWithLifecycle()
    val bioRewrites by viewModel.bioRewrites.collectAsStateWithLifecycle()
    val hingePrompts by viewModel.hingePrompts.collectAsStateWithLifecycle()
    val copilotReplies by viewModel.copilotReplies.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val userBiography by viewModel.userBiography.collectAsStateWithLifecycle()
    val showRateMeDialog by viewModel.showRateMeDialog.collectAsStateWithLifecycle()
    val showBiographyDialog by viewModel.showBiographyDialog.collectAsStateWithLifecycle()
    val showDatingAppMatchDialog by viewModel.showDatingAppMatchDialog.collectAsStateWithLifecycle()
    val recommendedApp by viewModel.recommendedApp.collectAsStateWithLifecycle()
    val showProfileCreationModal by viewModel.showProfileCreationModal.collectAsStateWithLifecycle()
    val copiedContentSnippet by viewModel.copiedContentSnippet.collectAsStateWithLifecycle()
    val copiedContentTitle by viewModel.copiedContentTitle.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

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
                biography = userBiography,
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
                },
                onOpenBiography = {
                    viewModel.openBiographyDialog()
                }
            )
        }

        ScreenState.ANALYZING -> {
            AnalyzingScreen(
                currentStepText = analysisStep,
                photos = userPhotos,
                isAnalyzing = isAnalyzing,
                onUpdatePhotoEdits = { photoId, edits ->
                    viewModel.updatePhotoEdits(photoId, edits)
                },
                onApplyEditsToAll = { edits ->
                    viewModel.applyEditsToAllPhotos(edits)
                },
                onSubmitToProfile = {
                    viewModel.submitPhotosToProfile()
                },
                onBackToUpload = {
                    viewModel.navigateTo(ScreenState.PHOTO_UPLOAD)
                }
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
                biography = userBiography,
                isPremium = isPremium,
                onOpenPaywall = { viewModel.navigateTo(ScreenState.PAYWALL) },
                onGenerateRewrites = { viewModel.generateBioRewrites() },
                onGeneratePrompts = { viewModel.generateHingePrompts() },
                onCopyOutput = { text, title ->
                    viewModel.triggerProfileCreationModal(text, title)
                },
                onEditBiography = {
                    viewModel.openBiographyDialog()
                },
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
                onCopyOutput = { text, title ->
                    viewModel.triggerProfileCreationModal(text, title)
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
                    viewModel.onSubscriptionPurchased(context)
                },
                onRestorePurchases = {
                    viewModel.onSubscriptionPurchased(context)
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
                themeMode = themeMode,
                onThemeModeChanged = { viewModel.setThemeMode(it, context) },
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

    // Modal Dialogs Overlays
    if (showRateMeDialog) {
        RateMeDialog(
            onRateClicked = { viewModel.dismissRateMe(context) },
            onDismiss = { viewModel.dismissRateMe(context) }
        )
    }

    if (showBiographyDialog) {
        PersonalBiographyModal(
            initialBio = userBiography,
            onSaveBiography = { newBio ->
                viewModel.submitBiography(newBio, context)
            },
            onDismiss = { viewModel.dismissBiographyDialog() }
        )
    }

    if (showDatingAppMatchDialog && recommendedApp != null) {
        CoachDatingAppMatchDialog(
            recommendation = recommendedApp!!,
            biography = userBiography,
            onApplyPlatform = { platform ->
                viewModel.applyRecommendedDatingApp(platform)
            },
            onDismiss = { viewModel.dismissDatingAppMatch() }
        )
    }

    if (showProfileCreationModal) {
        ProfileCreationModal(
            copiedText = copiedContentSnippet,
            title = copiedContentTitle,
            platform = selectedPlatform,
            biography = userBiography,
            leadPhoto = userPhotos.firstOrNull(),
            onEditBiography = { viewModel.openBiographyDialog() },
            onDismiss = { viewModel.dismissProfileCreationModal() }
        )
    }
}
