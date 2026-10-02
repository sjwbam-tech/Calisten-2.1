package com.example.ui.screens.skills

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.SkillEntity
import com.example.data.local.entity.SkillProgressEntity
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.parallax.CinematicParallaxHero
import com.example.ui.parallax.LocalReduceMotion
import com.example.ui.parallax.ParallaxConfig
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
fun SkillsScreen(
    skills: List<SkillEntity>,
    skillProgressList: List<SkillProgressEntity>,
    onUpdateSkillProgress: (skillId: String, stepIndex: Int, holdSec: Int, reps: Int, quality: Int) -> Unit,
    onIntegrateSkill: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedSkillForDetail by remember { mutableStateOf<SkillEntity?>(null) }
    var skillLoggingDialog by remember { mutableStateOf<SkillEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "درخت مهارت‌های کالیستنیکس",
            subtitle = "مسیر پیشرفت گام‌به‌گام و شاخص‌های آمادگی",
            onBackClick = onBack
        )

        val lazyListState = rememberLazyListState()
        val reduceMotion = LocalReduceMotion.current

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                CinematicParallaxHero(
                    backgroundImageRes = R.drawable.img_mountain_summit_mist_1790632774116,
                    contentDescription = "مهارت‌های کالیستنیکس",
                    lazyListState = lazyListState,
                    config = ParallaxConfig.SkillsCinematic,
                    reduceMotion = reduceMotion,
                    height = 140.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column {
                        Text(
                            text = "تسلط بر وزن بدن در فضا",
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "پیش‌نیازهای ساختاری، گشتاور لورها و پیشرفت تدریجی تاندون‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            color = IceBlueLight
                        )
                    }
                }
            }

            items(skills) { skill ->
                val progress = skillProgressList.firstOrNull { it.skillId == skill.id }
                val steps = try {
                    val arr = JSONArray(skill.progressionStepsJson)
                    (0 until arr.length()).map { arr.getString(it) }
                } catch (_: Exception) {
                    emptyList()
                }

                val currentStepIdx = progress?.currentStepIndex ?: 0
                val currentStepName = steps.getOrNull(currentStepIdx) ?: "گام ۱"

                LuxuryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = { selectedSkillForDetail = skill }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(IceBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = IceBluePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = skill.persianName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = skill.englishName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IceBlueLight
                                    )
                                }
                            }

                            // Current Step Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Obsidian800)
                                    .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "گام ${currentStepIdx + 1} از ${steps.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = skill.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current milestone status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مرحله فعلی: $currentStepName",
                                style = MaterialTheme.typography.labelMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Medium
                            )

                            Row {
                                TextButton(
                                    onClick = { onIntegrateSkill(skill.id) }
                                ) {
                                    Text("ادغام در برنامه", color = IceBluePrimary, fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = { skillLoggingDialog = skill }
                                ) {
                                    Text("ثبت رکورد", color = IceBlueLight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog with full Progression Tree
    if (selectedSkillForDetail != null) {
        val skill = selectedSkillForDetail!!
        val steps = try {
            val arr = JSONArray(skill.progressionStepsJson)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
        val progress = skillProgressList.firstOrNull { it.skillId == skill.id }
        val currentStepIdx = progress?.currentStepIndex ?: 0

        AlertDialog(
            onDismissRequest = { selectedSkillForDetail = null },
            title = {
                Text(
                    text = "درخت پیشرفت ${skill.persianName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "مراحل گام‌به‌گام برای تسلط کامل:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    steps.forEachIndexed { index, stepName ->
                        val isPassed = index < currentStepIdx
                        val isCurrent = index == currentStepIdx

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) IceBlueContainer else Obsidian800)
                                .border(1.dp, if (isCurrent) IceBluePrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isPassed) SafeGreen else if (isCurrent) IceBluePrimary else Obsidian700),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPassed) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Obsidian950, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrent) Obsidian950 else FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCurrent) FrostWhite else MutedSlate,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IceOutlineButton(
                        text = "ادغام در برنامه",
                        onClick = {
                            onIntegrateSkill(skill.id)
                            selectedSkillForDetail = null
                        }
                    )
                    IceButton(
                        text = "بستن",
                        onClick = { selectedSkillForDetail = null },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            },
            containerColor = Obsidian900
        )
    }

    // Log Skill Milestone Dialog
    if (skillLoggingDialog != null) {
        val skill = skillLoggingDialog!!
        val steps = try {
            val arr = JSONArray(skill.progressionStepsJson)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
        val currentProg = skillProgressList.firstOrNull { it.skillId == skill.id }
        var targetStep by remember { mutableIntStateOf(currentProg?.currentStepIndex ?: 0) }
        var holdSecInput by remember { mutableIntStateOf(currentProg?.bestHoldSec ?: 10) }
        var qualityScore by remember { mutableIntStateOf(8) }

        AlertDialog(
            onDismissRequest = { skillLoggingDialog = null },
            title = {
                Text(
                    text = "ثبت رکورد مرحله ${skill.persianName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "مرحله مهارت:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    steps.forEachIndexed { idx, name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetStep = idx }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (targetStep == idx) IceBluePrimary else Obsidian700),
                                contentAlignment = Alignment.Center
                            ) {
                                if (targetStep == idx) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Obsidian950, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name, color = FrostWhite, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "زمان هلد ثبت‌شده: $holdSecInput ثانیه", style = MaterialTheme.typography.labelMedium, color = FrostWhite)
                    Slider(
                        value = holdSecInput.toFloat(),
                        onValueChange = { holdSecInput = it.toInt() },
                        valueRange = 0f..60f,
                        steps = 59,
                        colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "کیفیت فرم اجرا: $qualityScore از ۱۰", style = MaterialTheme.typography.labelMedium, color = FrostWhite)
                    Slider(
                        value = qualityScore.toFloat(),
                        onValueChange = { qualityScore = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = IceBluePrimary, activeTrackColor = IceBluePrimary)
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "ذخیره پیشرفت",
                    onClick = {
                        onUpdateSkillProgress(skill.id, targetStep, holdSecInput, 0, qualityScore)
                        skillLoggingDialog = null
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { skillLoggingDialog = null }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
