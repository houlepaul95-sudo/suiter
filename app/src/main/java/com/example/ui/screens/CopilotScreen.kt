package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.CopilotReply
import com.example.ui.components.AiLabelBadge
import com.example.ui.components.ReportResultButton
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RosePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopilotScreen(
    replies: List<CopilotReply>,
    onAskCopilot: (context: String, isReplyMode: Boolean) -> Unit,
    onBack: () -> Unit,
    onReportSubmitted: (targetType: String, snippet: String, category: String) -> Unit
) {
    val context = LocalContext.current
    var isReplyMode by remember { mutableStateOf(false) } // false: Opener, true: What to say next
    var inputContext by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val openerQuickChips = listOf(
        "🐶 Bio: Loves dogs, hiking & cold brew",
        "🍷 Photo: Rooftop wine & travel in Italy",
        "🍕 Prompt: Best pizza in the city debate",
        "🏋️ Prompt: Gym by morning, tacos by night"
    )

    val replyQuickChips = listOf(
        "Dry: 'Haha yeah work has been super crazy lately'",
        "Low effort: 'nm hbu'",
        "Teasing: 'You definitely look like trouble haha'",
        "Date ready: 'I love Italian food, what spots do you like?'"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dating Copilot", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Senior Dating Coach AI • Tactical Date Conversion", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
            Spacer(modifier = Modifier.height(4.dp))

            // AI Coach Persona Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(RosePrimary, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dating Coach Tactical Directives", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (!isReplyMode) "Bypass boring 'hey' messages. Create instant curiosity & banter."
                                   else "Escape texting purgatory. Smoothly bridge conversation to phone/date in 4-6 texts.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = if (isReplyMode) 1 else 0,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = !isReplyMode,
                    onClick = { isReplyMode = false },
                    text = { Text("Match Openers", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = isReplyMode,
                    onClick = { isReplyMode = true },
                    text = { Text("What to Say Next", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (!isReplyMode) "Paste match's prompt or photo details:" else "Paste their last message:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = inputContext,
                onValueChange = { inputContext = it },
                placeholder = {
                    Text(
                        if (!isReplyMode)
                            "e.g. Loves golden retrievers, coffee snob, claims they make the world's best sourdough..."
                        else
                            "e.g. They said: 'Haha work has been so insane, I'm practically living at the office.'",
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copilot_input_field"),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Context Presets
            Text("Quick scenarios for instant testing:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(if (!isReplyMode) openerQuickChips else replyQuickChips) { chipText ->
                    SuggestionChip(
                        onClick = {
                            inputContext = chipText.substringAfter(": ")
                            isLoading = true
                            onAskCopilot(inputContext, isReplyMode)
                            isLoading = false
                        },
                        label = { Text(chipText, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    isLoading = true
                    onAskCopilot(inputContext, isReplyMode)
                    isLoading = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_copilot_replies_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (!isReplyMode) "Generate Magnetic Openers" else "Coach My Next Move (Get The Date)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RosePrimary)
                }
            } else if (replies.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tactical Responses (High Conversion)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    AiLabelBadge(label = "Coach Strategy")
                }

                Spacer(modifier = Modifier.height(10.dp))

                replies.forEach { reply ->
                    val isRefusal = reply.id == "refusal"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRefusal) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isRefusal) MaterialTheme.colorScheme.error else RosePrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = reply.style,
                                        color = if (isRefusal) MaterialTheme.colorScheme.onError else RosePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                if (!isRefusal) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Copilot Reply", reply.text)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied coach reply to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp).testTag("copy_copilot_reply_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = reply.text,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                fontWeight = if (isRefusal) FontWeight.Medium else FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = reply.reason,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            ReportResultButton(
                                targetType = "Copilot Reply",
                                contentSnippet = reply.text,
                                onReportSubmitted = onReportSubmitted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
