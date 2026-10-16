package com.example.data.model

object CompatibleAppsProvider {

    fun getCompatibleApps(
        gender: UserGender,
        interestedIn: InterestedIn
    ): List<CompatibleApp> {
        return when {
            gender == UserGender.MAN && interestedIn == InterestedIn.MEN -> getMenSeekingMenApps()
            gender == UserGender.WOMAN && interestedIn == InterestedIn.WOMEN -> getWomenSeekingWomenApps()
            gender == UserGender.MAN && interestedIn == InterestedIn.WOMEN -> getMenSeekingWomenApps()
            gender == UserGender.WOMAN && interestedIn == InterestedIn.MEN -> getWomenSeekingMenApps()
            else -> getInclusiveApps()
        }
    }

    private fun getMenSeekingMenApps(): List<CompatibleApp> {
        return listOf(
            CompatibleApp(
                id = "grindr",
                name = "Grindr",
                badge = "LGBTQ+ #1 Pick",
                matchScore = "99% Match",
                audience = "Men seeking Men",
                description = "The world's most widely used location-based social app for gay, bi, trans, and queer men.",
                coachStrategy = "Coach Strategy: Clear, friendly face photos outperform headless torso shots by 320% in genuine date conversions. Add your neighborhood, workout style, and weekend vibe in the bio.",
                keyFeatures = listOf("Nearby grid discovery", "Private albums with tap share", "Direct chat & saved phrases"),
                playStorePackage = "com.grindrapp.android",
                ratingScore = "4.2★",
                associatedPlatform = DatingPlatform.GRINDR
            ),
            CompatibleApp(
                id = "scruff",
                name = "Scruff",
                badge = "Community Favorite",
                matchScore = "96% Match",
                audience = "Men seeking Men",
                description = "Global community dating app for gay, bi, trans & queer men, focused on authentic connections, travel, and events.",
                coachStrategy = "Coach Strategy: Scruff community members appreciate outdoors, active lifestyles, and travel. Use Scruff Venture before your trips to meet locals in advance.",
                keyFeatures = listOf("Scruff Venture (travel connections)", "Local LGBTQ+ events & nightlife", "Advanced profile filters"),
                playStorePackage = "com.appspot.scruffapp",
                ratingScore = "4.4★",
                associatedPlatform = DatingPlatform.SCRUFF
            ),
            CompatibleApp(
                id = "hinge_m4m",
                name = "Hinge",
                badge = "Serious Romance",
                matchScore = "93% Match",
                audience = "Men seeking Men",
                description = "Designed to be deleted. Very popular among queer men seeking intentional dating and long-term relationships.",
                coachStrategy = "Coach Strategy: Use all 3 prompt answers to showcase your warmth and life ambition. Leave thoughtful comments directly on photo prompts instead of just tapping like.",
                keyFeatures = listOf("High-intent prompt commenting", "Voice & video prompts", "Detailed relationship goals"),
                playStorePackage = "co.hinge.app",
                ratingScore = "4.5★",
                associatedPlatform = DatingPlatform.HINGE
            ),
            CompatibleApp(
                id = "archer",
                name = "Archer",
                badge = "Modern & Social",
                matchScore = "91% Match",
                audience = "Men seeking Men",
                description = "Modern gay dating app blending visual social feeds, selfie verification, and personality prompts.",
                coachStrategy = "Coach Strategy: Post natural daylight portraits and high-energy lifestyle snaps. Complete selfie verification early to earn top visibility in the chronological feed.",
                keyFeatures = listOf("Selfie verification badge", "Chronological photo feeds", "Interactive prompt tags"),
                playStorePackage = "com.archer.dating",
                ratingScore = "4.3★",
                associatedPlatform = DatingPlatform.ALL
            ),
            CompatibleApp(
                id = "tinder_m4m",
                name = "Tinder",
                badge = "High Volume",
                matchScore = "89% Match",
                audience = "Men seeking Men",
                description = "Vast global network with dedicated LGBTQ+ orientation tags and quick local discovery.",
                coachStrategy = "Coach Secret: Keep your bio punchy and energetic. Pair a high-contrast smile lead photo with your top Spotify artist to initiate easy music banter.",
                keyFeatures = listOf("Orientation filters", "Global Passport feature", "Spotify & Instagram integration"),
                playStorePackage = "com.tinder",
                ratingScore = "4.0★",
                associatedPlatform = DatingPlatform.TINDER
            ),
            CompatibleApp(
                id = "jackd",
                name = "Jack'd",
                badge = "Diverse & Inclusive",
                matchScore = "88% Match",
                audience = "Men seeking Men",
                description = "Inclusive global platform for gay, bi, trans, and queer men of color and their allies worldwide.",
                coachStrategy = "Coach Strategy: Highlight authentic personality, cultural passions, and hobbies. Sincere bios with friendly smiles get the highest response rates.",
                keyFeatures = listOf("Global discovery grid", "Match stream & visitor logs", "Multi-photo profiles"),
                playStorePackage = "com.lucid.jackd",
                ratingScore = "4.1★",
                associatedPlatform = DatingPlatform.GRINDR
            )
        )
    }

    private fun getWomenSeekingWomenApps(): List<CompatibleApp> {
        return listOf(
            CompatibleApp(
                id = "her",
                name = "HER",
                badge = "LGBTQ+ #1 Pick",
                matchScore = "99% Match",
                audience = "Women seeking Women",
                description = "Award-winning dating & community app created exclusively for LGBTQ+ women and queer non-binary people.",
                coachStrategy = "Coach Strategy: HER prioritizes community connection and authentic vulnerability. Include photos with pets, artistic hobbies, and outdoor moments. Share your favorite queer culture references!",
                keyFeatures = listOf("Verified women-only community", "Curated queer community events", "Queer identity & pronoun tags"),
                playStorePackage = "com.weareher.her",
                ratingScore = "4.4★",
                associatedPlatform = DatingPlatform.HER
            ),
            CompatibleApp(
                id = "zoe",
                name = "Zoe",
                badge = "Verified Community",
                matchScore = "95% Match",
                audience = "Women seeking Women",
                description = "Cutting-edge dating app for lesbian, bi, and queer women with strict photo verification and personality matching.",
                coachStrategy = "Coach Strategy: Complete the personality questions. WLW daters value emotional intelligence and shared lifestyle goals.",
                keyFeatures = listOf("Strict selfie verification", "Personality match percentages", "Private and safe messaging"),
                playStorePackage = "com.zoe.dating",
                ratingScore = "4.3★",
                associatedPlatform = DatingPlatform.HER
            ),
            CompatibleApp(
                id = "bumble_w4w",
                name = "Bumble",
                badge = "High Active Pool",
                matchScore = "93% Match",
                audience = "Women seeking Women",
                description = "In same-gender matches, either person can send the first message within 24 hours. Very active and engaged queer community.",
                coachStrategy = "Coach Strategy: Send a message within the first 3 hours of matching! Mention something specific from their bio or third photo to stand out immediately.",
                keyFeatures = listOf("24-hour match window", "Question game icebreakers", "Interest & values badges"),
                playStorePackage = "com.bumble.app",
                ratingScore = "4.2★",
                associatedPlatform = DatingPlatform.BUMBLE
            ),
            CompatibleApp(
                id = "hinge_w4w",
                name = "Hinge",
                badge = "Deep Romance",
                matchScore = "92% Match",
                audience = "Women seeking Women",
                description = "Designed to be deleted. Exceptional for queer women looking for intentional romance and serious life partners.",
                coachStrategy = "Coach Strategy: Use the 'Together we could...' prompt with a cozy date concept (e.g. farmers market + coffee). Prompts that suggest specific activities get the best replies.",
                keyFeatures = listOf("Targeted prompt commenting", "Relationship intent indicators", "Voice notes & prompts"),
                playStorePackage = "co.hinge.app",
                ratingScore = "4.5★",
                associatedPlatform = DatingPlatform.HINGE
            ),
            CompatibleApp(
                id = "feeld_w4w",
                name = "Feeld",
                badge = "Open & Progressive",
                matchScore = "89% Match",
                audience = "Women seeking Women",
                description = "Safe space for open-minded dating, exploring queer identities, ethical non-monogamy, and authentic desires.",
                coachStrategy = "Coach Strategy: Feeld encourages radical honesty. State your communication style and relationship boundaries upfront with confidence.",
                keyFeatures = listOf("Expansive gender & orientation options", "Solo or coupled profiles", "Incognito mode options"),
                playStorePackage = "co.feeld",
                ratingScore = "4.1★",
                associatedPlatform = DatingPlatform.FEELD
            ),
            CompatibleApp(
                id = "tinder_w4w",
                name = "Tinder",
                badge = "Widest Reach",
                matchScore = "87% Match",
                audience = "Women seeking Women",
                description = "Massive user base with strong LGBTQ+ presence, great for connecting in both large cities and smaller towns.",
                coachStrategy = "Coach Secret: Use orientation stickers and list your favorite local spots in your bio to quickly find common ground.",
                keyFeatures = listOf("LGBTQ+ orientation stickers", "Global discovery", "Verified photo badges"),
                playStorePackage = "com.tinder",
                ratingScore = "4.0★",
                associatedPlatform = DatingPlatform.TINDER
            )
        )
    }

    private fun getMenSeekingWomenApps(): List<CompatibleApp> {
        return listOf(
            CompatibleApp(
                id = "hinge_m4w",
                name = "Hinge",
                badge = "Highest Date Rate",
                matchScore = "98% Match",
                audience = "Men seeking Women",
                description = "The premier app for intentional relationships. Matches occur through comments on prompts and photos.",
                coachStrategy = "Coach Strategy: Never send a plain like! Leave a witty comment or playful question on her 2nd photo or prompt. Make sure your lead photo has great lighting and an authentic smile.",
                keyFeatures = listOf("Comment-first matching", "Clear relationship intent filters", "Audio & video prompts"),
                playStorePackage = "co.hinge.app",
                ratingScore = "4.5★",
                associatedPlatform = DatingPlatform.HINGE
            ),
            CompatibleApp(
                id = "bumble_m4w",
                name = "Bumble",
                badge = "Women Message First",
                matchScore = "95% Match",
                audience = "Men seeking Women",
                description = "Women make the first move within 24 hours. Filters out spam and guarantees active interest.",
                coachStrategy = "Coach Strategy: Your bio must give her an easy, frictionless conversation opener! Include a fun debate or two truths & a lie in your bio so she can message you effortlessly.",
                keyFeatures = listOf("24-hour match timer", "Compliments before match", "Photo verification"),
                playStorePackage = "com.bumble.app",
                ratingScore = "4.2★",
                associatedPlatform = DatingPlatform.BUMBLE
            ),
            CompatibleApp(
                id = "tinder_m4w",
                name = "Tinder",
                badge = "Largest Dating Pool",
                matchScore = "90% Match",
                audience = "Men seeking Women",
                description = "Fast-paced visual matching. The most popular dating app in the world across all age brackets.",
                coachStrategy = "Coach Strategy: Visual impact is everything. Keep your lead photo clear and solo (no group shots as #1). Keep bio under 30 words with high-energy humor.",
                keyFeatures = listOf("Smart Photos algorithm", "Super Likes & Boosts", "Music & lifestyle integrations"),
                playStorePackage = "com.tinder",
                ratingScore = "4.0★",
                associatedPlatform = DatingPlatform.TINDER
            ),
            CompatibleApp(
                id = "cmb_m4w",
                name = "Coffee Meets Bagel",
                badge = "Curated Daily Matches",
                matchScore = "89% Match",
                audience = "Men seeking Women",
                description = "Delivers curated matches at noon each day, prioritizing thoughtful connections over infinite swiping.",
                coachStrategy = "Coach Strategy: CMB women are looking for depth and stability. Emphasize career ambition, family values, and what you enjoy doing on relaxed weekends.",
                keyFeatures = listOf("Noon curated match drops", "In-depth profile questions", "Icebreaker suggestions"),
                playStorePackage = "com.coffeemeetsbagel",
                ratingScore = "4.3★",
                associatedPlatform = DatingPlatform.ALL
            ),
            CompatibleApp(
                id = "boo_m4w",
                name = "Boo",
                badge = "Personality & MBTI",
                matchScore = "88% Match",
                audience = "Men seeking Women",
                description = "Connects based on personality psychology (MBTI & Enneagram) with social community universes.",
                coachStrategy = "Coach Strategy: Showcase your intellectual curiosity and passions. Thoughtful bios and engaging questions perform exceptionally well on Boo.",
                keyFeatures = listOf("MBTI compatibility analysis", "Interest universes & forums", "Soulmate compatibility radar"),
                playStorePackage = "enterprises.dating.boo",
                ratingScore = "4.4★",
                associatedPlatform = DatingPlatform.ALL
            ),
            CompatibleApp(
                id = "feeld_m4w",
                name = "Feeld",
                badge = "Open & Curious",
                matchScore = "86% Match",
                audience = "Men seeking Women",
                description = "For open-minded singles and couples seeking modern, non-judgmental connections.",
                coachStrategy = "Coach Strategy: Transparency is king. State your intentions respectfully and authentically without pickup lines.",
                keyFeatures = listOf("Non-traditional relationship options", "Private photos", "Fluid boundary definitions"),
                playStorePackage = "co.feeld",
                ratingScore = "4.1★",
                associatedPlatform = DatingPlatform.FEELD
            )
        )
    }

    private fun getWomenSeekingMenApps(): List<CompatibleApp> {
        return listOf(
            CompatibleApp(
                id = "bumble_w4m",
                name = "Bumble",
                badge = "You Set The Pace",
                matchScore = "98% Match",
                audience = "Women seeking Men",
                description = "Women start the conversation, eliminating unsolicited messages and giving you control.",
                coachStrategy = "Coach Strategy: Skip the basic 'hey'—ask about his cooking photo, dog, or hiking trip. A contextual opener gets an 88% response rate!",
                keyFeatures = listOf("Women message first", "24h match clock", "Snooze & travel modes"),
                playStorePackage = "com.bumble.app",
                ratingScore = "4.2★",
                associatedPlatform = DatingPlatform.BUMBLE
            ),
            CompatibleApp(
                id = "hinge_w4m",
                name = "Hinge",
                badge = "Best Relationship Pool",
                matchScore = "96% Match",
                audience = "Women seeking Men",
                description = "Relationship-focused app where singles bond over answers to creative lifestyle prompts.",
                coachStrategy = "Coach Strategy: Select prompts that invite date proposals, like 'My ideal Sunday' or 'I'll know it's time to delete Hinge when...'.",
                keyFeatures = listOf("Detailed relationship intent filters", "Standouts curated picks", "Direct photo commenting"),
                playStorePackage = "co.hinge.app",
                ratingScore = "4.5★",
                associatedPlatform = DatingPlatform.HINGE
            ),
            CompatibleApp(
                id = "cmb_w4m",
                name = "Coffee Meets Bagel",
                badge = "Serious Daters",
                matchScore = "91% Match",
                audience = "Women seeking Men",
                description = "Focuses on meaningful connections with daily curated matches at noon.",
                coachStrategy = "Coach Strategy: Describe what makes you laugh and what you value in a partner. Sincerity converts to coffee dates quickly.",
                keyFeatures = listOf("Daily curated batch", "Detailed interest profiles", "Curated chats"),
                playStorePackage = "com.coffeemeetsbagel",
                ratingScore = "4.3★",
                associatedPlatform = DatingPlatform.ALL
            ),
            CompatibleApp(
                id = "theleague_w4m",
                name = "The League",
                badge = "Ambitious Professionals",
                matchScore = "89% Match",
                audience = "Women seeking Men",
                description = "Curated dating community for career-driven singles, with LinkedIn privacy protections.",
                coachStrategy = "Coach Strategy: Use crisp, high-resolution lifestyle photos. Showcasing balanced drive and fun social life works best.",
                keyFeatures = listOf("Coworker privacy block", "Speed dating events", "Curated drops"),
                playStorePackage = "com.theleague.theleague",
                ratingScore = "4.1★",
                associatedPlatform = DatingPlatform.ALL
            ),
            CompatibleApp(
                id = "tinder_w4m",
                name = "Tinder",
                badge = "Widest Choices",
                matchScore = "87% Match",
                audience = "Women seeking Men",
                description = "The world's largest dating app, ideal for casual exploration, quick dates, and travel.",
                coachStrategy = "Coach Strategy: Look for verified photo checkmarks and prioritize bios with genuine substance to filter matches quickly.",
                keyFeatures = listOf("Verified profiles", "Passions tags", "Fast swipe matching"),
                playStorePackage = "com.tinder",
                ratingScore = "4.0★",
                associatedPlatform = DatingPlatform.TINDER
            )
        )
    }

    private fun getInclusiveApps(): List<CompatibleApp> {
        return listOf(
            CompatibleApp(
                id = "feeld_all",
                name = "Feeld",
                badge = "Inclusive & Fluid",
                matchScore = "98% Match",
                audience = "All Orientations",
                description = "Progressive platform celebrating diverse relationships, fluid identities, and open-minded dating.",
                coachStrategy = "Coach Strategy: Celebrate your authentic self without traditional labels. Clear intentions and boundaries build great connections.",
                keyFeatures = listOf("Fluid identity filters", "Open connection models", "Safe and private"),
                playStorePackage = "co.feeld",
                ratingScore = "4.1★",
                associatedPlatform = DatingPlatform.FEELD
            ),
            CompatibleApp(
                id = "hinge_all",
                name = "Hinge",
                badge = "Deep Connection",
                matchScore = "95% Match",
                audience = "All Orientations",
                description = "Designed to be deleted, welcoming all gender identities and orientations with nuanced profile prompts.",
                coachStrategy = "Coach Strategy: Emphasize your unique worldview and humor in your prompt choices.",
                keyFeatures = listOf("Nuanced gender & orientation tags", "Interactive prompt commenting", "Audio notes"),
                playStorePackage = "co.hinge.app",
                ratingScore = "4.5★",
                associatedPlatform = DatingPlatform.HINGE
            ),
            CompatibleApp(
                id = "bumble_all",
                name = "Bumble",
                badge = "High Engagement",
                matchScore = "92% Match",
                audience = "All Orientations",
                description = "Dynamic dating app with inclusive settings for everyone to connect comfortably.",
                coachStrategy = "Coach Strategy: Keep your bio fresh with fun conversational hooks and vivid candid photos.",
                keyFeatures = listOf("24-hour match timer", "Interest badges", "Photo verification"),
                playStorePackage = "com.bumble.app",
                ratingScore = "4.2★",
                associatedPlatform = DatingPlatform.BUMBLE
            ),
            CompatibleApp(
                id = "taimi",
                name = "Taimi",
                badge = "LGBTQ+ Community",
                matchScore = "90% Match",
                audience = "LGBTQ+ & Queer",
                description = "All-in-one LGBTQ+ dating app and social network with video stories, live streams, and groups.",
                coachStrategy = "Coach Strategy: Share stories and engage in interest groups to meet like-minded people organically.",
                keyFeatures = listOf("Livestreams & groups", "Customizable queer badges", "Verified profiles"),
                playStorePackage = "com.taimi.android",
                ratingScore = "4.1★",
                associatedPlatform = DatingPlatform.ALL
            ),
            CompatibleApp(
                id = "tinder_all",
                name = "Tinder",
                badge = "Global Reach",
                matchScore = "88% Match",
                audience = "All Orientations",
                description = "The world's largest dating pool with full orientation customization and global reach.",
                coachStrategy = "Coach Strategy: Lead with a bright, welcoming solo photo and concise bio highlighting your passions.",
                keyFeatures = listOf("Full orientation filters", "Global Passport", "Anthems & badges"),
                playStorePackage = "com.tinder",
                ratingScore = "4.0★",
                associatedPlatform = DatingPlatform.TINDER
            )
        )
    }
}
