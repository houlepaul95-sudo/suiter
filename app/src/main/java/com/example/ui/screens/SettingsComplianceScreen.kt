package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportLog
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedWarning
import com.example.ui.theme.RosePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComplianceScreen(
    reports: List<ReportLog>,
    isPremium: Boolean,
    onTogglePremium: (Boolean) -> Unit,
    onDeletePhotos: () -> Unit,
    onDeleteAccountAndData: () -> Unit,
    onOpenCompatibleApps: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeletePhotosDialog by remember { mutableStateOf(false) }
    var viewingSection by remember { mutableStateOf<String?>(null) } // "PRIVACY", "TERMS", "COMPLIANCE_MAP", "CHECKLIST", null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Compliance", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            Spacer(modifier = Modifier.height(8.dp))

            // RevenueCat Customer Center & Subscription Hub
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Customer Center", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Surface(
                            color = if (isPremium) GreenSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isPremium) "PRO ACTIVE" else "FREE TIER",
                                color = if (isPremium) GreenSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusMsg by com.example.data.service.RevenueCatService.statusMessage.collectAsState()
                    val isLiveSdk by com.example.data.service.RevenueCatService.isLiveSdkActive.collectAsState()
                    val currentTier by com.example.data.service.RevenueCatService.currentTier.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Tier:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentTier.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RosePrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("RevenueCat API Key:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(com.example.data.service.RevenueCatService.getMaskedApiKey(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Primary Entitlement:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(com.example.data.service.RevenueCatService.ENTITLEMENT_ID, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SDK Status:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = statusMsg ?: "Active",
                                fontSize = 11.sp,
                                color = if (isLiveSdk) GreenSuccess else RosePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Switch Subscription Tier (Preview & Testing):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.data.model.SubscriptionTier.values().forEach { tier ->
                            val isSelected = currentTier == tier
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val isSubscribed = tier != com.example.data.model.SubscriptionTier.FREE
                                    com.example.data.service.RevenueCatService.toggleProSimulated(isSubscribed, tier)
                                    onTogglePremium(isSubscribed)
                                    Toast.makeText(context, "${tier.title} activated", Toast.LENGTH_SHORT).show()
                                },
                                label = {
                                    Text(
                                        text = when (tier) {
                                            com.example.data.model.SubscriptionTier.FREE -> "Free"
                                            com.example.data.model.SubscriptionTier.WEEKLY -> "Weekly"
                                            com.example.data.model.SubscriptionTier.MONTHLY -> "Monthly"
                                            com.example.data.model.SubscriptionTier.ANNUAL -> "Annual"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                com.example.data.service.RevenueCatService.restorePurchases(
                                    onSuccess = { info ->
                                        val hasPro = info.entitlements["profilepulse_pro"]?.isActive == true ||
                                                     info.entitlements["pro"]?.isActive == true ||
                                                     info.entitlements.all.values.any { it.isActive }
                                        onTogglePremium(hasPro)
                                        Toast.makeText(context, if (hasPro) "Entitlement profilepulse_pro active!" else "No active subscriptions found.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Restore check: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Restore", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/account/subscriptions")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1.3f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                        ) {
                            Text("Manage in Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dating Preferences & Compatible Apps
            Text("Dating Preferences & Platform Matcher", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsTile(
                icon = Icons.Default.Explore,
                title = "Compatible Dating Apps",
                subtitle = "Set gender & preference, explore Grindr, Scruff, HER, Bumble, Hinge & coach tips",
                onClick = onOpenCompatibleApps
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Compliance Hub Sections
            Text("Google Play Compliance & Legal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsTile(
                icon = Icons.Default.Map,
                title = "Android Compliance Map (6 Policy Areas)",
                subtitle = "User data, Data safety, Paywall, AI & UGC safeguards",
                onClick = { viewingSection = "COMPLIANCE_MAP" }
            )

            SettingsTile(
                icon = Icons.AutoMirrored.Filled.FactCheck,
                title = "Pre-Submission Checklist",
                subtitle = "Final audit checklist before uploading AAB to Play Console",
                onClick = { viewingSection = "CHECKLIST" }
            )

            SettingsTile(
                icon = Icons.Default.PrivacyTip,
                title = "In-App Privacy Policy",
                subtitle = "Names Gemini AI provider, photo retention, no data sale",
                onClick = { viewingSection = "PRIVACY" }
            )

            SettingsTile(
                icon = Icons.Default.Description,
                title = "Terms of Service & UGC Rules",
                subtitle = "Prohibits explicit/harassing content, adults 18+ only",
                onClick = { viewingSection = "TERMS" }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // User Safety & Reported Content Log
            Text("Safety & Flagged AI Content (${reports.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            if (reports.isEmpty()) {
                Text(
                    text = "No content reports submitted. Flagging buttons on AI scores, bios, and copilot replies allow immediate in-app reporting without leaving the app.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            } else {
                reports.forEach { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(report.targetType, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Queued for Audit", color = RosePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Category: ${report.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("\"${report.contentSnippet}\"", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Play Mandatory Requirement: Account & Data Deletion
            Text("Data Controls & Deletion", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RedWarning)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "In accordance with Google Play's User Data & Account Deletion policies, you can delete your uploaded photos or wipe your complete account and history at any time.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDeletePhotosDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("delete_photos_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete My Photos & Cached Audits", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("delete_account_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedWarning)
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete My Account & All Data", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Confirmation Dialogs
    if (showDeletePhotosDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotosDialog = false },
            title = { Text("Delete Uploaded Photos?", fontWeight = FontWeight.Bold) },
            text = { Text("This will immediately delete all selected photos and cached audit insights from memory.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePhotos()
                        showDeletePhotosDialog = false
                        Toast.makeText(context, "All photos and cached audits deleted.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedWarning)
                ) {
                    Text("Delete Photos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotosDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Permanently Delete Account & Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This permanently purges all photos, bio snippets, audit records, and resets the app state. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccountAndData()
                        showDeleteAccountDialog = false
                        Toast.makeText(context, "Account and all associated data permanently deleted.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedWarning)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // In-App Policy Detail Modals
    when (viewingSection) {
        "PRIVACY" -> {
            PolicyDetailDialog(
                title = "Privacy Policy",
                content = com.example.ui.components.LegalPolicyTexts.PRIVACY_POLICY,
                onDismiss = { viewingSection = null }
            )
        }
        "TERMS" -> {
            PolicyDetailDialog(
                title = "Terms of Service",
                content = com.example.ui.components.LegalPolicyTexts.TERMS_OF_SERVICE,
                onDismiss = { viewingSection = null }
            )
        }
        "COMPLIANCE_MAP" -> {
            PolicyDetailDialog(
                title = "Android Compliance Map (6 Policy Areas)",
                content = """
                    The 6 Play Compliance Pillars:
                    
                    1. User Data & Privacy:
                    - Disclosed plainly before upload; zero media storage permissions; scoped Photo Picker used.
                    
                    2. Data Safety Declarations:
                    - Photos & bio collected and processed with Gemini AI; encrypted in transit; deletable.
                    
                    3. Account & Data Deletion:
                    - In-app 1-tap photo deletion and complete account purge buttons implemented.
                    
                    4. Payments & Subscriptions:
                    - Google Play Billing exclusively ($6.99/wk, $14.99/mo with 7-day trial, $59.99/yr). Clear renewal and cancel path.
                    
                    5. AI & UGC Safeguards:
                    - All outputs labeled AI-generated; in-app report button on every result screen; prompt refusals for sexual/harassing input.
                    
                    6. Store Listing & Promotion:
                    - No guaranteed match claims; no competitor trademark stuffing; adults-only declaration.
                """.trimIndent(),
                onDismiss = { viewingSection = null }
            )
        }
        "CHECKLIST" -> {
            PolicyDetailDialog(
                title = "Pre-Submission Checklist",
                content = """
                    Google Play AAB Pre-Submission Checklist:
                    
                    ✅ Privacy policy accessible at public URL and in-app, naming Gemini AI processing.
                    ✅ Data safety form declared accurately matching actual app behavior.
                    ✅ Prominent disclosure before first photo upload with affirmative consent.
                    ✅ Android Photo Picker used instead of broad storage permissions.
                    ✅ Terms of Use gate accepted before upload.
                    ✅ Working "Report this result" control on every AI result screen.
                    ✅ Refusal behavior active for sexualized or harassing inputs.
                    ✅ Adults-only 18+ declaration in onboarding and Play Console target audience.
                    ✅ Play Billing paywall displays price with billing period, renewal terms, and cancel path in Google Play.
                    ✅ Account & data deletion flow fully functional.
                """.trimIndent(),
                onDismiss = { viewingSection = null }
            )
        }
    }
}

@Composable
private fun SettingsTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RosePrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PolicyDetailDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(content, fontSize = 12.sp, lineHeight = 17.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text("Close")
            }
        }
    )
}
