package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.RosePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibleAppsScreen(
    currentGender: UserGender,
    currentPreference: InterestedIn,
    currentPlatform: DatingPlatform,
    onGenderSelected: (UserGender) -> Unit,
    onPreferenceSelected: (InterestedIn) -> Unit,
    onPlatformSelected: (DatingPlatform) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToBioRewriter: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedGender by remember(currentGender) { mutableStateOf(currentGender) }
    var selectedPreference by remember(currentPreference) { mutableStateOf(currentPreference) }

    val compatibleApps = remember(selectedGender, selectedPreference) {
        CompatibleAppsProvider.getCompatibleApps(selectedGender, selectedPreference)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Compatible Dating Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Targeted match pools & coach strategy",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("compatible_apps_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // Preferences Selector Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preference_selector_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(RosePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = RosePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Your Dating Preferences",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gender Selection: "I am a"
                        Text(
                            text = "I AM A",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
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
                                    onClick = {
                                        selectedGender = gender
                                        onGenderSelected(gender)
                                    },
                                    label = {
                                        Text(
                                            text = when (gender) {
                                                UserGender.MAN -> "Man 👨"
                                                UserGender.WOMAN -> "Woman 👩"
                                                UserGender.NON_BINARY -> "Non-Binary ✨"
                                            },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("gender_chip_${gender.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RosePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preference Selection: "Interested In"
                        Text(
                            text = "INTERESTED IN / SEEKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
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
                                    onClick = {
                                        selectedPreference = pref
                                        onPreferenceSelected(pref)
                                    },
                                    label = {
                                        Text(
                                            text = when (pref) {
                                                InterestedIn.WOMEN -> "Women 👩"
                                                InterestedIn.MEN -> "Men 👨"
                                                InterestedIn.EVERYONE -> "Everyone 💫"
                                            },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preference_chip_${pref.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralAccent,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Coach Tactical Advice Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RosePrimary.copy(alpha = 0.08f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                        )
                        Column {
                            val targetSummary = when {
                                selectedGender == UserGender.MAN && selectedPreference == InterestedIn.MEN ->
                                    "For Men seeking Men: Platforms like Grindr, Scruff, and Archer reward immediate visual clarity. A warm, well-lit headshot increases authentic matches by 3.4x over body-only photos."
                                selectedGender == UserGender.WOMAN && selectedPreference == InterestedIn.WOMEN ->
                                    "For Women seeking Women: Platforms like HER and Zoe emphasize community, shared artistic values, and lifestyle authenticity. Detail your hobbies and creative passions."
                                selectedGender == UserGender.MAN && selectedPreference == InterestedIn.WOMEN ->
                                    "For Men seeking Women: On Bumble, her opener depends on your bio hook. On Hinge, your lead smile and specific prompt answers drive date conversions."
                                selectedGender == UserGender.WOMAN && selectedPreference == InterestedIn.MEN ->
                                    "For Women seeking Men: Use Bumble and Hinge to filter for high-intent suitors. Clear prompts about your ideal weekend filter out pen-pals fast."
                                else ->
                                    "For All Orientations: Modern dating apps like Feeld, Hinge, and Taimi reward transparent boundaries and authentic self-expression."
                            }
                            Text(
                                text = "Dating Coach Strategy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = RosePrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = targetSummary,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Results Heading
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Compatible Platforms (${compatibleApps.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${selectedGender.label} seeking ${selectedPreference.label}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Compatible Apps List
            items(compatibleApps, key = { it.id }) { app ->
                AppMatchCard(
                    app = app,
                    isCurrentActive = app.associatedPlatform == currentPlatform,
                    onOpenPlayStore = {
                        openPlayStore(context, app.playStorePackage)
                    },
                    onSelectPlatform = { platform ->
                        onPlatformSelected(platform)
                        Toast.makeText(
                            context,
                            "Suiter active target set to ${app.name}!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onNavigateToUpload = onNavigateToUpload,
                    onNavigateToBioRewriter = onNavigateToBioRewriter
                )
            }
        }
    }
}

@Composable
private fun AppMatchCard(
    app: CompatibleApp,
    isCurrentActive: Boolean,
    onOpenPlayStore: () -> Unit,
    onSelectPlatform: (DatingPlatform) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToBioRewriter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compatible_app_card_${app.id}")
            .border(
                width = if (isCurrentActive) 2.dp else 1.dp,
                color = if (isCurrentActive) RosePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: App Name + Match Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App Logo Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        when (app.id) {
                                            "grindr" -> Color(0xFFFFB300)
                                            "scruff" -> Color(0xFF6D4C41)
                                            "her" -> Color(0xFFE91E63)
                                            "zoe" -> Color(0xFF9C27B0)
                                            "bumble", "bumble_w4w", "bumble_m4w", "bumble_w4m" -> Color(0xFFFFC107)
                                            "hinge_m4m", "hinge_w4w", "hinge_m4w", "hinge_w4m" -> Color(0xFF263238)
                                            else -> RosePrimary
                                        },
                                        CoralAccent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            if (isCurrentActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RosePrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE TARGET",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Text(
                            text = app.ratingScore + " • " + app.audience,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Match score badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RosePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.matchScore,
                        color = RosePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = app.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Coach Strategy Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.coachStrategy,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Key Feature tags
            if (app.keyFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    app.keyFeatures.take(2).forEach { feat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "✓ $feat",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Open in Play Store button
                OutlinedButton(
                    onClick = onOpenPlayStore,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("open_playstore_${app.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play Store",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Set as Active Target / Optimize Button
                val platform = app.associatedPlatform ?: DatingPlatform.ALL
                Button(
                    onClick = {
                        onSelectPlatform(platform)
                        onNavigateToBioRewriter()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("optimize_for_${app.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentActive) MaterialTheme.colorScheme.secondary else RosePrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCurrentActive) "Optimize Bio" else "Target App",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Safely opens Google Play Store using market:// or fallback https:// URL.
 */
private fun openPlayStore(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Cannot open Play Store on this device", Toast.LENGTH_SHORT).show()
        }
    }
}
