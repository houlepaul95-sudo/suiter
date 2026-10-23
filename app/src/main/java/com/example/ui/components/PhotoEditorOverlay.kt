package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PhotoEdits
import com.example.data.model.UserPhoto
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RosePrimary
import kotlin.math.roundToInt

/**
 * Full-featured Compose Canvas 'Photo Editor' Overlay that appears after a user selects a photo
 * for analysis. Uses native Android ColorMatrix image filters (brightness, contrast, skin smoothing,
 * golden warmth) and 2D vertex mesh perspective warping (drawBitmapMesh) for jawline manipulation.
 */
@Composable
fun PhotoEditorOverlay(
    photos: List<UserPhoto>,
    initialPhotoIndex: Int = 0,
    onUpdatePhotoEdits: (photoId: String, edits: PhotoEdits) -> Unit,
    onApplyEditsToAll: (edits: PhotoEdits) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveAndStartAnalysis: () -> Unit
) {
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    var selectedIndex by remember(initialPhotoIndex, photos.size) {
        mutableStateOf(initialPhotoIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0)))
    }
    val safeIndex = selectedIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0))
    val activePhoto = photos[safeIndex]
    val currentEdits = activePhoto.edits

    var selectedTab by remember { mutableStateOf(0) } // 0 = Skin & Tone Filters, 1 = Jawline & Perspective Mesh
    var showOriginal by remember { mutableStateOf(false) }
    var showMeshGrid by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("photo_editor_overlay"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(RosePrimary, CoralAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Canvas Photo Editor",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "ColorMatrix Skin Filters & 2D Mesh Jawline Warp",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_photo_editor_overlay_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Editor")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-photo Thumbnail Strip (if > 1 photo uploaded)
                if (photos.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(photos) { idx, item ->
                            val isSelected = idx == safeIndex
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedIndex = idx }
                            ) {
                                RetouchedPortraitImage(
                                    photo = item,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (item.edits.isEdited) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(GreenSuccess)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // =========================================================================
                // INTERACTIVE COMPOSE CANVAS VIEWPORT (WITH TOUCH DRAG JAW WARP + MESH GRID)
                // =========================================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(RosePrimary, CoralAccent)),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .pointerInput(activePhoto.id, currentEdits) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Dragging horizontally widens/slims the jawline; dragging vertically adjusts perspective warp
                                val deltaJaw = (dragAmount.x / 350f)
                                val deltaPersp = (dragAmount.y / 450f)
                                val updated = currentEdits.copy(
                                    jawWidth = (currentEdits.jawWidth + deltaJaw).coerceIn(-0.5f, 1f),
                                    perspectiveWarp = (currentEdits.perspectiveWarp + deltaPersp).coerceIn(-0.5f, 0.5f)
                                )
                                onUpdatePhotoEdits(activePhoto.id, updated)
                            }
                        }
                        .testTag("photo_editor_canvas_viewport")
                ) {
                    RetouchedPortraitImage(
                        photo = activePhoto,
                        showOriginal = showOriginal,
                        showMeshGrid = showMeshGrid && !showOriginal,
                        contentDescription = "Interactive Canvas Photo Editor Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top-left Live Canvas Status Pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (showOriginal) {
                                "ORIGINAL UNEDITED"
                            } else if (showMeshGrid) {
                                "2D MESH WARP GRID (DRAG TO SCULPT JAW)"
                            } else if (currentEdits.isEdited) {
                                "✨ CANVAS FILTER & MESH ACTIVE"
                            } else {
                                "DRAG CANVAS OR USE SLIDERS BELOW"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Top-right Controls: Mesh Grid Toggle & Before/After Compare
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (showMeshGrid) RosePrimary else Color.Black.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { showMeshGrid = !showMeshGrid }
                                .testTag("toggle_mesh_grid_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Toggle Warp Mesh",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showMeshGrid) "Hide Mesh" else "Warp Mesh",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (currentEdits.isEdited) {
                            Surface(
                                color = if (showOriginal) CoralAccent else Color.Black.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { showOriginal = !showOriginal }
                                    .testTag("overlay_compare_original_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Compare,
                                        contentDescription = "Compare Original",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showOriginal) "Edited" else "Original",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Active filter badges at bottom of Canvas
                    val badges = currentEdits.summaryBadges()
                    if (badges.isNotEmpty() && !showOriginal) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            badges.forEach { badge ->
                                Surface(
                                    color = RosePrimary.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = badge,
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

                Spacer(modifier = Modifier.height(10.dp))

                // Quick 1-Tap Presets Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetPill(
                        label = "✨ Smooth Skin + Brighten",
                        isActive = currentEdits.skinSoftening >= 0.6f && currentEdits.brightness > 0.05f,
                        testTag = "preset_smooth_skin_button",
                        onClick = {
                            showOriginal = false
                            onUpdatePhotoEdits(
                                activePhoto.id,
                                currentEdits.copy(
                                    skinSoftening = 0.72f,
                                    brightness = 0.12f,
                                    contrast = 0.08f,
                                    skinWarmth = 0.35f
                                )
                            )
                        }
                    )

                    PresetPill(
                        label = "🗿 Widen Jaw (Mesh Warp)",
                        isActive = currentEdits.jawWidth >= 0.5f,
                        testTag = "preset_widen_jaw_button",
                        onClick = {
                            showOriginal = false
                            showMeshGrid = true
                            selectedTab = 1
                            onUpdatePhotoEdits(
                                activePhoto.id,
                                currentEdits.copy(
                                    jawWidth = 0.65f,
                                    perspectiveWarp = 0.18f,
                                    jawContour = 0.50f
                                )
                            )
                        }
                    )

                    PresetPill(
                        label = "🔥 Full Studio Retouch",
                        isActive = currentEdits.skinSoftening >= 0.6f && currentEdits.jawWidth >= 0.5f,
                        testTag = "preset_full_studio_button",
                        onClick = {
                            showOriginal = false
                            onUpdatePhotoEdits(
                                activePhoto.id,
                                PhotoEdits(
                                    skinSoftening = 0.68f,
                                    jawWidth = 0.58f,
                                    skinWarmth = 0.42f,
                                    eyeClarity = 0.55f,
                                    jawContour = 0.48f,
                                    brightness = 0.10f,
                                    contrast = 0.12f,
                                    perspectiveWarp = 0.15f
                                )
                            )
                        }
                    )

                    PresetPill(
                        label = "↺ Reset",
                        isActive = false,
                        testTag = "preset_reset_button",
                        onClick = {
                            showOriginal = false
                            showMeshGrid = false
                            onUpdatePhotoEdits(activePhoto.id, PhotoEdits())
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Switcher Tabs: [Skin & Tone Filters] vs [Jawline & Perspective Warp]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf(
                        "Skin & Tone Filters" to Icons.Default.Tune,
                        "Jawline & Perspective Warp" to Icons.Default.GridOn
                    )
                    tabs.forEachIndexed { idx, (title, icon) ->
                        val active = selectedTab == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedTab = idx
                                    if (idx == 1) showMeshGrid = true
                                }
                                .testTag(if (idx == 0) "tab_skin_filters" else "tab_jaw_warp"),
                            color = if (active) RosePrimary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // =========================================================================
                    // TAB 0: COLORMATRIX SKIN SMOOTHING, BRIGHTNESS, CONTRAST & WARMTH
                    // =========================================================================
                    EditorFilterSlider(
                        title = "Skin Tone Smoothing (Bilateral Blur)",
                        subtitle = "Softens skin blemishes & harsh texture while preserving eye sharpness",
                        icon = Icons.Default.Spa,
                        value = currentEdits.skinSoftening,
                        valueRange = 0f..1f,
                        displayValue = "${(currentEdits.skinSoftening * 100).roundToInt()}%",
                        testTag = "overlay_slider_skin_softening",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(skinSoftening = v))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val bPct = (currentEdits.brightness * 100).roundToInt()
                    EditorFilterSlider(
                        title = "Brightness Adjustment (ColorMatrix)",
                        subtitle = "Lifts facial exposure & brightens dark indoor shadows",
                        icon = Icons.Default.LightMode,
                        value = currentEdits.brightness,
                        valueRange = -0.5f..0.5f,
                        displayValue = if (bPct > 0) "+$bPct%" else "$bPct%",
                        testTag = "overlay_slider_brightness",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(brightness = v))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val cPct = (currentEdits.contrast * 100).roundToInt()
                    EditorFilterSlider(
                        title = "Contrast & Pop Filter (ColorMatrix)",
                        subtitle = "Balances dynamic range for crisp dating card visual pop",
                        icon = Icons.Default.Contrast,
                        value = currentEdits.contrast,
                        valueRange = -0.5f..0.5f,
                        displayValue = if (cPct > 0) "+$cPct%" else "$cPct%",
                        testTag = "overlay_slider_contrast",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(contrast = v))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EditorFilterSlider(
                        title = "Golden Hour Skin Warmth",
                        subtitle = "Shifts RGB channels toward warm sun-kissed amber skin tones",
                        icon = Icons.Default.WbSunny,
                        value = currentEdits.skinWarmth,
                        valueRange = 0f..1f,
                        displayValue = "+${(currentEdits.skinWarmth * 100).roundToInt()}%",
                        testTag = "overlay_slider_warmth",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(skinWarmth = v))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EditorFilterSlider(
                        title = "Eye Clarity & Catchlight",
                        subtitle = "Enhances upper-face eye illumination and sharpens gaze",
                        icon = Icons.Default.Visibility,
                        value = currentEdits.eyeClarity,
                        valueRange = 0f..1f,
                        displayValue = "${(currentEdits.eyeClarity * 100).roundToInt()}%",
                        testTag = "overlay_slider_eye_clarity",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(eyeClarity = v))
                        }
                    )
                } else {
                    // =========================================================================
                    // TAB 1: 2D VERTEX MESH JAWLINE & PERSPECTIVE WARP
                    // =========================================================================
                    val jPct = (currentEdits.jawWidth * 100).roundToInt()
                    EditorFilterSlider(
                        title = "Jawline Width Warp (2D Bitmap Mesh)",
                        subtitle = "Warps lower-face mesh vertices outward to widen jaw or inward to slim",
                        icon = Icons.Default.OpenWith,
                        value = currentEdits.jawWidth,
                        valueRange = -0.5f..1f,
                        displayValue = if (jPct > 0) "+$jPct% Wide" else if (jPct < 0) "$jPct% Slim" else "0%",
                        testTag = "overlay_slider_jaw_width",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(jawWidth = v))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val pPct = (currentEdits.perspectiveWarp * 100).roundToInt()
                    EditorFilterSlider(
                        title = "Perspective & Chin Angle Warp",
                        subtitle = "Keystone perspective warp to correct selfie camera lens distortion",
                        icon = Icons.Default.CropFree,
                        value = currentEdits.perspectiveWarp,
                        valueRange = -0.5f..0.5f,
                        displayValue = if (pPct > 0) "+$pPct%" else "$pPct%",
                        testTag = "overlay_slider_perspective_warp",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(perspectiveWarp = v))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    EditorFilterSlider(
                        title = "Jawline & Cheekbone Shadow Contour",
                        subtitle = "Adds subtle peripheral shadow sculpting along the lower jaw",
                        icon = Icons.Default.FaceRetouchingNatural,
                        value = currentEdits.jawContour,
                        valueRange = 0f..1f,
                        displayValue = "${(currentEdits.jawContour * 100).roundToInt()}%",
                        testTag = "overlay_slider_jaw_contour",
                        onValueChange = { v ->
                            showOriginal = false
                            onUpdatePhotoEdits(activePhoto.id, currentEdits.copy(jawContour = v))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons: Save & Return OR Save & Start Analysis Immediately
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_and_close_editor_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Keep & Add More", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSaveAndStartAnalysis,
                        modifier = Modifier
                            .weight(1.35f)
                            .height(50.dp)
                            .testTag("apply_and_analyze_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RosePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply & Run AI Audit", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetPill(
    label: String,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (isActive) RosePrimary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun EditorFilterSlider(
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
            .padding(horizontal = 12.dp, vertical = 7.dp)
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
                .height(30.dp)
                .testTag(testTag)
        )
    }
}
