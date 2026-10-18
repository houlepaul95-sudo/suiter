package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DatingAppRecommendation
import com.example.data.model.DatingPlatform
import com.example.data.model.UserBiography
import com.example.ui.theme.*

/**
 * 1. Rate Us on Google Play Dialog (shown on startup)
 */
@Composable
fun RateMeDialog(
    onRateClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedStars by remember { mutableIntStateOf(5) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Heart / Crown icon header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(RosePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Love Suiter",
                        tint = RosePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enjoying Suiter?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Help fellow singles find genuine connections! A quick 5-star review on Google Play helps our dating coach reach more people looking for love.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Interactive 5 stars
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedStars = i },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i stars",
                                tint = GoldAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (selectedStars == 5) "★★★★★ 5-Star Experience!" else "$selectedStars / 5 Stars",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aistudio.datingoptimizer.rvxqpm"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.aistudio.datingoptimizer.rvxqpm"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                        onRateClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rate_playstore_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rate 5 Stars on Google Play", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("rate_maybe_later_button")
                ) {
                    Text("Maybe Later", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 2. Personal Biography Modal
 * Asks for:
 * - Birth Year (Age Check: must be 18+)
 * - Race / Cultural Heritage / Identity
 * - Gender Identity
 * - Job / Profession
 * - Hobbies & Passions
 * - Social Fun & Weekend Vibe
 * - Favorite Food / Culinary Craving
 * - Dating Goals
 * Saves to Firebase & local repository
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalBiographyModal(
    initialBio: UserBiography,
    onSaveBiography: (UserBiography) -> Unit,
    onDismiss: () -> Unit
) {
    var birthYearText by remember { mutableStateOf(initialBio.birthYear?.toString() ?: "") }
    var raceIdentity by remember { mutableStateOf(initialBio.raceOrHeritage) }
    var genderIdentity by remember { mutableStateOf(initialBio.genderIdentity.ifBlank { "Man" }) }
    var jobTitle by remember { mutableStateOf(initialBio.jobTitle) }
    var hobbies by remember { mutableStateOf(initialBio.hobbies) }
    var socialFun by remember { mutableStateOf(initialBio.socialFun) }
    var favoriteFood by remember { mutableStateOf(initialBio.favoriteFood) }
    var datingGoals by remember { mutableStateOf(initialBio.datingGoals) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentYear = 2026
    val parsedYear = birthYearText.toIntOrNull()
    val calculatedAge = parsedYear?.let { currentYear - it }
    val isUnder18 = calculatedAge != null && calculatedAge < 18
    val isValidYear = parsedYear != null && parsedYear in 1924..currentYear

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Suiter Profile Dossier",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosePrimary
                        )
                        Text(
                            text = "Used by our seasoned dating coach to write authentic, magnetic bios and recommend your best dating site.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    // 1. AGE CHECK & BIRTH YEAR (18+ MANDATORY)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnder18) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUnder18) Icons.Default.Warning else Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (isUnder18) MaterialTheme.colorScheme.error else RosePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Age Verification (18+ Mandatory)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isUnder18) MaterialTheme.colorScheme.error else RosePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter your 4-digit birth year. Suiter strictly enforces an adults-only 18+ policy for all profiles.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = birthYearText,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        birthYearText = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("Birth Year (e.g. 1998)") },
                                placeholder = { Text("YYYY") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("birth_year_input"),
                                trailingIcon = {
                                    if (calculatedAge != null) {
                                        Text(
                                            text = if (isUnder18) "$calculatedAge yrs (Under 18)" else "Age $calculatedAge ✓",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnder18) MaterialTheme.colorScheme.error else GreenSuccess,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                    }
                                }
                            )

                            if (isUnder18) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⛔ You must be at least 18 years old to create a profile or use Suiter.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. RACE / CULTURAL HERITAGE / IDENTITY
                    Text("Race, Ethnicity & Identity", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Helps the coach craft authentic, tailored cultural references and dating angles:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    val raceOptions = listOf("Caucasian / White", "Black / African descent", "Hispanic / Latino", "Asian / Pacific Islander", "South Asian", "Middle Eastern", "Mixed / Multiracial")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column {
                            raceOptions.chunked(3).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    chunk.forEach { option ->
                                        FilterChip(
                                            selected = raceIdentity == option,
                                            onClick = { raceIdentity = if (raceIdentity == option) "" else option },
                                            label = { Text(option, fontSize = 10.sp) },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = raceIdentity,
                        onValueChange = { raceIdentity = it },
                        label = { Text("Identity / Heritage (or type custom)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("race_identity_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. JOB / CAREER / PROFESSION
                    Text("Job, Career & Ambition", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val jobSuggestions = listOf("Software Engineer", "Healthcare / Nurse", "Creative / Designer", "Finance / Business", "Chef / Culinary", "Student / Grad")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column {
                            jobSuggestions.chunked(3).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    chunk.forEach { sugg ->
                                        SuggestionChip(
                                            onClick = { jobTitle = sugg },
                                            label = { Text(sugg, fontSize = 10.sp) },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("What do you do for work?") },
                        placeholder = { Text("e.g. Senior Software Architect, ICU Nurse, Architect") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("job_title_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. HOBBIES & PASSIONS
                    Text("Hobbies & Passions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = hobbies,
                        onValueChange = { hobbies = it },
                        label = { Text("What energizes you in your free time?") },
                        placeholder = { Text("e.g. Rock climbing, vintage vinyl records, film photography, hiking") },
                        modifier = Modifier.fillMaxWidth().testTag("hobbies_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. SOCIAL FUN & WEEKEND VIBE
                    Text("Social Fun & Weekend Vibe", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = socialFun,
                        onValueChange = { socialFun = it },
                        label = { Text("How do you like to spend a night out?") },
                        placeholder = { Text("e.g. Speakeasy jazz bars, spontaneous road trips, hosting dinner parties, live concerts") },
                        modifier = Modifier.fillMaxWidth().testTag("social_fun_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. FAVORITE FOOD / CULINARY CRAVING
                    Text("Favorite Food & Culinary Craving", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = favoriteFood,
                        onValueChange = { favoriteFood = it },
                        label = { Text("What food are you constantly hunting down?") },
                        placeholder = { Text("e.g. Spicy handmade tonkotsu ramen, authentic street tacos, wood-fired sourdough pizza") },
                        modifier = Modifier.fillMaxWidth().testTag("favorite_food_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. DATING GOALS
                    Text("Dating Goals & Intentions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    val goalOptions = listOf("Long-term romance with wit", "Casual dating & great banter", "Exploring authentic connections")
                    goalOptions.forEach { goal ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datingGoals = goal }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = datingGoals == goal,
                                onClick = { datingGoals = goal }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(goal, fontSize = 12.sp)
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom Action Bar
                Button(
                    onClick = {
                        if (birthYearText.isBlank() || !isValidYear) {
                            errorMessage = "Please enter a valid 4-digit birth year."
                            return@Button
                        }
                        if (isUnder18) {
                            errorMessage = "You must be 18 or older to create a profile on Suiter."
                            return@Button
                        }
                        if (jobTitle.isBlank() && hobbies.isBlank() && favoriteFood.isBlank()) {
                            errorMessage = "Please fill in at least your job, a hobby, or favorite food so our coach can personalize your profile."
                            return@Button
                        }

                        onSaveBiography(
                            UserBiography(
                                birthYear = parsedYear,
                                age = calculatedAge,
                                raceOrHeritage = raceIdentity.trim(),
                                genderIdentity = genderIdentity.trim(),
                                jobTitle = jobTitle.trim(),
                                hobbies = hobbies.trim(),
                                socialFun = socialFun.trim(),
                                favoriteFood = favoriteFood.trim(),
                                datingGoals = datingGoals.trim(),
                                isCompleted = true,
                                savedToFirebase = true
                            )
                        )
                    },
                    enabled = !isUnder18,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_biography_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Dossier & Recommend Dating App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * 3. Coach Dating App Match Dialog
 * Recommends the highest-converting dating platform matching their profile dossier!
 */
@Composable
fun CoachDatingAppMatchDialog(
    recommendation: DatingAppRecommendation,
    onApplyPlatform: (DatingPlatform) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge
                Surface(
                    color = RosePrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "🎯 Coach Dating App Match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RosePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = recommendation.appName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RosePrimary
                )

                Text(
                    text = "${recommendation.matchScore} • ${recommendation.badge}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenSuccess
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = recommendation.coachHeadline,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Why This Fits Your Profile Dossier:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RosePrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        recommendation.whyItMatchesProfile.forEach { reason ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(reason, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Recommended Bio Strategy:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(recommendation.recommendedBioAngle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onApplyPlatform(recommendation.targetPlatform) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("apply_dating_app_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set ${recommendation.appName} as My Target App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${recommendation.playStorePackage}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${recommendation.playStorePackage}"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Get ${recommendation.appName} on Google Play", fontSize = 12.sp)
                }

                TextButton(onClick = onDismiss) {
                    Text("Continue to Profile Optimizer", fontSize = 12.sp)
                }
            }
        }
    }
}
