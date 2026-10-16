package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RosePrimary

object LegalPolicyTexts {

    const val CONTACT_EMAIL = "diamondedgemetals@gmail.com"
    const val APP_NAME = "ProfilePulse"
    const val LAST_UPDATED = "October 2026"

    val PRIVACY_POLICY = """
        $APP_NAME PRIVACY POLICY
        Last Updated: $LAST_UPDATED
        Contact / Data Controller: $CONTACT_EMAIL

        1. INTRODUCTION & ADULTS-ONLY COMMITMENT
        ProfilePulse ("we", "us", or "our") operates the ProfilePulse Android application. We are committed to protecting your privacy and handling your data with complete transparency. ProfilePulse is strictly designed for adults aged 18 and older. We do not knowingly collect or solicit data from anyone under the age of 18.

        2. DATA WE COLLECT
        We collect only the minimum data required to deliver our profile audit and dating coach services:
        • Profile Photos: 4 to 6 photos that you explicitly choose using the zero-permission Android Photo Picker. We do NOT request broad media storage permissions (READ_EXTERNAL_STORAGE or READ_MEDIA_IMAGES).
        • Bio & Prompt Text: Dating profile bios, answers to prompt questions, and conversational snippets that you voluntarily paste or type into the app.
        • Preferences: Gender and dating orientation preferences selected to customize app recommendations.
        • Purchase Data: Subscription status and entitlement records managed via Google Play Billing and RevenueCat SDK. We do NOT receive or store your credit card numbers or financial details.
        • Safety & Report Logs: Any content snippets you voluntarily report using the in-app "Report this result" control.

        3. HOW YOUR DATA IS USED
        Your photos and profile text are used exclusively to:
        • Provide algorithmic feedback on photo clarity, lighting, facial composition, and sequence ordering.
        • Generate creative bio rewrites and icebreaker ideas.
        • Match you with compatible dating platforms (such as Grindr, Scruff, HER, Bumble, and Hinge).
        • We NEVER sell, rent, monetize, or broker your personal data or photos to third parties or advertisers.

        4. AI PROCESSING & THIRD-PARTY PROVIDERS
        • Google Gemini AI: Profile analysis, bio rewrites, and conversational tips are generated using Google Gemini AI models. Data transmitted for AI processing is encrypted in transit using industry-standard TLS encryption.
        • RevenueCat: We utilize RevenueCat SDK to verify subscription status and manage pro entitlements ('profilepulse_pro').

        5. DATA RETENTION & SECURITY
        • Photo processing is temporary and session-focused.
        • All network communications utilize encrypted HTTPS/TLS channels.
        • We do not retain copies of your photos on external marketing databases.

        6. USER CONTROL & COMPLETE DATA DELETION
        In strict compliance with Google Play's User Data & Data Deletion policies, you maintain full control:
        • 1-Tap Photo Deletion: In Settings, tap "Delete My Photos & Cached Audits" to instantly erase all photos and cached scores from the app.
        • Complete Account Wipe: In Settings, tap "Delete My Account & All Data" to permanently purge all onboarding records, preferences, bios, and report logs.
        • Manual Deletion Request: You may also email our data team at $CONTACT_EMAIL with "Data Deletion Request" in the subject line, and any associated records will be permanently expunged within 30 days.

        7. GOOGLE PLAY BILLING & SUBSCRIPTIONS
        ProfilePulse offers optional auto-renewing subscriptions (Weekly, Monthly with 7-day trial, and Yearly) processed exclusively via Google Play Billing. You may manage or cancel your subscription at any time by visiting Google Play Store > Subscriptions.

        8. CHANGES TO THIS POLICY
        We may update this Privacy Policy from time to time. Material updates will be indicated by the "Last Updated" date at the top of this document and through in-app notifications.

        If you have questions regarding this Privacy Policy, please contact us at:
        $CONTACT_EMAIL
    """.trimIndent()

    val TERMS_OF_SERVICE = """
        $APP_NAME TERMS OF SERVICE
        Last Updated: $LAST_UPDATED

        1. ACCEPTANCE & ELIGIBILITY
        By downloading, installing, or using ProfilePulse, you agree to be bound by these Terms of Service. You must be at least 18 years old to use ProfilePulse.

        2. USER CONDUCT & ACCEPTABLE USE
        You agree to upload only photographs and bio text that you own or have explicit rights to use. You agree NOT to upload:
        • Sexually explicit, pornographic, or obscene material.
        • Any imagery depicting minors.
        • Content that promotes hate speech, violence, harassment, or defamation.
        • Images or personal information belonging to other individuals without their consent.

        3. AI-GENERATED CONTENT TRANSPARENCY
        All photo scores, lead photo recommendations, bio rewrites, and copilot suggestions are generated by artificial intelligence for guidance and entertainment purposes. ProfilePulse does not guarantee dating matches, romantic outcomes, or third-party platform success.

        4. IN-APP CONTENT REPORTING
        If any AI output generates questionable or offensive phrasing, you can tap the "Report this result" control located on the screen to instantly flag it for audit.

        5. SUBSCRIPTIONS & CANCELLATIONS
        Payments are charged to your Google Play account at confirmation of purchase. Subscriptions automatically renew unless cancelled in Google Play subscription settings at least 24 hours before the end of the current billing cycle.

        6. CONTACT
        For inquiries or support, contact: $CONTACT_EMAIL
    """.trimIndent()
}

@Composable
fun LegalDetailDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = RosePrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
