package com.example.data.model

import kotlin.math.abs

data class PhotoEdits(
    val skinSoftening: Float = 0f,     // 0f..1f (Soften skin tone & smooth blemishes)
    val jawWidth: Float = 0f,          // -0.5f..1f (Slim or widen jawline structure via mesh warp)
    val skinWarmth: Float = 0f,        // 0f..1f (Golden hour skin warmth)
    val eyeClarity: Float = 0f,        // 0f..1f (Eye brightness & contrast pop)
    val jawContour: Float = 0f,        // 0f..1f (Cheekbone & jawline shadow sculpting)
    val brightness: Float = 0f,        // -0.5f..0.5f (Canvas brightness filter adjustment)
    val contrast: Float = 0f,          // -0.5f..0.5f (Canvas contrast filter adjustment)
    val perspectiveWarp: Float = 0f    // -0.5f..0.5f (Vertical chin/jaw perspective tilt warp)
) {
    val isEdited: Boolean
        get() = skinSoftening > 0.01f || abs(jawWidth) > 0.01f ||
                skinWarmth > 0.01f || eyeClarity > 0.01f || jawContour > 0.01f ||
                abs(brightness) > 0.01f || abs(contrast) > 0.01f || abs(perspectiveWarp) > 0.01f

    fun summaryBadges(): List<String> {
        val badges = mutableListOf<String>()
        if (skinSoftening > 0.01f) badges.add("Skin Softened ${(skinSoftening * 100).toInt()}%")
        if (jawWidth > 0.01f) badges.add("Jaw Widened +${(jawWidth * 100).toInt()}%")
        else if (jawWidth < -0.01f) badges.add("Jaw Slimmed ${(jawWidth * 100).toInt()}%")
        if (abs(perspectiveWarp) > 0.01f) {
            val p = (perspectiveWarp * 100).toInt()
            badges.add("Jaw Perspective ${if (p > 0) "+$p%" else "$p%"}")
        }
        if (abs(brightness) > 0.01f) {
            val b = (brightness * 100).toInt()
            badges.add("Brightness ${if (b > 0) "+$b%" else "$b%"}")
        }
        if (abs(contrast) > 0.01f) {
            val c = (contrast * 100).toInt()
            badges.add("Contrast ${if (c > 0) "+$c%" else "$c%"}")
        }
        if (skinWarmth > 0.01f) badges.add("Warm Glow +${(skinWarmth * 100).toInt()}%")
        if (eyeClarity > 0.01f) badges.add("Eye Clarity +${(eyeClarity * 100).toInt()}%")
        if (jawContour > 0.01f) badges.add("Contour +${(jawContour * 100).toInt()}%")
        return badges
    }
}

data class UserPhoto(
    val id: String,
    val uriString: String,
    val isLeadCandidate: Boolean = false,
    val detectedAspect: String = "Portrait",
    val edits: PhotoEdits = PhotoEdits()
)

data class PhotoAudit(
    val photoIndex: Int,
    val label: String,
    val isLeadPick: Boolean,
    val ratingScore: Int, // e.g. 58, 72, 84
    val honestCritique: String = "",
    val strengths: List<String>,
    val flags: List<String>,
    val actionTip: String
)

// Persona and Dating Vibe constants
const val VIBE_FLIRTATIOUS = "Flirtatious"
const val VIBE_FUNNY = "Funny"
const val VIBE_FLIRTATIOUS_AND_FUNNY = "Flirtatious, and funny"

enum class DatingVibe(val label: String) {
    FLIRTATIOUS_AND_FUNNY("Flirtatious, and funny"),
    FLIRTATIOUS("Flirtatious"),
    FUNNY("Funny"),
    WITTY_AND_PLAYFUL("Witty & Playful"),
    CONFIDENT_AND_DIRECT("Confident & Direct"),
    CHEEK_AND_CHARMING("Cheeky & Charming")
}

enum class AppThemeMode(val title: String) {
    LIGHT("Warm Light (Inviting)"),
    DARK("Velvet Dark (Inviting)"),
    SYSTEM("System Default")
}

data class UserBiography(
    val birthYear: Int? = null,
    val age: Int? = null,
    val raceOrHeritage: String = "",
    val genderIdentity: String = "Man",
    val jobTitle: String = "",
    val hobbies: String = "",
    val socialFun: String = "",
    val favoriteFood: String = "",
    val datingGoals: String = "",
    val datingVibe: String = VIBE_FLIRTATIOUS_AND_FUNNY,
    val tone: String = VIBE_FLIRTATIOUS_AND_FUNNY,
    val isCompleted: Boolean = false,
    val savedToFirebase: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class DatingAppRecommendation(
    val appName: String,
    val matchScore: String = "96% Match",
    val badge: String = "Coach Top Pick",
    val coachHeadline: String,
    val whyItMatchesProfile: List<String>,
    val recommendedBioAngle: String,
    val targetPlatform: DatingPlatform,
    val playStorePackage: String
)

data class ProfileAuditResult(
    val overallScore: Int,
    val photoQualityScore: Int,
    val diversityScore: Int,
    val bioHookScore: Int,
    val firstImpressionScore: Int,
    val leadPhotoIndex: Int,
    val recommendedOrder: List<Int>,
    val summaryTitle: String,
    val topStrength: String,
    val topFix: String,
    val freeInsight: String,
    val premiumLockedCount: Int = 4,
    val detailedPhotoAudits: List<PhotoAudit>,
    val generatedAt: Long = System.currentTimeMillis()
)

data class BioRewrite(
    val id: String,
    val tone: String, // "Funny", "Direct", "Charming"
    val targetApp: String,
    val bioText: String,
    val whyItWorks: String
)

data class HingePromptResult(
    val promptQuestion: String,
    val promptAnswer: String,
    val category: String
)

data class CopilotReply(
    val id: String,
    val style: String,
    val text: String,
    val reason: String
)

data class ReportLog(
    val id: String,
    val targetType: String,
    val contentSnippet: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UserGender(val label: String) {
    MAN("Man"),
    WOMAN("Woman"),
    NON_BINARY("Non-Binary")
}

enum class InterestedIn(val label: String) {
    WOMEN("Women"),
    MEN("Men"),
    EVERYONE("Everyone")
}

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val description: String,
    val badge: String? = null,
    val isOneTime: Boolean = false,
    val durationDays: Int? = null
) {
    FREE("free", "Basic Plan", "Essential score, 1 quick fix & dating app matching"),
    PROFILE_BOOST("suitor_profile_boost_30d", "Suitor Profile Boost", "30 days of full AI photo feedback, bio rewrites & action plan", "ONE-TIME • 30 DAYS", isOneTime = true, durationDays = 30),
    WEEKLY("weekly", "Weekly Boost", "Full photo deep-dive & bio rewriter for 7 days", "FLEXIBLE"),
    MONTHLY("monthly", "Monthly Pro", "All AI features, Copilot & rewrites with 7-day free trial", "MOST POPULAR"),
    ANNUAL("annual", "Annual VIP", "Complete dating profile advantage + priority AI model", "BEST VALUE (SAVE 80%)")
}

data class CompatibleApp(
    val id: String,
    val name: String,
    val badge: String,
    val matchScore: String = "95% Match",
    val audience: String = "All Singles",
    val description: String,
    val coachStrategy: String,
    val keyFeatures: List<String> = emptyList(),
    val playStorePackage: String,
    val ratingScore: String = "4.5★",
    val associatedPlatform: DatingPlatform? = null
)

enum class DatingPlatform(val displayName: String, val tagline: String) {
    HINGE("Hinge", "Designed to be deleted"),
    TINDER("Tinder", "Fast-paced & visual impact"),
    BUMBLE("Bumble", "Women make the first move"),
    GRINDR("Grindr", "Leading social network for gay, bi, trans & queer men"),
    SCRUFF("Scruff", "Global community for gay, bi, trans & queer men"),
    HER("HER", "Award-winning LGBTQ+ dating & community for women"),
    FEELD("Feeld", "Open-minded & progressive connections"),
    ALL("All Dating Apps", "Universal balanced appeal")
}
