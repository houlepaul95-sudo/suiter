package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.DatingPlatform
import com.example.data.model.UserBiography
import com.example.data.model.UserPhoto
import com.example.ui.components.AiLabelBadge
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RosePrimary

data class TestPhotoItem(
    val url: String,
    val title: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploadScreen(
    platform: DatingPlatform,
    photos: List<UserPhoto>,
    bio: String,
    biography: UserBiography = UserBiography(),
    onPhotosSelected: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onStartAnalysis: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCompatibleApps: () -> Unit = {},
    onOpenBiography: () -> Unit = {}
) {
    var showSourceDialog by remember { mutableStateOf(false) }
    var showLibraryDialog by remember { mutableStateOf(false) }
    var showUrlInputDialog by remember { mutableStateOf(false) }
    var customUrlText by remember { mutableStateOf("") }

    // Curated high quality diverse test profile photos for emulator testing
    val curatedTestPhotos = remember {
        listOf(
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80",
                title = "Warm Portrait",
                category = "Headshot"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&auto=format&fit=crop&q=80",
                title = "Approach Smile",
                category = "Lead Candidate"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop&q=80",
                title = "Cafe Lifestyle",
                category = "Full-Body"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&auto=format&fit=crop&q=80",
                title = "Outdoor Natural",
                category = "Activity"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=600&auto=format&fit=crop&q=80",
                title = "Sunset Candid",
                category = "Lifestyle"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=600&auto=format&fit=crop&q=80",
                title = "Casual Jacket",
                category = "Social"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=600&auto=format&fit=crop&q=80",
                title = "City Explorer",
                category = "Active"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=600&auto=format&fit=crop&q=80",
                title = "Golden Hour",
                category = "Portrait"
            ),
            TestPhotoItem(
                url = "https://images.unsplash.com/photo-1488161628813-04466f872be2?w=600&auto=format&fit=crop&q=80",
                title = "Beach & Travel",
                category = "Vacation"
            )
        )
    }

    // Android Photo Picker Launcher (zero-permission scoped media access)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onPhotosSelected(uris.map { it.toString() })
        }
    }

    // Local Document / File Picker Launcher for selecting a single local dating profile photo
    val localFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotosSelected(listOf(uri.toString()))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.clickable { onOpenCompatibleApps() }) {
                        Text("Suiter Audit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Target: ${platform.displayName}",
                                fontSize = 12.sp,
                                color = RosePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch app",
                                tint = RosePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenBiography, modifier = Modifier.testTag("dossier_profile_button")) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = "Profile Dossier", tint = RosePrimary)
                    }
                    IconButton(onClick = onOpenCompatibleApps, modifier = Modifier.testTag("compatible_apps_upload_button")) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = "Find Compatible Apps")
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("settings_button")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings & Privacy")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Emulator Friendly Notice & 1-Tap Load Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Emulator Test Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RosePrimary
                            )
                        }

                        Button(
                            onClick = {
                                val full6Set = curatedTestPhotos.take(6).map { it.url }
                                onPhotosSelected(full6Set)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("quick_load_6_photos_button")
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1-Tap 6 Photos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Testing on an emulator? Tap '1-Tap 6 Photos' or open the Test Gallery to select realistic dating profile photos instantly.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upload Profile Photos (${photos.size}/6)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                AiLabelBadge(label = "Gemini Vision Audit")
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Upload a dating profile photo from your device or add up to 6 photos for full analysis & pre-submit retouching.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated Primary Local File Picker Button for Uploading a Dating Profile Photo for Analysis
            Button(
                onClick = {
                    try {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } catch (_: Exception) {
                        localFilePickerLauncher.launch("image/*")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("upload_local_photo_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RosePrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Upload Local Photo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload Local Photo for Analysis",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo Grid (2x3)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(6) { index ->
                    if (index < photos.size) {
                        val photo = photos[index]
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.85f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, if (index == 0) RosePrimary else Color.Transparent, RoundedCornerShape(12.dp))
                        ) {
                            com.example.ui.components.RetouchedPortraitImage(
                                photo = photo,
                                contentDescription = "Uploaded photo ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Lead indicator
                            if (index == 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RosePrimary)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("Current #1", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Retouched badge
                            if (photo.edits.isEdited) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GreenSuccess.copy(alpha = 0.9f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("✨ Edited", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Remove button
                            IconButton(
                                onClick = { onPhotoRemoved(photo.id) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(26.dp)
                                    .padding(2.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        // Empty slot
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.85f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    try {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } catch (_: Exception) {
                                        showSourceDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add Photo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Action Row: Local File Browser, Source Options Dialog, Test Gallery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { localFilePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("browse_local_file_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Local Files", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showSourceDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("add_photos_main_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sources", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showLibraryDialog = true },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("open_test_gallery_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Gallery", fontSize = 12.sp)
                }
            }

            // Low Photo Path Graceful Guidance (Play Compliance Map)
            if (photos.isNotEmpty() && photos.size < 4) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You have ${photos.size} photo${if (photos.size == 1) "" else "s"}. We will analyze what you provided! Adding 4+ photos unlocks full sequence and diversity recommendations.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Bio Section
            Text(
                text = "Current Dating Bio or Prompts (Optional)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paste your current bio or prompt to receive AI-powered rewrites and high-converting hooks.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = onBioChanged,
                placeholder = {
                    Text(
                        "e.g. Passionate about coffee, weekend hikes, and obscure vinyl records. Ambitious by day, amateur pasta maker by night...",
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bio_input_field"),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Run Analysis Button
            Button(
                onClick = onStartAnalysis,
                enabled = photos.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_analysis_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (photos.isEmpty()) "Add photos to audit" else "Audit My Profile (${photos.size} photo${if (photos.size == 1) "" else "s"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal 1: Select Photo Source Dialog
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Add Photos", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Choose how you want to add photos for audit and testing:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Option A: Test Photo Library (Best for emulator)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourceDialog = false
                                showLibraryDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Collections, contentDescription = null, tint = RosePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Test Photo Library (Emulator)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Select from realistic sample dating portraits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option B: 1-Tap Load Full 6-Photo Set
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val full6 = curatedTestPhotos.take(6).map { it.url }
                                onPhotosSelected(full6)
                                showSourceDialog = false
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = CoralAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Quick Load 6 Realistic Photos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Instant set: Portrait, Lifestyle, Full Body, Action", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option C: Android Photo Picker
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourceDialog = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Android Gallery Picker", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Pick image files stored on the device", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option D: Custom Image URL
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourceDialog = false
                                showUrlInputDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Enter Custom Image URL", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Paste any web image URL directly", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal 2: Curated Test Photo Library Grid
    if (showLibraryDialog) {
        Dialog(onDismissRequest = { showLibraryDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Test Photo Library", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Tap photos to add (${photos.size}/6 selected)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showLibraryDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(curatedTestPhotos) { item ->
                            val isAlreadyAdded = photos.any { it.uriString == item.url }
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        2.dp,
                                        if (isAlreadyAdded) RosePrimary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        if (isAlreadyAdded) {
                                            val photo = photos.find { it.uriString == item.url }
                                            photo?.let { onPhotoRemoved(it.id) }
                                        } else if (photos.size < 6) {
                                            onPhotosSelected(listOf(item.url))
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = item.url,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (isAlreadyAdded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(RosePrimary.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val allUrls = curatedTestPhotos.take(6).map { it.url }
                                onPhotosSelected(allUrls)
                                showLibraryDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Select All 6", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showLibraryDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done (${photos.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal 3: Custom URL Input
    if (showUrlInputDialog) {
        AlertDialog(
            onDismissRequest = { showUrlInputDialog = false },
            title = { Text("Add Photo by URL", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Paste a direct link to an image (JPEG, PNG, WebP):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customUrlText,
                        onValueChange = { customUrlText = it },
                        placeholder = { Text("https://example.com/photo.jpg", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customUrlText.isNotBlank()) {
                            onPhotosSelected(listOf(customUrlText.trim()))
                            customUrlText = ""
                            showUrlInputDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
