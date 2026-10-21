package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhotoEdits
import com.example.data.model.UserPhoto
import com.example.ui.components.AiLabelBadge
import com.example.ui.components.RetouchedPortraitImage
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RosePrimary
import kotlin.math.roundToInt

@Composable
fun AnalyzingScreen(
    currentStepText: String,
    photos: List<UserPhoto> = emptyList(),
    isAnalyzing: Boolean = true,
    onUpdatePhotoEdits: (photoId: String, edits: PhotoEdits) -> Unit = { _, _ -> },
    onApplyEditsToAll: (edits: PhotoEdits) -> Unit = {},
    onSubmitToProfile: () -> Unit = {},
    onBackToUpload: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var showPhotoEditor by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableStateOf(0) }
    var showOriginalComparison by remember { mutableStateOf(false) }

    val safeIndex = if (photos.isEmpty()) 0 else selectedPhotoIndex.coerceIn(0, photos.lastIndex)
    val activePhoto = photos.getOrNull(safeIndex)
    val currentEdits = activePhoto?.edits ?: PhotoEdits()
    val editedCount = photos.count { it.edits.isEdited }

    val datingTips = listOf(
        "Tip: Softening harsh indoor skin shadows & widening jawline posture increases right-swipe rate by up to 34%.",
        "Tip: Clear eye-contact and warm natural lighting in Photo #1 trigger instant subconscious trust on Hinge & Tinder.",
        "Tip: Including at least one authentic full-body lifestyle photo eliminates ambiguity and boosts reply rates by 42%."
    )

    var currentTipIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2800)
            currentTipIndex = (currentTipIndex + 1) % datingTips.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row with Back option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackToUpload,
                modifier = Modifier.testTag("loading_back_to_upload_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Upload",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload Screen", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            AiLabelBadge(label = "Gemini Vision & Retouch Studio")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated Pulse Radar + Status Header
        Box(
            modifier = Modifier
                .size(if (showPhotoEditor) 76.dp else 104.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            RosePrimary.copy(alpha = 0.35f),
                            CoralAccent.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (showPhotoEditor) 52.dp else 68.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(RosePrimary, CoralAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAnalyzing) Icons.Default.AutoAwesome else Icons.Default.Verified,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (showPhotoEditor) 26.dp else 34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isAnalyzing) "Auditing Dating Profile..." else "Audit Ready • Pre-Submit Review",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    color = RosePrimary,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = currentStepText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // PRE-SUBMIT PHOTO RETOUCH & SCULPT STUDIO CARD (IN LOADING SCREEN)
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(RosePrimary, CoralAccent, GoldAccent))
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RosePrimary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FaceRetouchingNatural,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit Pictures Before Submitting",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Soften skin tone, widen jaw, add warm glow & eye clarity",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (editedCount > 0) {
                        Surface(
                            color = GreenSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$editedCount Edited",
                                color = GreenSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Toggle Button to Open/Close Photo Retouch Editor in Loading Screen
                Button(
                    onClick = { showPhotoEditor = !showPhotoEditor },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("edit_pictures_loading_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showPhotoEditor) MaterialTheme.colorScheme.secondaryContainer else RosePrimary,
                        contentColor = if (showPhotoEditor) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (showPhotoEditor) Icons.Default.ExpandLess else Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showPhotoEditor) "Hide Photo Retouch Controls" else "Edit & Retouch Pictures (Skin Tone, Jaw & More)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick 1-Tap Preset Chips (always visible on the loading screen for instant 1-tap editing!)
                Text(
                    text = "Quick 1-Tap Enhancements:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickRetouchChip(
                        label = "Soften Skin Tone",
                        icon = Icons.Default.Spa,
                        isSelected = currentEdits.skinSoftening >= 0.5f,
                        testTag = "quick_soften_skin_button",
                        onClick = {
                            showPhotoEditor = true
                            activePhoto?.let { photo ->
                                val newSoft = if (currentEdits.skinSoftening >= 0.5f) 0f else 0.72f
                                onUpdatePhotoEdits(photo.id, currentEdits.copy(skinSoftening = newSoft))
                            }
                        }
                    )

                    QuickRetouchChip(
                        label = "Widen Jaw",
                        icon = Icons.Default.OpenWith,
                        isSelected = currentEdits.jawWidth >= 0.35f,
                        testTag = "quick_widen_jaw_button",
                        onClick = {
                            showPhotoEditor = true
                            activePhoto?.let { photo ->
                                val newJaw = if (currentEdits.jawWidth >= 0.35f) 0f else 0.65f
                                onUpdatePhotoEdits(photo.id, currentEdits.copy(jawWidth = newJaw))
                            }
                        }
                    )

                    QuickRetouchChip(
                        label = "Golden Skin Glow",
                        icon = Icons.Default.WbSunny,
                        isSelected = currentEdits.skinWarmth >= 0.4f,
                        testTag = "quick_warm_skin_button",
                        onClick = {
                            showPhotoEditor = true
                            activePhoto?.let { photo ->
                                val newWarmth = if (currentEdits.skinWarmth >= 0.4f) 0f else 0.55f
                                onUpdatePhotoEdits(photo.id, currentEdits.copy(skinWarmth = newWarmth))
                            }
                        }
                    )

                    QuickRetouchChip(
                        label = "Eye Clarity + Jaw Sculpt",
                        icon = Icons.Default.AutoFixHigh,
                        isSelected = currentEdits.eyeClarity >= 0.4f && currentEdits.jawContour >= 0.4f,
                        testTag = "quick_full_sculpt_button",
                        onClick = {
                            showPhotoEditor = true
                            activePhoto?.let { photo ->
                                onUpdatePhotoEdits(
                                    photo.id,
                                    PhotoEdits(
                                        skinSoftening = 0.68f,
                                        jawWidth = 0.60f,
                                        skinWarmth = 0.45f,
                                        eyeClarity = 0.60f,
                                        jawContour = 0.55f
                                    )
                                )
                            }
                        }
                    )
                }

                // Expandable Full Retouch Studio inside Loading Screen
                AnimatedVisibility(
                    visible = showPhotoEditor,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (photos.isEmpty()) {
                            Text(
                                text = "No photos uploaded yet. Go back to upload a photo first.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Photo Selector Carousel (if multiple photos)
                            if (photos.size > 1) {
                                Text(
                                    text = "Select Photo to Edit (${safeIndex + 1} of ${photos.size}):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(photos) { idx, itemPhoto ->
                                        val isSelected = idx == safeIndex
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedPhotoIndex = idx }
                                        ) {
                                            RetouchedPortraitImage(
                                                photo = itemPhoto,
                                                contentDescription = "Thumbnail ${idx + 1}",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            if (itemPhoto.edits.isEdited) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(3.dp)
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(GreenSuccess),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Live Before/After Portrait Preview Box
                            activePhoto?.let { photo ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            1.5.dp,
                                            if (currentEdits.isEdited && !showOriginalComparison) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    RetouchedPortraitImage(
                                        photo = photo,
                                        showOriginal = showOriginalComparison,
                                        contentDescription = "Live Retouch Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Top-left status badge
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.72f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = if (showOriginalComparison) {
                                                "ORIGINAL (UNEDITED)"
                                            } else if (currentEdits.isEdited) {
                                                "✨ RETOUCHED PREVIEW"
                                            } else {
                                                "PHOTO #${safeIndex + 1} PREVIEW"
                                            },
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Top-right Compare Original Toggle Button
                                    if (currentEdits.isEdited) {
                                        Surface(
                                            color = if (showOriginalComparison) RosePrimary else Color.Black.copy(alpha = 0.72f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                                .clickable { showOriginalComparison = !showOriginalComparison }
                                                .testTag("compare_original_toggle")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Compare,
                                                    contentDescription = "Compare Before & After",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (showOriginalComparison) "Show Edited" else "Compare Original",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Active modification pills at bottom of preview
                                    val activeBadges = currentEdits.summaryBadges()
                                    if (activeBadges.isNotEmpty() && !showOriginalComparison) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                                    )
                                                )
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            activeBadges.forEach { badgeText ->
                                                Surface(
                                                    color = RosePrimary.copy(alpha = 0.9f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = badgeText,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // =================================================================
                                // SLIDER 1: SOFTEN SKIN TONE
                                // =================================================================
                                RetouchSliderRow(
                                    title = "Soften Skin Tone & Blemishes",
                                    subtitle = "Smooths pores, softens harsh shadows & evens complexion",
                                    icon = Icons.Default.Spa,
                                    value = currentEdits.skinSoftening,
                                    valueRange = 0f..1f,
                                    displayValue = "${(currentEdits.skinSoftening * 100).roundToInt()}%",
                                    testTag = "slider_soften_skin",
                                    onValueChange = { newVal ->
                                        showOriginalComparison = false
                                        onUpdatePhotoEdits(photo.id, currentEdits.copy(skinSoftening = newVal))
                                    }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // =================================================================
                                // SLIDER 2: WIDEN JAWLINE
                                // =================================================================
                                val jawPct = (currentEdits.jawWidth * 100).roundToInt()
                                RetouchSliderRow(
                                    title = "Widen Jaw & Lower-Face Structure",
                                    subtitle = "Broadens jawline width or sculpts lower facial proportions",
                                    icon = Icons.Default.OpenWith,
                                    value = currentEdits.jawWidth,
                                    valueRange = -0.5f..1f,
                                    displayValue = if (jawPct > 0) "+$jawPct% Wide" else if (jawPct < 0) "$jawPct% Slim" else "0% Neutral",
                                    testTag = "slider_widen_jaw",
                                    onValueChange = { newVal ->
                                        showOriginalComparison = false
                                        onUpdatePhotoEdits(photo.id, currentEdits.copy(jawWidth = newVal))
                                    }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // =================================================================
                                // SLIDER 3: WARM SKIN TONE (GOLDEN GLOW)
                                // =================================================================
                                RetouchSliderRow(
                                    title = "Warm Skin Tone (Golden Hour Glow)",
                                    subtitle = "Adds healthy sun-kissed bronze warmth to pale indoor lighting",
                                    icon = Icons.Default.WbSunny,
                                    value = currentEdits.skinWarmth,
                                    valueRange = 0f..1f,
                                    displayValue = "+${(currentEdits.skinWarmth * 100).roundToInt()}%",
                                    testTag = "slider_warm_skin",
                                    onValueChange = { newVal ->
                                        showOriginalComparison = false
                                        onUpdatePhotoEdits(photo.id, currentEdits.copy(skinWarmth = newVal))
                                    }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // =================================================================
                                // SLIDER 4: EYE CLARITY & CATCHLIGHT
                                // =================================================================
                                RetouchSliderRow(
                                    title = "Eye Clarity & Gaze Pop",
                                    subtitle = "Sharpens eye contact & brightens upper-face catchlights",
                                    icon = Icons.Default.Visibility,
                                    value = currentEdits.eyeClarity,
                                    valueRange = 0f..1f,
                                    displayValue = "${(currentEdits.eyeClarity * 100).roundToInt()}%",
                                    testTag = "slider_eye_clarity",
                                    onValueChange = { newVal ->
                                        showOriginalComparison = false
                                        onUpdatePhotoEdits(photo.id, currentEdits.copy(eyeClarity = newVal))
                                    }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // =================================================================
                                // SLIDER 5: JAW & CHEEKBONE CONTOUR
                                // =================================================================
                                RetouchSliderRow(
                                    title = "Cheekbone & Jawline Contour",
                                    subtitle = "Enhances facial bone definition & studio depth",
                                    icon = Icons.Default.Contrast,
                                    value = currentEdits.jawContour,
                                    valueRange = 0f..1f,
                                    displayValue = "${(currentEdits.jawContour * 100).roundToInt()}%",
                                    testTag = "slider_jaw_contour",
                                    onValueChange = { newVal ->
                                        showOriginalComparison = false
                                        onUpdatePhotoEdits(photo.id, currentEdits.copy(jawContour = newVal))
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Row: Reset Photo or Apply to All Photos
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            showOriginalComparison = false
                                            onUpdatePhotoEdits(photo.id, PhotoEdits())
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .testTag("reset_photo_edits_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset Photo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    if (photos.size > 1) {
                                        FilledTonalButton(
                                            onClick = {
                                                onApplyEditsToAll(currentEdits)
                                            },
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(40.dp)
                                                .testTag("apply_edits_all_photos_button"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Apply to All ${photos.size} Photos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // PRIMARY SUBMIT TO PROFILE CTA BUTTON
        // =========================================================================
        Button(
            onClick = onSubmitToProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_to_profile_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RosePrimary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = if (editedCount > 0) Icons.Default.AutoAwesome else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (editedCount > 0) {
                    "Submit Edited Pictures to Profile ($editedCount Retouched)"
                } else {
                    "Submit to My Profile & View Score"
                },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Educational Dating Tip Card (Teaches while user reviews/edits)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = CoralAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profile Science Insight",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CoralAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = datingTips[currentTipIndex],
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun QuickRetouchChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else RosePrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RetouchSliderRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RosePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = displayValue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = RosePrimary
            )
        }
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = RosePrimary,
                activeTrackColor = RosePrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .testTag(testTag)
        )
    }
}
