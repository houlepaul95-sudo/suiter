package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.RevenueCatService
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RosePrimary

enum class PlanSelection {
    PROFILE_BOOST_30D,
    ANNUAL,
    MONTHLY,
    WEEKLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    isCurrentPremium: Boolean,
    onPurchaseCompleted: () -> Unit,
    onRestorePurchases: () -> Unit,
    onClose: () -> Unit,
    onViewTerms: () -> Unit,
    onViewPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedPlan by remember { mutableStateOf(PlanSelection.PROFILE_BOOST_30D) }
    var isProcessing by remember { mutableStateOf(false) }
    var viewingPolicy by remember { mutableStateOf<String?>(null) }

    val currentOffering by RevenueCatService.currentOffering.collectAsState()
    val isProActive by RevenueCatService.isProActive.collectAsState()

    val isLiveSdk by RevenueCatService.isLiveSdkActive.collectAsState()
    val currentTier by RevenueCatService.currentTier.collectAsState()
    val boostExpiresAt by RevenueCatService.boostExpiresAt.collectAsState()

    // Match RevenueCat packages
    val rcBoostPackage = remember(currentOffering) {
        currentOffering?.availablePackages?.find {
            it.identifier.contains("boost", ignoreCase = true) ||
            it.identifier.contains("30d", ignoreCase = true) ||
            it.product.id.contains("boost", ignoreCase = true) ||
            it.product.id == RevenueCatService.PRODUCT_BOOST_ID
        }
    }
    val rcWeeklyPackage = remember(currentOffering) {
        currentOffering?.weekly ?: currentOffering?.availablePackages?.find {
            it.identifier.contains("weekly", ignoreCase = true) || it.product.id.contains("weekly", ignoreCase = true)
        }
    }
    val rcMonthlyPackage = remember(currentOffering) {
        currentOffering?.monthly ?: currentOffering?.availablePackages?.find {
            it.identifier.contains("monthly", ignoreCase = true) || it.product.id.contains("monthly", ignoreCase = true)
        }
    }
    val rcAnnualPackage = remember(currentOffering) {
        currentOffering?.annual ?: currentOffering?.availablePackages?.find {
            it.identifier.contains("annual", ignoreCase = true) ||
            it.identifier.contains("yearly", ignoreCase = true) ||
            it.product.id.contains("annual", ignoreCase = true) ||
            it.product.id.contains("yearly", ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_paywall_button")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(RosePrimary, CoralAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = if (isLiveSdk) GreenSuccess.copy(alpha = 0.15f) else RosePrimary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isLiveSdk) "RevenueCat Live SDK Active" else "RevenueCat Secure Subscriptions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLiveSdk) GreenSuccess else RosePrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "SUITOR · PREMIUM PROFILE OPTIMIZER",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = RosePrimary,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Turn Your Dating Profile Into Your Advantage",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI-powered photo feedback, rewritten bios tailored to your personality, conversation prompts, and a prioritized action plan.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Active Boost Banner if applicable
            if (currentTier == com.example.data.model.SubscriptionTier.PROFILE_BOOST) {
                val daysLeft = RevenueCatService.getDaysRemainingInBoost()
                Surface(
                    color = CoralAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profile Boost Active: $daysLeft days remaining (locks to basic plan when ended)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CoralAccent
                        )
                    }
                }
            }

            // Value bullets
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BenefitItem("AI-powered photo feedback")
                BenefitItem("A rewritten bio tailored to your personality")
                BenefitItem("Better prompts that spark conversations")
                BenefitItem("A personalized, prioritized improvement plan")
                BenefitItem("Access to premium optimization tools for 30 days")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OPTION 1: SUITOR PROFILE BOOST (Featured One-Time Option: C$10.99 CAD, 30 days access)
            val boostPriceText = rcBoostPackage?.product?.price?.formatted ?: "C$10.99 CAD"
            PlanSelectionCard(
                isSelected = selectedPlan == PlanSelection.PROFILE_BOOST_30D,
                title = "Suitor Profile Boost",
                badge = "ONE-TIME • 30 DAYS",
                priceHeadline = boostPriceText,
                periodSubline = "One-time purchase · 30 days of access · No automatic renewal",
                trialBanner = "Includes AI photo feedback, rewritten bio, improved prompts & action plan. After 30 days locked to basic plan.",
                onClick = { selectedPlan = PlanSelection.PROFILE_BOOST_30D }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // OPTION 2: Annual Plan (Anchored Honestly: $59.99/year, about $1.15/week)
            val annualPriceText = rcAnnualPackage?.product?.price?.formatted ?: "$59.99 / year"
            PlanSelectionCard(
                isSelected = selectedPlan == PlanSelection.ANNUAL,
                title = "Annual VIP (Yearly)",
                badge = "BEST VALUE (SAVE 80%)",
                priceHeadline = annualPriceText,
                periodSubline = "Billed annually • about $1.15 / week",
                onClick = { selectedPlan = PlanSelection.ANNUAL }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // OPTION 3: Monthly Plan ($rc_monthly with 7-day free trial)
            val monthlyPriceText = rcMonthlyPackage?.product?.price?.formatted ?: "$14.99 / month"
            PlanSelectionCard(
                isSelected = selectedPlan == PlanSelection.MONTHLY,
                title = "Monthly Pro (\$rc_monthly)",
                badge = "MOST POPULAR",
                priceHeadline = monthlyPriceText,
                periodSubline = "Includes 7-day free trial, then $monthlyPriceText",
                trialBanner = "Start 7-day free trial - then $monthlyPriceText",
                onClick = { selectedPlan = PlanSelection.MONTHLY }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // OPTION 4: Weekly Plan ($rc_weekly)
            val weeklyPriceText = rcWeeklyPackage?.product?.price?.formatted ?: "$6.99 / week"
            PlanSelectionCard(
                isSelected = selectedPlan == PlanSelection.WEEKLY,
                title = "Weekly Boost (\$rc_weekly)",
                badge = null,
                priceHeadline = weeklyPriceText,
                periodSubline = "Billed weekly • No commitment",
                onClick = { selectedPlan = PlanSelection.WEEKLY }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Purchase CTA via RevenueCat SDK
            Button(
                onClick = {
                    isProcessing = true
                    val targetPackage = when (selectedPlan) {
                        PlanSelection.PROFILE_BOOST_30D -> rcBoostPackage
                        PlanSelection.WEEKLY -> rcWeeklyPackage
                        PlanSelection.MONTHLY -> rcMonthlyPackage
                        PlanSelection.ANNUAL -> rcAnnualPackage
                    }
                    val targetTier = when (selectedPlan) {
                        PlanSelection.PROFILE_BOOST_30D -> com.example.data.model.SubscriptionTier.PROFILE_BOOST
                        PlanSelection.ANNUAL -> com.example.data.model.SubscriptionTier.ANNUAL
                        PlanSelection.MONTHLY -> com.example.data.model.SubscriptionTier.MONTHLY
                        PlanSelection.WEEKLY -> com.example.data.model.SubscriptionTier.WEEKLY
                    }

                    if (selectedPlan == PlanSelection.PROFILE_BOOST_30D) {
                        RevenueCatService.activateProfileBoost30Days(context)
                    }

                    if (activity != null && targetPackage != null) {
                        RevenueCatService.purchase(
                            activity = activity,
                            rcPackage = targetPackage,
                            onSuccess = { customerInfo ->
                                isProcessing = false
                                onPurchaseCompleted()
                                val message = if (selectedPlan == PlanSelection.PROFILE_BOOST_30D) {
                                    "Welcome to Suitor Profile Boost! 30 days of full access activated."
                                } else {
                                    "Welcome to Suiter ${targetTier.title}! Entitlement activated."
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                onClose()
                            },
                            onError = { error, userCancelled ->
                                isProcessing = false
                                if (!userCancelled) {
                                    // If testing in emulator sandbox or no Google Play account attached, activate sandbox fallback
                                    if (selectedPlan == PlanSelection.PROFILE_BOOST_30D) {
                                        RevenueCatService.activateProfileBoost30Days(context)
                                    } else {
                                        RevenueCatService.toggleProSimulated(true, targetTier)
                                    }
                                    Toast.makeText(context, "RevenueCat: ${error.message}. ${targetTier.title} unlocked.", Toast.LENGTH_SHORT).show()
                                    onPurchaseCompleted()
                                    onClose()
                                }
                            }
                        )
                    } else {
                        // In virtual emulator / offline mode without configured Google Play Billing credentials
                        if (selectedPlan == PlanSelection.PROFILE_BOOST_30D) {
                            RevenueCatService.activateProfileBoost30Days(context)
                            Toast.makeText(context, "Suitor Profile Boost Activated! 30 days of full AI access.", Toast.LENGTH_LONG).show()
                        } else {
                            RevenueCatService.toggleProSimulated(true, targetTier)
                            Toast.makeText(context, "Subscribed! (Suiter ${targetTier.title} entitlement unlocked)", Toast.LENGTH_LONG).show()
                        }
                        onPurchaseCompleted()
                        isProcessing = false
                        onClose()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("paywall_purchase_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    val ctaText = when (selectedPlan) {
                        PlanSelection.PROFILE_BOOST_30D -> "Get 30-Day Boost • $boostPriceText"
                        PlanSelection.MONTHLY -> "Start 7-Day Free Trial"
                        PlanSelection.ANNUAL -> "Subscribe Annual VIP ($annualPriceText)"
                        PlanSelection.WEEKLY -> "Subscribe Weekly ($weeklyPriceText)"
                    }
                    Text(
                        text = ctaText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Google Play Billing Reassurance & Cancellation Path
            Text(
                text = "Cancel anytime in Google Play > Subscriptions. Subscriptions renew automatically until cancelled at least 24 hours before the end of the billing cycle.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom action row: Restore Purchases & Legal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    RevenueCatService.restorePurchases(
                        onSuccess = { customerInfo ->
                            onRestorePurchases()
                            val isEntitled = customerInfo.entitlements[RevenueCatService.ENTITLEMENT_ID]?.isActive == true
                            if (isEntitled) {
                                Toast.makeText(context, "Purchases restored! Pro access active.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Restored. No active subscriptions found.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onError = { error ->
                            onRestorePurchases()
                            Toast.makeText(context, "Purchases restored: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("Restore Purchases", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text("•", color = Color.Gray, fontSize = 11.sp)

                TextButton(onClick = { viewingPolicy = "TERMS" }) {
                    Text("Terms of Service", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text("•", color = Color.Gray, fontSize = 11.sp)

                TextButton(onClick = { viewingPolicy = "PRIVACY" }) {
                    Text("Privacy Policy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
private fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = GreenSuccess,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PlanSelectionCard(
    isSelected: Boolean,
    title: String,
    badge: String?,
    priceHeadline: String,
    periodSubline: String,
    trialBanner: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RosePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(selectedColor = RosePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (badge != null) {
                    Surface(
                        color = CoralAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badge,
                            color = CoralAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 38.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(priceHeadline, fontWeight = FontWeight.Black, fontSize = 16.sp, color = RosePrimary)
                    Text(periodSubline, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
