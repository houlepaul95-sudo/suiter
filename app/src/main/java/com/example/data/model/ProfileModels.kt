package com.example.data.model

data class UserPhoto(
    val id: String,
    val uriString: String,
    val isLeadCandidate: Boolean = false,
    val detectedAspect: String = "Portrait"
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
    val badge: String? = null
) {
    FREE("free", "Free Tier", "Essential score, 1 quick fix & dating app matching"),
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
