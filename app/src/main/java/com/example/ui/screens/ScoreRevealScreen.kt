package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PhotoAudit
import com.example.data.model.ProfileAuditResult
import com.example.data.model.UserPhoto
import com.example.ui.components.AiLabelBadge
import com.example.ui.components.ReportResultButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreRevealScreen(
    audit: ProfileAuditResult,
    photos: List<UserPhoto>,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
    onOpenBioRewriter: () -> Unit,
    onOpenCopilot: () -> Unit,
    onOpenShareCard: () -> Unit,
    onOpenCompatibleApps: () -> Unit = {},
    onReportSubmitted: (targetType: String, snippet: String, category: String) -> Unit,
    onRetestProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Audit Results", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = onOpenCompatibleApps, modifier = Modifier.testTag("compatible_apps_top_button")) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = "Compatible apps")
                    }
                    IconButton(onClick = onOpenShareCard, modifier = Modifier.testTag("share_card_top_button")) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share card")
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // AI Transparency & Compliance Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiLabelBadge(label = "AI-Generated Profile Audit")
                ReportResultButton(
                    targetType = "Profile Audit",
                    contentSnippet = "Score ${audit.overallScore}/100: ${audit.topFix}",
                    onReportSubmitted = onReportSubmitted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(RosePrimary, CoralAccent, GoldAccent, RosePrimary)
                                )
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${audit.overallScore}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp,
                                    color = RosePrimary
                                )
                                Text(
                                    text = "out of 100",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = audit.summaryTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sub-score Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScoreStatItem("Photo Clarity", "${audit.photoQualityScore}%")
                        ScoreStatItem("Diversity", "${audit.diversityScore}%")
                        ScoreStatItem("Bio Hook", "${audit.bioHookScore}%")
                        ScoreStatItem("First Imp.", "${audit.firstImpressionScore}%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dating Coach Honest Assessment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dating Coach Strategic Diagnosis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Top Strength: ${audit.topStrength}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Conversion Leak: ${audit.topFix}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = RosePrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Free Instant Fix Insight (Compliance Requirement: 1 free insight immediately provided)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(RosePrimary, CoralAccent)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = RosePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Free Quick Fix (High Impact)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RosePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = audit.freeInsight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Optimal Photo Order Sequence
            Text(
                text = "Recommended Photo Sequence",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tested order to maximize match conversion and hold viewer attention:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(photos) { index, photo ->
                        val auditDetail = audit.detailedPhotoAudits.getOrNull(index)
                        val isLead = index == audit.leadPhotoIndex

                        Card(
                            modifier = Modifier
                                .width(130.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                                    AsyncImage(
                                        model = photo.uriString,
                                        contentDescription = "Photo #$index",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isLead) RosePrimary else Color.Black.copy(alpha = 0.7f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isLead) "Lead Pick #1" else "Slot #${index + 1}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = auditDetail?.ratingScore?.let { "$it/100" } ?: "75/100",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = RosePrimary
                                    )
                                    Text(
                                        text = auditDetail?.strengths?.firstOrNull() ?: "Good visual presence",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Premium In-Depth Audit Teaser or Unlocked details
            if (!isPremium) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPaywall() }
                        .testTag("paywall_teaser_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock 4 Deeper Red-Flag Fixes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Get exact crop coordinates, facial symmetry feedback, bio rewrite generator, and dating conversation copilot.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenPaywall,
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("unlock_paywall_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("See Premium Plans (From $1.15/wk)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Suiter Pro Active", fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("All 6-slot photo red-flag fixes, bio styles, and dating copilot date-closers are fully unlocked.", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Photo Red-Flag Diagnosis (Pro feature)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("All-Slot Red-Flag Diagnoses (Pro Unlocked)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        audit.detailedPhotoAudits.forEach { detail ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(detail.label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RosePrimary)
                                        Text("${detail.ratingScore}/100", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (detail.ratingScore < 70) CoralAccent else RosePrimary)
                                    }
                                    if (detail.honestCritique.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(6.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = detail.honestCritique,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                    if (detail.flags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        detail.flags.forEach { flag ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Honest Fix: $flag", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(detail.actionTip, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Compatible Dating Apps Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCompatibleApps() }
                    .testTag("compatible_apps_reveal_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RosePrimary.copy(alpha = 0.08f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RosePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compatible Dating Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RosePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Find high-converting apps (Grindr, Scruff, HER, Bumble, Hinge) tailored to your preferences.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open apps",
                        tint = RosePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Buttons to Bio Rewriter & Dating Copilot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenBioRewriter,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("open_bio_rewriter_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bio Rewriter", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenCopilot,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("open_copilot_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dating Copilot", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(onClick = onRetestProfile) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Re-test with New Photos", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScoreStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
