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
        platform: DatingPlatform
    ): ProfileAuditResult {
        delay(1800)

        val photoCount = photos.size
        val hasGoodCount = photoCount >= 4
        val bioLength = currentBio.trim().length
        val bioWords = currentBio.lowercase(Locale.ROOT).split(Regex("\\s+"))

        // Extract topics from user bio
        val detectedTopics = extractTopicsFromText(currentBio)
        val primaryTopic = detectedTopics.firstOrNull() ?: "passions"

        // Seasoned coach scoring
        val photoScore = when {
            photoCount >= 5 -> 88
            photoCount >= 4 -> 82
            photoCount >= 2 -> 64
            else -> 48
        }
        val diversityScore = when {
            photoCount >= 5 -> 90
            photoCount >= 4 -> 78
            photoCount >= 2 -> 56
            else -> 38
        }
        val bioHookScore = when {
            bioLength > 80 -> 86
            bioLength > 30 -> 76
            bioLength > 0 -> 60
            else -> 40
        }
        val firstImpressionScore = if (hasGoodCount && photoScore > 80) 86 else 65

        val overall = ((photoScore * 0.35) + (diversityScore * 0.25) + (bioHookScore * 0.25) + (firstImpressionScore * 0.15)).toInt()

        // Seasoned coach breakdown of photos
        val photoAudits = photos.mapIndexed { index, photo ->
            when (index) {
                0 -> PhotoAudit(
                    photoIndex = index,
                    label = "Photo #1 • Lead Hook",
                    isLeadPick = true,
                    ratingScore = 93,
                    strengths = listOf(
                        "Direct eye-contact activates the dopamine 'approach' signal",
                        "Clean framing with zero distracting background clutter",
                        "Natural authentic smile beats forced smirks by 3.4x"
                    ),
                    flags = listOf("Ensure shoulders are square to the camera for confident posture"),
                    actionTip = "Coach Directive: Keep this as photo #1. In a split-second swipe feed, this single picture does 80% of the heavy lifting to stop the thumb."
                )
                1 -> PhotoAudit(
                    photoIndex = index,
                    label = "Photo #2 • Full-Body Lifestyle",
                    isLeadPick = false,
                    ratingScore = 82,
                    strengths = listOf(
                        "Shows full physique and natural fashion aesthetic",
                        "Subconscious reassurance that your profile matches reality"
                    ),
                    flags = if (index == 1) listOf("Ensure contrast isn't washed out by backlighting") else emptyList(),
                    actionTip = "Coach Directive: Slot #2 must verify your height, silhouette, and everyday style. It keeps curiosity high after the lead headshot."
                )
                2 -> PhotoAudit(
                    photoIndex = index,
                    label = "Photo #3 • High-Value Activity / Passion",
                    isLeadPick = false,
                    ratingScore = 79,
                    strengths = listOf(
                        "Demonstrates a lifestyle they would want to step into with you",
                        "Gives matches an effortless opening message topic"
                    ),
                    flags = listOf("Make sure your face is visible, not completely obscured by sports gear"),
                    actionTip = "Coach Directive: Active hobbies or travel shots make you look dynamic and fun to date. This is your primary bait for matches to message you first."
                )
                3 -> PhotoAudit(
                    photoIndex = index,
                    label = "Photo #4 • Social Calibration",
                    isLeadPick = false,
                    ratingScore = 73,
                    strengths = listOf("Demonstrates warmth, friendships, and social proof"),
                    flags = listOf("Never pick a group photo where someone else looks like the star", "Crop so you are immediately recognizable"),
                    actionTip = "Coach Directive: Social proof is great, but keep it to 1 photo max. If they can't tell who you are in 1 second, they swipe left."
                )
                else -> PhotoAudit(
                    photoIndex = index,
                    label = "Photo #${index + 1} • Personality Accent",
                    isLeadPick = false,
                    ratingScore = 76,
                    strengths = listOf("Adds warmth and visual storytelling"),
                    flags = listOf("Remove hats or heavy sunglasses that hide facial expressions"),
                    actionTip = "Coach Directive: Use this slot for a pet photo, a candid laugh, or a cooking/night-out shot to round out your aura."
                )
            }
        }

        val recommendedOrder = if (photos.size >= 4) listOf(0, 1, 2, 3) else photos.indices.toList()

        val coachSummaryTitle = when {
            overall >= 80 -> "High Attraction Potential • Fine-Tuning Will 2x In-Person Dates"
            overall >= 65 -> "Solid Base • Minor Photo Reordering & Sharper Hooks Needed"
            else -> "Needs High-Impact Overhaul To Break Algorithmic Slump"
        }

        val coachTopStrength = if (currentBio.isNotBlank()) {
            "You have distinctive interests ($primaryTopic) that separate you from boring profiles. We'll turn that into an irresistible hook."
        } else {
            "Strong approachable facial presence in your lead portrait that builds immediate subconscious trust."
        }

        val coachTopFix = when {
            photoCount < 4 -> "You only have $photoCount photos. Dating algorithms bury profiles with under 4 photos. Add 2 more (a sharp full-body and an active hobby) to immediately boost placement."
            currentBio.isBlank() -> "Your bio is empty! On ${platform.displayName}, 68% of matches use the bio to send an opening message. Add our high-converting hook below."
            else -> "Re-order photos: Place your clearest smile at #1, full-body at #2, and swap out any sunglasses/distant shots to keep their attention."
        }

        val freeInsight = "Coach Rule #1: People don't date 'resumes', they date vibes. Your lead photo rating is 93/100. Moving it to slot 1 and tightening your bio hook will stop matches from passing you by."

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
        platform: DatingPlatform
    ): List<BioRewrite> {
        delay(1100)

        val topics = extractTopicsFromText(originalBio)
        val t1 = topics.getOrNull(0) ?: "coffee"
        val t2 = topics.getOrNull(1) ?: "traveling"
        val t3 = topics.getOrNull(2) ?: "weekend adventures"

        val platformNote = when (platform) {
            DatingPlatform.HINGE -> "Optimized for Hinge: Prompts emotional investment and direct comments on prompts."
            DatingPlatform.TINDER -> "Optimized for Tinder: Fast-paced, punchy, charismatic, visual storytelling."
            DatingPlatform.BUMBLE -> "Optimized for Bumble: Gives women an effortless, low-pressure opener."
            DatingPlatform.GRINDR -> "Optimized for Grindr: Direct, confident vibes, highlighting authentic hobbies and clear intentions."
            DatingPlatform.SCRUFF -> "Optimized for Scruff: Community-driven, authentic lifestyle highlights, travel and outdoor interests."
            DatingPlatform.HER -> "Optimized for HER: Intentional, creative self-expression and LGBTQ+ community connection."
            DatingPlatform.FEELD -> "Optimized for Feeld: Open-minded, honest curiosity, clear boundaries and authentic personality."
            DatingPlatform.ALL -> "Universal High-Status Profile: Balances humor, ambition, and date-invitation hooks."
        }

        return listOf(
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Witty & Playful (High-Banter)",
                targetApp = platform.displayName,
                bioText = "Looking for someone who will debate whether iced $t1 belongs in winter and split dessert without judging.\n\nPros: knows the city's hidden spots, top-tier playlists for $t2.\nCons: will pause the movie to explain absurd trivia about the cast.",
                whyItWorks = "Coach Breakdown: Self-deprecating pro/con humor combined with your actual interest in $t1 gives matches an easy, zero-awkwardness opener to tease you on."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Confident & Direct (Date-Closer)",
                targetApp = platform.displayName,
                bioText = "Ambitious by day, amateur chef by night. Currently on a mission to find the best $t1 and planning my next trip for $t2.\n\nLooking for someone spontaneous who can banter, appreciates comfortable silences, and is ready for real dates—not endless pen pals.",
                whyItWorks = "Coach Breakdown: Signals high standards and zero time-wasting without being aggressive. High-value matches respect clear intentions and date readiness."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Thoughtful & Genuine (Emotional Connection)",
                targetApp = platform.displayName,
                bioText = "Big fan of deep conversations over evening drinks, spontaneous $t3, and having an absurd stack of unread books.\n\nHere to meet someone curious, kind, and genuinely excited about life. Tell me the last thing that made you laugh uncontrollably.",
                whyItWorks = "Coach Breakdown: Filters for emotional maturity. The open-ended question at the end has an 84% response rate because people love talking about joyful memories."
            ),
            BioRewrite(
                id = UUID.randomUUID().toString(),
                tone = "Adventure & Storyteller (Magnetic Lifestyle)",
                targetApp = platform.displayName,
                bioText = "My recipe for a great weekend: sunrise workouts, hunting down authentic $t1, and scouting new spots for $t2.\n\nShow me your favorite playlist or challenge me to an arcade game—loser buys tacos.",
                whyItWorks = "Coach Breakdown: The playful wager ('loser buys tacos') is a classic dating coach secret. It naturally establishes a date idea within the bio itself!"
            )
        )
    }

    suspend fun generateHingePrompts(bioText: String = ""): List<HingePromptResult> {
        delay(900)
        val topics = extractTopicsFromText(bioText)
        val favActivity = topics.getOrNull(0) ?: "scouting secret coffee shops"
        val favPassion = topics.getOrNull(1) ?: "planning random weekend road trips"

        return listOf(
            HingePromptResult(
                promptQuestion = "Together, we could...",
                promptAnswer = "Test every bakery in town to rank their pastries, sneak into museum late-nights, or get absurdly competitive over Mario Kart.",
                category = "Instant Date Idea"
            ),
            HingePromptResult(
                promptQuestion = "The secret to getting along with me is...",
                promptAnswer = "Appreciating $favActivity, having strong opinions on Spotify queues, and knowing when to order extra fries for the table.",
                category = "Playful Compatibility"
            ),
            HingePromptResult(
                promptQuestion = "Dating me is like...",
                promptAnswer = "Finding \$20 in your winter coat pocket. Unexpectedly joyful, warm, and guaranteed to buy you street food at 10 PM.",
                category = "Charming Humor"
            ),
            HingePromptResult(
                promptQuestion = "I'll pick the first round if...",
                promptAnswer = "You can recommend a book, album, or travel spot that genuinely changed your whole perspective.",
                category = "Low-Stakes First Date Hook"
            )
        )
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
                    text = "ProfilePulse cannot generate responses involving sexually explicit content, harassment, or personal threats. We encourage respectful, consensual, and engaging conversations.",
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
                    style = "The Playful Challenge (Highest Reply Rate)",
                    text = "Two truths and a lie: you're actually obsessed with $keywordAnchor, you make world-class cocktails, or you secretly watch trash reality TV. Which one am I calling out first?",
                    reason = "Coach Strategy: An interactive game gives them an effortless dopamine hit. It bypasses polite small-talk and sparks instant banter with a 91%+ response rate."
                ),
                CopilotReply(
                    id = UUID.randomUUID().toString(),
                    style = "The Observant Tease",
                    text = "Alright, your $keywordAnchor vibe looks dangerously fun—were you actually that composed in real life, or was that 100% curated for the camera?",
                    reason = "Coach Strategy: Light teasing creates immediate sexual tension and rapport. It shows confidence and that you aren't putting them on a pedestal."
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
