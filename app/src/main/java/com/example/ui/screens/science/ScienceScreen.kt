package com.example.ui.screens.science

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ScienceSourceEntity
import com.example.ui.components.IceButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.SafeGreen
import org.json.JSONArray

@Composable
fun ScienceScreen(
    sources: List<ScienceSourceEntity>,
    currentPackVersion: String,
    onUpdateSciencePack: () -> Unit,
    onBack: () -> Unit
) {
    var selectedSourceDetail by remember { mutableStateOf<ScienceSourceEntity?>(null) }
    var updateSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "پایگاه علمی تمرین و شواهد",
            subtitle = "منابع معتبر فیزیولوژی و بیومکانیک",
            onBackClick = onBack
        )

        // Science Pack Version Banner
        LuxuryCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            borderColor = GlassCardBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = IceBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "بسته علمی فعال: Science Pack $currentPackVersion",
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مبتنی بر متاتحلیل‌ها و تحقیقات دانشگاهی معتبر",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                }

                if (currentPackVersion == "v1.0") {
                    IceButton(
                        text = "بروزرسانی به v1.1",
                        onClick = {
                            onUpdateSciencePack()
                            updateSuccessMessage = "بسته علمی با موفقیت به نسخه v1.1 ارتقا یافت."
                        },
                        modifier = Modifier.height(36.dp),
                        testTag = "update_science_pack_btn"
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "آخرین نسخه", color = SafeGreen, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (updateSuccessMessage != null) {
            Text(
                text = updateSuccessMessage ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = SafeGreen,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Sources List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(sources) { src ->
                LuxuryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    onClick = { selectedSourceDetail = src }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = src.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = IceBlueLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "سال انتشار: ${src.publicationYear}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = src.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = src.authorOrg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "کاربرد عملی در کالیستن: ${src.practicalApplication}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FrostWhite
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedSourceDetail != null) {
        val src = selectedSourceDetail!!
        val takeaways = try {
            val arr = JSONArray(src.keyTakeawaysJson)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }

        AlertDialog(
            onDismissRequest = { selectedSourceDetail = null },
            title = {
                Column {
                    Text(
                        text = src.topic,
                        style = MaterialTheme.typography.labelMedium,
                        color = IceBlueLight
                    )
                    Text(
                        text = src.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "محقق و ژورنال:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Text(text = src.authorOrg, style = MaterialTheme.typography.bodyMedium, color = FrostWhite)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "سطح شواهد علمی: ${src.evidenceLevel}", style = MaterialTheme.typography.labelSmall, color = IceBluePrimary)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "یافته‌های کلیدی پژوهش:", style = MaterialTheme.typography.labelMedium, color = FrostWhite, fontWeight = FontWeight.Bold)
                    takeaways.forEach { item ->
                        Text(text = "• $item", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "کاربرد عملی در تمرینات شما:", style = MaterialTheme.typography.labelMedium, color = FrostWhite, fontWeight = FontWeight.Bold)
                    Text(text = src.practicalApplication, style = MaterialTheme.typography.bodySmall, color = FrostWhite)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "محدودیت‌های مطالعه:", style = MaterialTheme.typography.labelSmall, color = CautionYellow)
                    Text(text = src.limitations, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
                }
            },
            confirmButton = {
                IceButton(
                    text = "بستن",
                    onClick = { selectedSourceDetail = null },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            },
            containerColor = Obsidian900
        )
    }
}
