package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BioRewrite
import com.example.data.model.DatingPlatform
import com.example.data.model.HingePromptResult
import com.example.ui.components.AiLabelBadge
import com.example.ui.components.ReportResultButton
import com.example.ui.theme.RosePrimary

import com.example.data.model.UserBiography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioRewriterScreen(
    platform: DatingPlatform,
    currentBio: String,
    rewrites: List<BioRewrite>,
    hingePrompts: List<HingePromptResult>,
    biography: UserBiography = UserBiography(),
    isPremium: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    onGenerateRewrites: () -> Unit,
    onGeneratePrompts: () -> Unit,
    onCopyOutput: (text: String, title: String) -> Unit = { _, _ -> },
    onEditBiography: () -> Unit = {},
    onBack: () -> Unit,
    onReportSubmitted: (targetType: String, snippet: String, category: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Bios, 1: Hinge Prompts

    LaunchedEffect(Unit) {
        if (rewrites.isEmpty()) {
            onGenerateRewrites()
        }
        if (hingePrompts.isEmpty()) {
            onGeneratePrompts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Bio & Prompt Rewriter", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiLabelBadge(label = "Gemini Bio Optimizer")
                Text(
                    text = platform.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RosePrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Dossier Integration Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditBiography() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            val bioDetails = listOfNotNull(
                                biography.datingVibe.takeIf { it.isNotBlank() },
                                biography.jobTitle.takeIf { it.isNotBlank() },
                                biography.favoriteFood.takeIf { it.isNotBlank() }
                            ).joinToString(" • ")

                            Text(
                                text = if (bioDetails.isNotBlank()) "Dossier: $bioDetails" else "Complete Profile Dossier to Personalize",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "Tuned for ${platform.displayName} • Vibe: ${biography.datingVibe} • Tap to edit",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Dossier", tint = RosePrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Profile Bios", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Prompt Answers", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dating Coach Insight Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.TipsAndUpdates, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coach Directive: Generic bios get swiped away in 0.5 seconds. These rewrites weave your real job, favorite foods, and weekend banter into high-converting date invitations.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Bio Rewrites
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bio Variations (Tailored to You)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Regenerates seasoned coach variations", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onGenerateRewrites,
                        modifier = Modifier.height(36.dp).testTag("regenerate_bios_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate Variations", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (rewrites.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RosePrimary)
                    }
                } else {
                    rewrites.forEachIndexed { index, item ->
                        val isLocked = !isPremium && index > 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SuggestionBadge(item.tone)
                                    if (!isLocked) {
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Bio Rewrite", item.bioText)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Copied bio to clipboard!", Toast.LENGTH_SHORT).show()
                                                onCopyOutput(item.bioText, "Bio Rewrite: ${item.tone}")
                                            },
                                            modifier = Modifier.size(32.dp).testTag("copy_bio_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy bio", modifier = Modifier.size(18.dp))
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Pro Tier", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RosePrimary)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (!isLocked) {
                                    Text(
                                        text = item.bioText,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Normal
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = item.whyItWorks, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ReportResultButton(
                                        targetType = "Bio Rewrite",
                                        contentSnippet = item.bioText,
                                        onReportSubmitted = onReportSubmitted
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Unlock ${item.tone} style with Suiter Pro",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = onOpenPaywall,
                                            modifier = Modifier.fillMaxWidth().height(38.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Unlock All Styles with Pro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Hinge Prompts
                if (hingePrompts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RosePrimary)
                    }
                } else {
                    hingePrompts.forEachIndexed { index, prompt ->
                        val isLocked = !isPremium && index > 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prompt.promptQuestion,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RosePrimary
                                    )
                                    if (!isLocked) {
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Hinge Prompt", "${prompt.promptQuestion}\n${prompt.promptAnswer}")
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Copied prompt answer!", Toast.LENGTH_SHORT).show()
                                                onCopyOutput("${prompt.promptQuestion}\n${prompt.promptAnswer}", "Hinge Prompt: ${prompt.promptQuestion}")
                                            },
                                            modifier = Modifier.size(32.dp).testTag("copy_prompt_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy prompt", modifier = Modifier.size(18.dp))
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Pro Tier", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RosePrimary)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (!isLocked) {
                                    Text(
                                        text = prompt.promptAnswer,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ReportResultButton(
                                        targetType = "Hinge Prompt",
                                        contentSnippet = "${prompt.promptQuestion}: ${prompt.promptAnswer}",
                                        onReportSubmitted = onReportSubmitted
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Category: ${prompt.category} (Pro Strategy)",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedButton(
                                            onClick = onOpenPaywall,
                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Unlock All Prompts with Pro", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SuggestionBadge(text: String) {
    Surface(
        color = RosePrimary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = RosePrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
