package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatingPlatform
import com.example.data.model.InterestedIn
import com.example.data.model.UserGender
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.RosePrimary

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (platform: DatingPlatform, gender: UserGender, preference: InterestedIn) -> Unit,
    onOpenCompatibleApps: () -> Unit = {},
    onViewPrivacyPolicy: () -> Unit,
    onViewTerms: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Age & Intro, 2: Prominent Disclosure & Consent, 3: Dating App Selection
    var isAdultConfirmed by remember { mutableStateOf(false) }
    var termsAgreed by remember { mutableStateOf(false) }
    var disclosureAgreed by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf(UserGender.MAN) }
    var selectedPreference by remember { mutableStateOf(InterestedIn.WOMEN) }
    var selectedPlatform by remember { mutableStateOf(DatingPlatform.HINGE) }
    var selectedGoal by remember { mutableStateOf("Fix My Lead Photo & Order") }
    var viewingPolicy by remember { mutableStateOf<String?>(null) } // "PRIVACY", "TERMS", null

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ProfilePulse",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = RosePrimary
                    )
                    Text(
                        text = "Step $step of 3",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = RosePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> {
                    // STEP 1: Adults Only & Core Introduction
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(RosePrimary, CoralAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Elevate Your Dating Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AI-powered photo audits, optimal lead-photo selection, and magnetic bio rewrites built to get you noticed on Hinge, Tinder, and Bumble.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Adults-Only Policy Requirement
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = RosePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Adults Only (18+) Requirement",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ProfilePulse is exclusively for adults aged 18 and older. We strictly prohibit minors and enforce zero tolerance for harmful or non-consensual content.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAdultConfirmed = !isAdultConfirmed }
                            ) {
                                Checkbox(
                                    checked = isAdultConfirmed,
                                    onCheckedChange = { isAdultConfirmed = it },
                                    modifier = Modifier.testTag("age_confirm_checkbox")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "I confirm that I am at least 18 years of age.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { step = 2 },
                        enabled = isAdultConfirmed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step1_continue_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                2 -> {
                    // STEP 2: Prominent Disclosure & Terms Gate (Play Compliance Map)
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = RosePrimary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Data Privacy & AI Disclosure",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Before you upload photos or bio text, please review our transparent data handling practices:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DisclosureBullet(
                                icon = Icons.Default.Image,
                                title = "What we collect",
                                desc = "Only the 4-6 profile photos and bio snippets you explicitly select using Android's system photo picker."
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DisclosureBullet(
                                icon = Icons.Default.Psychology,
                                title = "Third-Party AI Processing",
                                desc = "Photos and text are securely transmitted to Gemini AI models solely to generate scoring, ordering tips, and rewrites. Never sold or used for ad targeting."
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DisclosureBullet(
                                icon = Icons.Default.DeleteOutline,
                                title = "Instant Deletion Anytime",
                                desc = "You can delete your photos and wipe your entire account and data at any time directly in Settings with 1 tap."
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Consent Checkboxes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { disclosureAgreed = !disclosureAgreed }
                    ) {
                        Checkbox(
                            checked = disclosureAgreed,
                            onCheckedChange = { disclosureAgreed = it },
                            modifier = Modifier.testTag("disclosure_agree_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I consent to my photos and bio being processed by Gemini AI for profile optimization.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAgreed = !termsAgreed }
                    ) {
                        Checkbox(
                            checked = termsAgreed,
                            onCheckedChange = { termsAgreed = it },
                            modifier = Modifier.testTag("terms_agree_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I accept the Terms of Service and confirm my uploaded content adheres to community guidelines.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { viewingPolicy = "PRIVACY" }) {
                            Text("Privacy Policy", fontSize = 12.sp, color = RosePrimary)
                        }
                        Text("•", modifier = Modifier.align(Alignment.CenterVertically), color = Color.Gray)
                        TextButton(onClick = { viewingPolicy = "TERMS" }) {
                            Text("Terms of Service", fontSize = 12.sp, color = RosePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { step = 3 },
                        enabled = disclosureAgreed && termsAgreed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("step2_consent_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Text("I Agree & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                3 -> {
                    // STEP 3: Dating Preferences, Compatible Apps & Goals
                    Text(
                        text = "Your Dating Preferences",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tell us who you are and who you want to meet so our AI dating coach can tailor your photo ratings and match strategy.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Gender Preference Selection Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "I AM A",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                UserGender.entries.forEach { gender ->
                                    val isSelected = selectedGender == gender
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedGender = gender },
                                        label = {
                                            Text(
                                                text = when (gender) {
                                                    UserGender.MAN -> "Man 👨"
                                                    UserGender.WOMAN -> "Woman 👩"
                                                    UserGender.NON_BINARY -> "Non-Binary ✨"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RosePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "LOOKING FOR / PREFERENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InterestedIn.entries.forEach { pref ->
                                    val isSelected = selectedPreference == pref
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedPreference = pref },
                                        label = {
                                            Text(
                                                text = when (pref) {
                                                    InterestedIn.WOMEN -> "Women 👩"
                                                    InterestedIn.MEN -> "Men 👨"
                                                    InterestedIn.EVERYONE -> "Everyone 💫"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CoralAccent,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compatible Apps Recommendation Highlight
                    val topAppNames = when {
                        selectedGender == UserGender.MAN && selectedPreference == InterestedIn.MEN -> "Grindr, Scruff, Hinge & Archer"
                        selectedGender == UserGender.WOMAN && selectedPreference == InterestedIn.WOMEN -> "HER, Zoe, Bumble & Hinge"
                        selectedGender == UserGender.MAN && selectedPreference == InterestedIn.WOMEN -> "Hinge, Bumble, Tinder & CMB"
                        selectedGender == UserGender.WOMAN && selectedPreference == InterestedIn.MEN -> "Bumble, Hinge & CMB"
                        else -> "Feeld, HER, Grindr, Hinge & Bumble"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCompatibleApps() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = RosePrimary.copy(alpha = 0.08f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RosePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Compatible Apps for You",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = RosePrimary
                                )
                                Text(
                                    text = "Top matches: $topAppNames",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View compatible apps",
                                tint = RosePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Which app is your primary focus?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Platforms sorted intelligently based on user preference
                    val orderedPlatforms = remember(selectedGender, selectedPreference) {
                        when {
                            selectedGender == UserGender.MAN && selectedPreference == InterestedIn.MEN ->
                                listOf(DatingPlatform.GRINDR, DatingPlatform.SCRUFF, DatingPlatform.HINGE, DatingPlatform.TINDER, DatingPlatform.ALL)
                            selectedGender == UserGender.WOMAN && selectedPreference == InterestedIn.WOMEN ->
                                listOf(DatingPlatform.HER, DatingPlatform.BUMBLE, DatingPlatform.HINGE, DatingPlatform.FEELD, DatingPlatform.ALL)
                            else ->
                                listOf(DatingPlatform.HINGE, DatingPlatform.BUMBLE, DatingPlatform.TINDER, DatingPlatform.FEELD, DatingPlatform.ALL)
                        }
                    }

                    orderedPlatforms.forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPlatform = platform }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) RosePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPlatform = platform },
                                    colors = RadioButtonDefaults.colors(selectedColor = RosePrimary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(platform.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(platform.tagline, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "What is your main goal?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    listOf(
                        "Fix My Lead Photo & Order",
                        "Rewrite My Bio & Prompts",
                        "Full Comprehensive Audit"
                    ).forEach { goal ->
                        val isSelected = selectedGoal == goal
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGoal = goal },
                            label = { Text(goal) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RosePrimary.copy(alpha = 0.15f),
                                selectedLabelColor = RosePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onCompleteOnboarding(selectedPlatform, selectedGender, selectedPreference) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("complete_onboarding_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Text("Start Photo Audit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    when (viewingPolicy) {
        "PRIVACY" -> {
            com.example.ui.components.LegalDetailDialog(
                title = "Privacy Policy",
                content = com.example.ui.components.LegalPolicyTexts.PRIVACY_POLICY,
                onDismiss = { viewingPolicy = null }
            )
        }
        "TERMS" -> {
            com.example.ui.components.LegalDetailDialog(
                title = "Terms of Service",
                content = com.example.ui.components.LegalPolicyTexts.TERMS_OF_SERVICE,
                onDismiss = { viewingPolicy = null }
            )
        }
    }
}

@Composable
private fun DisclosureBullet(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RosePrimary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}
