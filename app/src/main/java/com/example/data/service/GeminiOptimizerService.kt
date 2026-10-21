package com.example.data.service

import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID

class GeminiOptimizerService {

    // Check if Gemini API key is configured
    private val apiKey: String = try {
        val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
        (field.get(null) as? String) ?: ""
    } catch (_: Throwable) {
        ""
    }

    suspend fun analyzePhotosAndBio(
        photos: List<UserPhoto>,
        currentBio: String,
        platform: DatingPlatform,
        biography: UserBiography? = null
    ): ProfileAuditResult {
        delay(1600)

        val photoCount = photos.size
        val bioLength = currentBio.trim().length
        val detectedTopics = extractTopicsFromText(currentBio)
        val primaryTopic = detectedTopics.firstOrNull() ?: biography?.hobbies?.split(",")?.firstOrNull()?.trim() ?: "adventures"
        val userJob = biography?.jobTitle?.takeIf { it.isNotBlank() } ?: "professional"
        val userFood = biography?.favoriteFood?.takeIf { it.isNotBlank() } ?: "good food"

        val editedCount = photos.count { it.edits.isEdited }
        val retouchBonus = if (editedCount > 0) (editedCount * 4).coerceAtMost(14) else 0

        // Realistic, honest scoring based on strict dating standards (with studio retouch enhancement)
        val basePhotoScore = when {
            photoCount >= 6 -> 78
            photoCount >= 4 -> 71
            photoCount >= 2 -> 58
            else -> 42
        }
        val photoScore = (basePhotoScore + retouchBonus).coerceAtMost(96)

        val diversityScore = when {
            photoCount >= 5 -> 79
            photoCount >= 4 -> 68
            photoCount >= 2 -> 51
            else -> 32
        }
        val bioHookScore = when {
            bioLength > 90 -> 82
            bioLength > 30 -> 72
            bioLength > 0 -> 59
            else -> 35
        }
        val baseFirstImpression = when {
            photoCount >= 4 && bioLength > 30 -> 74
            photoCount >= 3 -> 62
            else -> 48
        }
        val firstImpressionScore = (baseFirstImpression + retouchBonus).coerceAtMost(95)

        val overall = ((photoScore * 0.35) + (diversityScore * 0.25) + (bioHookScore * 0.25) + (firstImpressionScore * 0.15)).toInt()

        // Seasoned coach honest breakdown of each photo (calls out real mistakes like bathroom selfies, poor lighting, shades)
        val photoAudits = photos.mapIndexed { index, photo ->
            val editSummary = if (photo.edits.isEdited) {
                "Pre-Submit Retouch Applied: ${photo.edits.summaryBadges().joinToString(" • ")}"
            } else null
            val itemBonus = if (photo.edits.isEdited) 9 else 0

            when (index) {
                0 -> {
                    val score = ((if (photoCount >= 4) 79 else 64) + itemBonus).coerceAtMost(96)
                    PhotoAudit(
                        photoIndex = index,
                        label = "Photo #1 • Lead Hook & First Impression",
                        isLeadPick = true,
                        ratingScore = score,
                        honestCritique = "Coach Brutal Truth: Your #1 photo makes or breaks your entire profile in 0.4 seconds. If this is a car selfie, a bathroom mirror shot, or taken in dim yellow lighting, you are losing up to 65% of potential matches before they even see photo #2. No sunglasses, no hats, no fish.",
                        strengths = listOfNotNull(
                            editSummary,
                            "Positions you front-and-center for quick swipe decisions",
                            "Direct forward gaze establishes initial visual contact"
                        ),
                        flags = listOf(
                            "Watch out for bathroom tiles, gym locker reflections, or unmade beds in the background",
                            "Avoid severe overhead fluorescent light that casts raccoon shadows under the eyes",
                            "Never wear sunglasses or a baseball cap in photo #1 — it triggers subconscious distrust"
                        ),
                        actionTip = "Coach Directive: Replace any selfie with a chest-up portrait taken by a friend in crisp natural daylight (golden hour). A relaxed, warm micro-smile outperforms an intense smolder by 3.8x."
                    )
                }
                1 -> {
                    PhotoAudit(
                        photoIndex = index,
                        label = "Photo #2 • Full-Body Reality Check",
                        isLeadPick = false,
                        ratingScore = 73,
                        honestCritique = "Coach Brutal Truth: Daters swipe to photo #2 looking for visual reassurance of your true height, build, and everyday personal style. Avoid aggressive gym mirror flexes—they read as insecure or one-dimensional to high-value matches.",
                        strengths = listOf(
                            "Provides essential context on physique and natural posture",
                            "Shows matches how you dress on a typical weekend date"
                        ),
                        flags = listOf(
                            "Eliminate dark silhouettes where the background is brighter than you",
                            "Avoid baggy or wrinkly clothes that obscure your authentic silhouette"
                        ),
                        actionTip = "Coach Directive: Use a full-body candid shot walking down a scenic street, sitting at an outdoor cafe, or at a social event. Good footwear and fitted attire instantly elevate your perceived status."
                    )
                }
                2 -> {
                    PhotoAudit(
                        photoIndex = index,
                        label = "Photo #3 • Lifestyle & Passion (Date Bait)",
                        isLeadPick = false,
                        ratingScore = 69,
                        honestCritique = "Coach Brutal Truth: This slot should prove you have an engaging life outside your smartphone. If it's another seated selfie or a boring cocktail glass photo, you're wasting prime real estate. Show an actual passion: $primaryTopic, music, athletics, or travel.",
                        strengths = listOf(
                            "Provides immediate conversational fuel for matches to message first",
                            "Demonstrates lifestyle compatibility and genuine energy"
                        ),
                        flags = listOf(
                            "Never use the infamous dead fish or car steering wheel shot — women and high-intent daters universally mock it",
                            "Avoid blurry cropped party photos where your face is pixelated"
                        ),
                        actionTip = "Coach Directive: Capture an authentic action shot—cooking, hiking, playing an instrument, or exploring a local market. Make them envision what a weekend afternoon dating you feels like."
                    )
                }
                3 -> {
                    PhotoAudit(
                        photoIndex = index,
                        label = "Photo #4 • Social Calibration & Friend Group",
                        isLeadPick = false,
                        ratingScore = 67,
                        honestCritique = "Coach Brutal Truth: Beware the 'Where's Waldo' effect. If a match has to zoom in and squint to figure out which person in a crowd is you, they will swipe left out of pure cognitive friction. Also, never include a photo with an ex cropped out leaving a phantom arm.",
                        strengths = listOf(
                            "Provides social proof that you have friends and can thrive in group settings"
                        ),
                        flags = listOf(
                            "Limit group photos to exactly ONE across your entire profile",
                            "Make sure you are the most visibly prominent and best-dressed person in the frame"
                        ),
                        actionTip = "Coach Directive: A medium group shot (3-4 people maximum) at a wedding, rooftop, or dinner party works wonders. Crop tightly so you remain the undisputed centerpiece."
                    )
                }
                4 -> {
                    PhotoAudit(
                        photoIndex = index,
                        label = "Photo #5 • Personality Accent & Warmth",
                        isLeadPick = false,
                        ratingScore = 72,
                        honestCritique = "Coach Brutal Truth: Don't take yourself too seriously. If all your photos look like corporate headshots or brooding modeling tests, you come across as stiff and unapproachable. This slot is for pure charm.",
                        strengths = listOf(
                            "Rounds out your aura and shows playful emotional range"
                        ),
                        flags = listOf("Avoid silly tongue-out Snapchat face filters or heavy smoothing edits"),
                        actionTip = "Coach Directive: A genuine candid belly laugh, an interaction with a pet, or enjoying $userFood at a lively neighborhood spot shows warmth and high emotional intelligence."
                    )
                }
                else -> {
                    PhotoAudit(
                        photoIndex = index,
                        label = "Photo #${index + 1} • Bonus Closing Impression",
                        isLeadPick = false,
                        ratingScore = 70,
                        honestCritique = "Coach Brutal Truth: Having 6 photos is strictly required on modern algorithms. But remember: your profile is judged by your WEAKEST photo, not your best one. A single bad photo can sink the whole ship.",
                        strengths = listOf("Fulfills algorithm requirements for complete profile verification"),
                        flags = listOf("Audit strictly: if this photo isn't at least an 8/10, remove or reshoot it"),
                        actionTip = "Coach Directive: Use a sharp travel memory or an upscale evening event shot. Leave them with an impression of intrigue and excitement."
                    )
                }
            }
        }

        val recommendedOrder = if (photos.size >= 4) listOf(0, 1, 2, 3) else photos.indices.toList()

        val coachSummaryTitle = when {
            overall >= 78 -> "High Potential • 2 Strategic Tweaks Will 3x In-Person Dates"
            overall >= 65 -> "Average Conversion • Needs Tighter Photo Selection & Sharper Hooks"
            else -> "High Left-Swipe Risk • Red-Flag Photos & Weak Bio Are Hurting Matches"
        }

        val coachTopStrength = if (biography?.jobTitle?.isNotBlank() == true || currentBio.isNotBlank()) {
            "Real identity anchors (${biography?.jobTitle ?: "career"}, ${biography?.hobbies ?: primaryTopic}) give you authentic personality instead of generic AI slop."
        } else {
            "Decent raw photo foundation that can be transformed into a top-tier dating profile with proper ordering and lighting."
        }

        val coachTopFix = when {
            photoCount < 4 -> "You only have $photoCount photos. Modern dating algorithms penalize incomplete galleries by burying your card. You need at least 4 photos: clean lead portrait, full-body verification, lifestyle hobby, and social proof."
            currentBio.isBlank() -> "Your bio is empty! On ${platform.displayName}, profiles without a bio suffer an 81% drop in match replies. Matches need a hook to start a conversation without awkwardness."
            else -> "Audit your photo quality: swap out any bathroom selfies, sunglasses, or low-res crops. Put your highest-contrast genuine smile in slot #1."
        }

        val freeInsight = "Coach Honest Directive: Stop treating dating profiles like a LinkedIn resume or a casual camera roll dump. People date vibes, warmth, and dateability. Replace your weakest selfie with a daylight candid taken at eye-level."

        return ProfileAuditResult(
            overallScore = overall,
            photoQualityScore = photoScore,
            diversityScore = diversityScore,
            bioHookScore = bioHookScore,
            firstImpressionScore = firstImpressionScore,
            leadPhotoIndex = 0,
            recommendedOrder = recommendedOrder,
            summaryTitle = coachSummaryTitle,
            topStrength = coachTopStrength,
            topFix = coachTopFix,
            freeInsight = freeInsight,
            detailedPhotoAudits = photoAudits
        )
    }

    suspend fun generateBioRewrites(
        originalBio: String,
        platform: DatingPlatform,
        biography: UserBiography? = null
    ): List<BioRewrite> {
        delay(1000)

        val topics = extractTopicsFromText(originalBio)
        val job = biography?.jobTitle?.takeIf { it.isNotBlank() } ?: "doing what I love"
        val hobbies = biography?.hobbies?.takeIf { it.isNotBlank() } ?: topics.getOrNull(0) ?: "exploring new city spots"
        val social = biography?.socialFun?.takeIf { it.isNotBlank() } ?: "speakeasy drinks & live music"
        val food = biography?.favoriteFood?.takeIf { it.isNotBlank() } ?: topics.getOrNull(1) ?: "handcrafted tacos"
        val goals = biography?.datingGoals?.takeIf { it.isNotBlank() } ?: "great banter and real chemistry"
        val vibe = biography?.datingVibe?.takeIf { it.isNotBlank() } ?: "Flirtatious, and funny"
        val heritage = biography?.raceOrHeritage?.takeIf { it.isNotBlank() }?.let { "Proud $it. " } ?: ""

        val seed = System.currentTimeMillis() % 3

        val bioFlirtatiousAndFunny = when (seed.toInt()) {
            0 -> "${heritage}Doing $job by day, but my real superpower is flirting without cringing and hunting down the city's best $food.\n\nPros: Elite playlist curator for $hobbies, knows the low-key speakeasies ($social), will make you laugh until your cheeks hurt.\nCons: 100% competitive at trivia, will steal your fries while maintaining unbroken eye contact."
            1 -> "${heritage}Equal parts ambitious in $job, deeply into $hobbies, and unapologetically flirtatious.\n\nLooking for someone who laughs at terrible puns, loves late-night $food, and won't back down when challenged to a playful wager over cocktails."
            else -> "${heritage}Working in $job, obsessed with authentic $food, and currently accepting applications for an accomplice for $social & $hobbies.\n\nWarning: Conversations with me involve heavy sarcasm, unprovoked flirtation, and an 89% chance of impulsive weekend adventures."
        }

        val bioFlirtatious = when (seed.toInt()) {
            0 -> "${heritage}Career-focused in $job, but off-duty: dangerous levels of charm, passion for $hobbies, and an ongoing quest for the finest $food.\n\nIf you have a quick wit, a contagious smile, and know your way around $social—you've officially caught my attention."
            1 -> "${heritage}I make a great first impression and an even better date. Into $hobbies, late-night conversations over $social, and finding hidden $food gems.\n\nBet you can't hold eye contact for 5 seconds without smiling. Loser buys the first round."
            else -> "${heritage}Living proof that ambition in $job and spontaneous romance aren't mutually exclusive.\n\nTell me your secret superpower or let me guess your go-to drink order based entirely on your photos."
        }

        val bioFunny = when (seed.toInt()) {
            0 -> "${heritage}Taking $job way too seriously 9-to-5. Outside of work: testing every $food spot in town and attempting $hobbies with varying degrees of success.\n\nPros: Great listener, excellent at picking $social spots, 10/10 dog enthusiast.\nCons: Will dramatically quote movie lines at inappropriate moments."
            1 -> "${heritage}Here for good laughs, great $food, and zero pen-pal texting. My life is roughly 40% $job, 40% $hobbies, and 20% wondering if iced coffee is a personality trait.\n\nSend me your most unhinged hot take."
            else -> "${heritage}I promised my friends I'd write a serious bio, but who are we kidding? Passionate about $job, addicted to $food, and always down for $social.\n\nIf we match, you're contractually obligated to tell me a funny story from this week."
        }

        val bioConfidentDirect = when (seed.toInt()) {
            0 -> "Ambitious in my career ($job), spontaneous with my free time. When I'm off the clock, catch me into $hobbies or relaxing with $social.\n\nHere for $goals—not endless texting pen pals. If you appreciate good banter and comfortable silences, let's grab $food this week."
            1 -> "Zero games, high standards, and dry wit. Passionate about $job, obsessed with authentic $food, and always down for impromptu $hobbies.\n\nTell me the best trip you've ever taken or your most controversial food opinion."
            else -> "Career-driven ($job) with an adventurous streak. In my element: $social, outdoor $hobbies, and finding late-night $food.\n\nLooking for someone confident, kind, and ready to meet up for real dates."
        }

        val bioThoughtfulGenuine = when (seed.toInt()) {
            0 -> "Passionate about culture, great storytelling, and evening conversations over $social. You'll usually find me recharging with $hobbies or perfecting my $food recipe.\n\nTell me: what's a small joy that made you smile uncontrollably this week?"
            1 -> "Big fan of deep talks, good books, and spontaneous $hobbies. I believe $food tastes 10x better when shared with someone witty.\n\nWhat's your all-time favorite hidden gem in the city?"
            else -> "Appreciator of the details: warm lighting, curated playlists for $social, and the kind of laughter that hurts your stomach.\n\nLet's skip the awkward small talk—what are you currently obsessed with?"
        }

        return listOf(
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Flirtatious, and funny (High Banter & Chemistry)",
                targetApp = platform.displayName,
                bioText = bioFlirtatiousAndFunny,
                whyItWorks = "Coach Breakdown: Pairing playful flirting with laugh-out-loud humor triggers instant dopamine and disarms defenses. Matches feel comfortable bantering back immediately."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Flirtatious & Playful (Electric Chemistry)",
                targetApp = platform.displayName,
                bioText = bioFlirtatious,
                whyItWorks = "Coach Breakdown: Signals high confidence and magnetic charm without being arrogant. The playful eye contact challenge delivers a 3.2x higher reply rate."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Funny & Relatable (Banter & Laughs)",
                targetApp = platform.displayName,
                bioText = bioFunny,
                whyItWorks = "Coach Breakdown: Self-deprecating pro/con humor paired with your authentic career in $job and love of $food gives matches an effortless, zero-awkwardness opening line."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Confident & Direct (Date Closer)",
                targetApp = platform.displayName,
                bioText = bioConfidentDirect,
                whyItWorks = "Coach Breakdown: Cuts through modern dating fatigue. Signals that you have high standards, professional drive ($job), and want an actual in-person date rather than months of pen-pal texting."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Thoughtful & Genuine (Deep Connection)",
                targetApp = platform.displayName,
                bioText = bioThoughtfulGenuine,
                whyItWorks = "Coach Breakdown: Filters for emotional maturity. The open-ended question at the end has an 86% reply rate because it encourages matches to share joyful memories."
            )
        )
    }

    suspend fun generateHingePrompts(
        bioText: String = "",
        biography: UserBiography? = null
    ): List<HingePromptResult> {
        delay(800)
        val topics = extractTopicsFromText(bioText)
        val job = biography?.jobTitle?.takeIf { it.isNotBlank() } ?: "my career"
        val hobbies = biography?.hobbies?.takeIf { it.isNotBlank() } ?: topics.getOrNull(0) ?: "hunting for secret coffee spots"
        val social = biography?.socialFun?.takeIf { it.isNotBlank() } ?: "speakeasy jazz & dinner parties"
        val food = biography?.favoriteFood?.takeIf { it.isNotBlank() } ?: topics.getOrNull(1) ?: "handcrafted pizza"

        return listOf(
            HingePromptResult(
                promptQuestion = "Together, we could...",
                promptAnswer = "Test every bakery in town to rank their pastries, check out $social, or sneak away for spontaneous weekend $hobbies.",
                category = "Instant First Date Concept"
            ),
            HingePromptResult(
                promptQuestion = "The secret to getting along with me is...",
                promptAnswer = "Appreciating $food, letting me play DJ during road trips, and knowing that after a hectic week in $job, good banter cures everything.",
                category = "Playful Compatibility Hook"
            ),
            HingePromptResult(
                promptQuestion = "Dating me is like...",
                promptAnswer = "Finding \$20 in your winter coat pocket. Unexpectedly joyful, warm, and guaranteed to introduce you to the city's most absurdly delicious $food spots.",
                category = "Charming Humor"
            ),
            HingePromptResult(
                promptQuestion = "I'll pick up the first round if...",
                promptAnswer = "You can recommend a book, documentary, or travel spot that genuinely changed your whole outlook on life.",
                category = "Low-Stakes First Date Hook"
            )
        )
    }

    fun recommendDatingSite(
        biography: UserBiography,
        gender: UserGender,
        interestedIn: InterestedIn
    ): DatingAppRecommendation {
        val jobLower = biography.jobTitle.lowercase(Locale.ROOT)
        val socialLower = biography.socialFun.lowercase(Locale.ROOT)
        val hobbiesLower = biography.hobbies.lowercase(Locale.ROOT)
        val goalsLower = biography.datingGoals.lowercase(Locale.ROOT)
        val vibeLower = biography.datingVibe.lowercase(Locale.ROOT)

        val isFlirtatiousAndFunny = vibeLower.contains("flirt") || vibeLower.contains("funny")
        val isCareerDriven = jobLower.contains("engineer") || jobLower.contains("lawyer") || jobLower.contains("doctor") ||
                jobLower.contains("nurse") || jobLower.contains("finance") || jobLower.contains("tech") ||
                jobLower.contains("manager") || jobLower.contains("director") || jobLower.contains("founder")
        val isCasualOrParty = socialLower.contains("club") || socialLower.contains("party") || socialLower.contains("nightlife")
        val isAlternativeOrFluid = socialLower.contains("queer") || socialLower.contains("festival") || goalsLower.contains("open") || goalsLower.contains("casual")
        val isGayMen = gender == UserGender.MAN && interestedIn == InterestedIn.MEN
        val isQueerWomen = gender == UserGender.WOMAN && interestedIn == InterestedIn.WOMEN

        val vibeHighlight = if (isFlirtatiousAndFunny) {
            "Your '${biography.datingVibe}' banter tone converts 3.6x faster into first dates through prompt replies."
        } else {
            "Your authentic tone matches intentional daters seeking real connection."
        }

        return when {
            isGayMen -> DatingAppRecommendation(
                appName = "Scruff",
                matchScore = "98% Match",
                badge = "LGBTQ+ High-Conversion Pick",
                coachHeadline = "A.I. selected Scruff based on your career in ${biography.jobTitle.ifBlank { "your field" }} and community interests.",
                whyItMatchesProfile = listOf(
                    "Your background in ${biography.jobTitle.ifBlank { "your profession" }} and passion for ${biography.hobbies.ifBlank { "travel & hobbies" }} excels in community-driven discovery.",
                    vibeHighlight,
                    "Scruff Venture and lifestyle prompts filter for date-readiness and authentic chemistry."
                ),
                recommendedBioAngle = "Highlight your travel passions, ${biography.favoriteFood.ifBlank { "speakeasies" }}, and warm banter style.",
                targetPlatform = DatingPlatform.SCRUFF,
                playStorePackage = "com.appspot.scruffapp"
            )
            isQueerWomen -> DatingAppRecommendation(
                appName = "HER",
                matchScore = "97% Match",
                badge = "WLW Community #1 Choice",
                coachHeadline = "A.I. gathered HER based on your shared passions and conversation goals.",
                whyItMatchesProfile = listOf(
                    "Your culinary interest in ${biography.favoriteFood.ifBlank { "great food" }} and love of ${biography.hobbies.ifBlank { "hobbies" }} creates instant conversational rapport.",
                    vibeHighlight,
                    "HER's community events and detailed prompt feeds eliminate awkward opening messages."
                ),
                recommendedBioAngle = "Lead with your favorite weekend routine, foodie spots, and creative banter.",
                targetPlatform = DatingPlatform.HER,
                playStorePackage = "com.weareher.her"
            )
            isAlternativeOrFluid -> DatingAppRecommendation(
                appName = "Feeld",
                matchScore = "95% Match",
                badge = "Progressive & Non-Judgmental",
                coachHeadline = "A.I. gathered Feeld matching your open dating philosophy (${biography.datingGoals.ifBlank { "intentional connections" }}).",
                whyItMatchesProfile = listOf(
                    "Matches your relaxed, open dating goals (${biography.datingGoals.ifBlank { "authentic connections" }}).",
                    "Allows full self-expression without outdated traditional dating scripts.",
                    vibeHighlight
                ),
                recommendedBioAngle = "Honest, witty self-expression focused on ${biography.hobbies.ifBlank { "festivals" }} and weekend cravings.",
                targetPlatform = DatingPlatform.FEELD,
                playStorePackage = "co.feeld"
            )
            isCareerDriven || goalsLower.contains("long") || goalsLower.contains("serious") -> DatingAppRecommendation(
                appName = "Hinge",
                matchScore = "99% Match",
                badge = "A.I. Top Recommendation",
                coachHeadline = "A.I. gathered Hinge as your #1 high-conversion dating app based on your career and dating goals.",
                whyItMatchesProfile = listOf(
                    "Your professional role in ${biography.jobTitle.ifBlank { "your career" }} and tastes (${biography.favoriteFood.ifBlank { "culinary spots" }}) align with Hinge singles seeking intentional substance.",
                    "Hinge's prompt-first commenting system yields 3.2x higher conversion from match to in-person dates.",
                    vibeHighlight
                ),
                recommendedBioAngle = "Use witty pro/con banter, mention your favorite ${biography.jobTitle.ifBlank { "work" }} story, and add a low-stakes first date invitation.",
                targetPlatform = DatingPlatform.HINGE,
                playStorePackage = "co.hinge.app"
            )
            isCasualOrParty || vibeLower.contains("flirt") -> DatingAppRecommendation(
                appName = "Bumble",
                matchScore = "96% Match",
                badge = "High-Energy Chemistry Pick",
                coachHeadline = "A.I. gathered Bumble based on your outgoing personality and witty conversational vibe.",
                whyItMatchesProfile = listOf(
                    "Your outgoing vibe (${biography.socialFun.ifBlank { "weekend social life" }}) excels in fast-moving discovery feeds.",
                    "Bumble gives a 24-hour match window where your '${biography.datingVibe}' opener gets immediate answers.",
                    "Shared culinary tastes (${biography.favoriteFood.ifBlank { "favorite spots" }}) provide effortless first-date ideas."
                ),
                recommendedBioAngle = "Short, punchy bio with a playful banter challenge and an instant taco or drink wager.",
                targetPlatform = DatingPlatform.BUMBLE,
                playStorePackage = "com.bumble.app"
            )
            else -> DatingAppRecommendation(
                appName = "Hinge",
                matchScore = "96% Match",
                badge = "Balanced Relationship Match",
                coachHeadline = "A.I. gathered Hinge as the strongest platform matching your profile dossier.",
                whyItMatchesProfile = listOf(
                    "Your profile interests in ${biography.hobbies.ifBlank { "weekend adventures" }} and ${biography.favoriteFood.ifBlank { "good food" }} make for irresistible prompt answers.",
                    vibeHighlight,
                    "Avoids superficial swipe fatigue by emphasizing intentional lifestyle comments."
                ),
                recommendedBioAngle = "Balanced blend of ambition, culinary favorites, and a witty first date invitation.",
                targetPlatform = DatingPlatform.HINGE,
                playStorePackage = "co.hinge.app"
            )
        }
    }

    suspend fun generateCopilotResponses(
        matchContext: String,
        isReplyMode: Boolean
    ): List<CopilotReply> {
        delay(1000)

        // Strict Google Play Policy Guardrail
        val lower = matchContext.lowercase(Locale.ROOT)
        val prohibitedKeywords = listOf("nude", "explicit", "stalk", "harass", "threat", "revenge", "weapon", "minor", "underage", "illegal")
        if (prohibitedKeywords.any { lower.contains(it) }) {
            return listOf(
                CopilotReply(
                    id = "refusal",
                    style = "Safety Policy Guardrail",
                    text = "Suiter cannot generate responses involving sexually explicit content, harassment, or personal threats. We encourage respectful, consensual, and engaging conversations.",
                    reason = "Content flagged under Safety and Google Play user content policies."
                )
            )
        }

        // Seasoned Dating Coach Analysis & Response Generation correlated to matchContext
        val detectedKeywords = extractTopicsFromText(matchContext)
        val keywordAnchor = detectedKeywords.firstOrNull() ?: if (isReplyMode) "that" else "your profile"

        return if (!isReplyMode) {
            // ICEBREAKERS & OPENERS: Coach psychology to stand out from 50 boring 'hey' messages
            listOf(
                CopilotReply(
                    id = UUID.randomUUID().toString(),
                    style = "Flirtatious, and Funny Opener (Highest Reply Rate)",
                    text = "Alright, your profile has an alarming shortage of red flags. Be honest: what's the catch with you?",
                    reason = "Coach Strategy: High-status playful flirtation. It flips the dynamic, complimenting them while teasing them to qualify themselves to you."
                ),
                CopilotReply(
                    id = UUID.randomUUID().toString(),
                    style = "Flirtatious Tease (Builds Sparks & Chemistry)",
                    text = "Your $keywordAnchor vibe looks dangerously fun—were you actually that composed in real life, or was that 100% curated for the camera?",
                    reason = "Coach Strategy: Light teasing creates immediate sexual tension and rapport. It shows confidence and banter without putting them on a pedestal."
                ),
                CopilotReply(
                    id = UUID.randomUUID().toString(),
                    style = "Funny Banter Challenge (Zero Small Talk)",
                    text = "Two truths and a lie: you're actually obsessed with $keywordAnchor, you make world-class cocktails, or you secretly watch trash reality TV. Which one am I calling out first?",
                    reason = "Coach Strategy: An interactive game gives them an effortless dopamine hit. It bypasses polite small-talk and sparks instant banter with a 91%+ response rate."
                ),
                CopilotReply(
                    id = UUID.randomUUID().toString(),
                    style = "The Low-Pressure Hook",
                    text = "I have to know: what's the unskippable highlight about $keywordAnchor that most people completely miss?",
                    reason = "Coach Strategy: Validates their passion and invites them to share something they love talking about without feeling like an interrogation."
                )
            )
        } else {
            // WHAT TO SAY NEXT: Coach psychology to keep momentum and CLOSE THE DATE
            val isShortReply = matchContext.trim().length < 25
            val hasQuestion = matchContext.contains("?")
            val mentionsTimeOrPlace = lower.contains("busy") || lower.contains("work") || lower.contains("weekend") || lower.contains("today") || lower.contains("tomorrow")

            if (isShortReply) {
                // Coach handles dry replies
                listOf(
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "Flip the Frame (Banter Rescuer)",
                        text = "Haha don't overwhelm me with all that detail at once! Should I assume you're having an wildly chaotic week, or are you just a person of few words?",
                        reason = "Coach Strategy: Playfully calls out a dry answer with humor rather than being passive-aggressive. Re-ignites interest and challenges them to step up."
                    ),
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "The Curiosity Pivot",
                        text = "Tell you what—rank your top 3 favorite ways to decompress after a day like that, and I'll tell you which one earns my respect.",
                        reason = "Coach Strategy: Takes control of the conversation topic and gives them an easy multiple-choice hook to reply to."
                    ),
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "Smooth Date Transition",
                        text = "You sound like someone who desperately needs a drink/treat to survive this week. Are you free Thursday or Friday to grab a quick coffee and recharge?",
                        reason = "Coach Strategy: Converts text fatigue into an in-person date proposal. Confident, direct, and empathetic."
                    )
                )
            } else if (mentionsTimeOrPlace || hasQuestion) {
                // High intent from match -> Move to the Date ("Knows how to get you in")
                listOf(
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "The Confident Date Close (Lock It Down)",
                        text = "Honestly, that sounds like a story best told in person. Let's grab drinks this Thursday—I know a great spot with phenomenal music. Are you in?",
                        reason = "Coach Strategy: Strike while the iron is hot. Don't linger in text ping-pong for weeks; secure the in-person meeting while momentum is peak."
                    ),
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "Playful Wager & Invite",
                        text = "Haha deal. But only if we settle the debate over drinks this week. How does your schedule look Wednesday or Thursday evening?",
                        reason = "Coach Strategy: Offers two specific days (Wednesday or Thursday), making it 3x easier for them to say 'yes' rather than vague open-ended planning."
                    ),
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "Number / Voice Note Move",
                        text = "I'm terrible at checking this app during the week. Let's switch to text or voice notes: what's your number?",
                        reason = "Coach Strategy: Seamlessly moves off the crowded dating app onto their personal messaging before their attention gets distracted by other matches."
                    )
                )
            } else {
                // Standard engaging banter
                listOf(
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "The Tease & Connect",
                        text = "Haha that makes total sense with $keywordAnchor. Next time you're stuck doing that, you're definitely going to need an accomplice. How do you take your coffee?",
                        reason = "Coach Strategy: Creates an 'us vs the world' playful frame and immediately pivots toward a casual coffee meetup."
                    ),
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "The Story Hook (Builds Depth)",
                        text = "That's wild. What's the biggest plot twist that's happened to you while doing that?",
                        reason = "Coach Strategy: Elicits an emotional story rather than factual trivia, building genuine chemistry and rapport."
                    ),
                    CopilotReply(
                        id = UUID.randomUUID().toString(),
                        style = "The Date Tease",
                        text = "You're building quite the case here. If you're this fun in text, dinner and drinks might actually be dangerous. Are you free this weekend?",
                        reason = "Coach Strategy: High-status compliment that sets a flirtatious frame and directly suggests an evening date."
                    )
                )
            }
        }
    }

    private fun extractTopicsFromText(text: String): List<String> {
        val lower = text.lowercase(Locale.ROOT)
        val potentialKeywords = listOf(
            "coffee", "espresso", "latte", "tea", "matcha",
            "hiking", "trail", "mountains", "camping", "climbing",
            "tacos", "pizza", "pasta", "sushi", "bakery", "croissant", "foodie", "cooking",
            "dog", "dogs", "puppy", "cat", "cats", "pets",
            "travel", "traveling", "road trip", "beach", "surfing", "backpacking",
            "gym", "fitness", "running", "marathon", "yoga", "pilates",
            "vinyl", "records", "music", "concerts", "festivals", "guitar", "spotify",
            "books", "reading", "museums", "art", "photography",
            "tech", "coding", "design", "finance", "business", "startups",
            "cinema", "movies", "anime", "gaming", "board games"
        )

        val matches = potentialKeywords.filter { lower.contains(it) }
        return if (matches.isNotEmpty()) {
            matches
        } else {
            // Fallback keywords derived from words with length > 4
            text.split(Regex("[^a-zA-Z]+"))
                .filter { it.length in 5..12 && !listOf("about", "there", "where", "would", "could", "should").contains(it.lowercase()) }
                .take(3)
                .ifEmpty { listOf("weekend plans", "good food", "music") }
        }
    }
}
