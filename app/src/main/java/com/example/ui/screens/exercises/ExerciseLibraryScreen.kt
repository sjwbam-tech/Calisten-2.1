package com.example.ui.screens.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.ExerciseEntity
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.parallax.CinematicParallaxHero
import com.example.ui.parallax.LocalReduceMotion
import com.example.ui.parallax.ParallaxConfig
import com.example.ui.parallax.parallaxLazyScroll
import com.example.ui.theme.AlertRed
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

@Composable
fun ExerciseLibraryScreen(
    exercises: List<ExerciseEntity>,
    onCreateCustomExercise: (
        persianName: String,
        englishName: String,
        category: String,
        muscles: List<String>,
        movementPattern: String,
        difficulty: String,
        startingPosition: String,
        instructions: List<String>,
        safetyNotes: String
    ) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedExerciseDetail by remember { mutableStateOf<ExerciseEntity?>(null) }
    var showCreateCustomDialog by remember { mutableStateOf(false) }
    val reduceMotion = LocalReduceMotion.current

    val categories = listOf(
        "ALL" to "همه",
        "PUSH" to "فشاری (Push)",
        "PULL" to "کششی (Pull)",
        "LEGS" to "پاها (Legs)",
        "CORE" to "هسته بدن (Core)",
        "SKILLS" to "مهارت‌ها (Skills)",
        "GRIP" to "پنجه و آویزان"
    )

    val filteredExercises = exercises.filter { ex ->
        val matchesCategory = selectedCategory == "ALL" || ex.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                ex.persianName.contains(searchQuery, ignoreCase = true) ||
                ex.englishName.contains(searchQuery, ignoreCase = true) ||
                ex.musclesJson.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "کتابخانه حرکات کالیستنیکس",
            subtitle = "${filteredExercises.size} حرکت یافت شد",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { showCreateCustomDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer)
                        .testTag("create_custom_exercise_top_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ایجاد حرکت سفارشی",
                        tint = IceBluePrimary
                    )
                }
            }
        )

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجوی نام فارسی، انگلیسی یا عضله...", color = MutedSlate) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = IceBluePrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "پاک کردن", tint = MutedSlate)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_exercise_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = FrostWhite,
                unfocusedTextColor = FrostWhite,
                focusedBorderColor = IceBluePrimary,
                unfocusedBorderColor = Obsidian700,
                focusedContainerColor = Obsidian900,
                unfocusedContainerColor = Obsidian900
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Category Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (catId, label) ->
                FilterChip(
                    selected = selectedCategory == catId,
                    onClick = { selectedCategory = catId },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IceBluePrimary,
                        selectedLabelColor = Obsidian950
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val lazyListState = rememberLazyListState()

        // Exercise List
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                CinematicParallaxHero(
                    backgroundImageRes = R.drawable.hero_coastal_cliff_1790610437386,
                    contentDescription = "کتابخانه حرکات",
                    lazyListState = lazyListState,
                    config = ParallaxConfig.LibraryHeader,
                    reduceMotion = reduceMotion,
                    height = 130.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column {
                        Text(
                            text = "اطلس بیومکانیک کالیستنیکس",
                            style = MaterialTheme.typography.titleMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تحلیل گشتاور اهرم‌ها، سلامت مفاصل و زنجیره‌های پیشرفت",
                            style = MaterialTheme.typography.bodySmall,
                            color = IceBlueLight
                        )
                    }
                }
            }

            items(filteredExercises) { ex ->
                LuxuryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    onClick = { selectedExerciseDetail = ex }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    .background(Obsidian800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = IceBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ex.persianName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ex.englishName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBlueLight
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val diffColor = when (ex.difficulty) {
                                "BEGINNER" -> SafeGreen
                                "INTERMEDIATE" -> IceBluePrimary
                                else -> CautionYellow
                            }
                            Text(
                                text = when (ex.difficulty) {
                                    "BEGINNER" -> "مبتدی"
                                    "INTERMEDIATE" -> "متوسط"
                                    "ADVANCED" -> "پیشرفته"
                                    else -> ex.difficulty
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = diffColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val categoryName = when (ex.category) {
                                "PUSH" -> "فشاری"
                                "PULL" -> "کششی"
                                "LEGS" -> "پایین‌تنه"
                                "CORE" -> "میان‌تنه"
                                "SKILLS" -> "مهارت"
                                "GRIP" -> "گیرش"
                                else -> ex.category
                            }
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Exercise Detail Dialog
    if (selectedExerciseDetail != null) {
        val detail = selectedExerciseDetail!!
        AlertDialog(
            onDismissRequest = { selectedExerciseDetail = null },
            title = {
                Column {
                    Text(
                        text = detail.persianName,
                        style = MaterialTheme.typography.titleLarge,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = detail.englishName,
                        style = MaterialTheme.typography.labelMedium,
                        color = IceBlueLight
                    )
                }
            },
            text = {
                val detailScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(detailScrollState)
                ) {
                    CinematicParallaxHero(
                        backgroundImageRes = R.drawable.img_mountain_summit_mist_1790632774116,
                        contentDescription = detail.persianName,
                        scrollState = detailScrollState,
                        height = 110.dp,
                        config = ParallaxConfig.DetailHero,
                        reduceMotion = reduceMotion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Obsidian950.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "الگوی حرکتی: ${detail.movementPattern}",
                                style = MaterialTheme.typography.labelSmall,
                                color = IceBlueLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (detail.startingPosition.isNotEmpty()) {
                        Text(
                            text = "وضعیت شروع حرکت:",
                            style = MaterialTheme.typography.labelMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = detail.startingPosition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Exercise-specific joints, tendons, and connected structures
                    val anatomy = getExerciseAnatomy(detail)
                    if (anatomy != null) {
                        Text(
                            text = "مفاصل درگیر:",
                            style = MaterialTheme.typography.labelMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = anatomy.primaryJoints,
                            style = MaterialTheme.typography.bodySmall,
                            color = IceBlueLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "تاندون‌ها و ساختارهای همبند:",
                            style = MaterialTheme.typography.labelMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = anatomy.tendonsAndStructures,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (!anatomy.stabilizingStructures.isNullOrEmpty()) {
                            Text(
                                text = "ساختارهای تثبیت‌کننده و محافظ:",
                                style = MaterialTheme.typography.labelMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = anatomy.stabilizingStructures,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (detail.breathingNotes.isNotEmpty()) {
                        Text(
                            text = "الگوی تنفس و تمپو (${detail.tempo}):",
                            style = MaterialTheme.typography.labelMedium,
                            color = FrostWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = detail.breathingNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (detail.safetyCriteria.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CautionYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ملاحظات ایمنی و توقف:",
                                style = MaterialTheme.typography.labelMedium,
                                color = CautionYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = detail.safetyCriteria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlate
                        )
                    }
                }
            },
            confirmButton = {
                IceButton(
                    text = "بستن",
                    onClick = { selectedExerciseDetail = null },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            },
            containerColor = Obsidian900
        )
    }

    // Create Custom Exercise Dialog
    if (showCreateCustomDialog) {
        var customPersianName by remember { mutableStateOf("") }
        var customEnglishName by remember { mutableStateOf("") }
        var customCategory by remember { mutableStateOf("PUSH") }
        var customDifficulty by remember { mutableStateOf("INTERMEDIATE") }
        var customStartingPos by remember { mutableStateOf("") }
        var customSafety by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateCustomDialog = false },
            title = {
                Text(
                    text = "ایجاد حرکت سفارشی",
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
                    OutlinedTextField(
                        value = customPersianName,
                        onValueChange = { customPersianName = it },
                        label = { Text("نام فارسی حرکت") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customEnglishName,
                        onValueChange = { customEnglishName = it },
                        label = { Text("نام انگلیسی حرکت") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "دسته حرکت:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PUSH", "PULL", "LEGS", "CORE").forEach { cat ->
                            FilterChip(
                                selected = customCategory == cat,
                                onClick = { customCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customStartingPos,
                        onValueChange = { customStartingPos = it },
                        label = { Text("نحوه استقرار و شروع") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customSafety,
                        onValueChange = { customSafety = it },
                        label = { Text("نکات ایمنی و احتیاطی") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "ذخیره حرکت سفارشی",
                    onClick = {
                        if (customPersianName.isNotBlank()) {
                            onCreateCustomExercise(
                                customPersianName.trim(),
                                customEnglishName.trim().ifEmpty { customPersianName },
                                customCategory,
                                listOf("عضلات هدف"),
                                "CUSTOM",
                                customDifficulty,
                                customStartingPos,
                                listOf("اجرای کنترل‌شده با تمرکز بر فرم صحیح"),
                                customSafety
                            )
                            showCreateCustomDialog = false
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "save_custom_exercise_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateCustomDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}

private data class ExerciseAnatomyInfo(
    val primaryJoints: String,
    val tendonsAndStructures: String,
    val stabilizingStructures: String? = null
)

private fun getExerciseAnatomy(exercise: ExerciseEntity): ExerciseAnatomyInfo? {
    // 1. Precise movement-specific mappings for all exercises
    when (exercise.id) {
        "squat_bodyweight" -> return ExerciseAnatomyInfo(
            primaryJoints = "زانو، لگن، مچ پا",
            tendonsAndStructures = "تاندون پاتلار (کشکک)، تاندون چهارسر ران، تاندون آشیل",
            stabilizingStructures = "رباط‌های جانبی و متقاطع زانو، کپسول مفصلی ران"
        )
        "squat_bulgarian" -> return ExerciseAnatomyInfo(
            primaryJoints = "زانو، لگن، مچ پا",
            tendonsAndStructures = "تاندون پاتلار زانو، تاندون چهارسر ران، تاندون آشیل",
            stabilizingStructures = "ثبات‌دهنده‌های جانبی لگن، رباط‌های محافظ زانو"
        )
        "squat_pistol" -> return ExerciseAnatomyInfo(
            primaryJoints = "زانو، لگن، مچ پا",
            tendonsAndStructures = "تاندون پاتلار و چهارسر ران، تاندون آشیل، غضروف منیسک",
            stabilizingStructures = "کمپلکس مچ و کف پا، رباط‌های تثبیت‌کننده زانو"
        )
        "pushup_std" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون پشت‌بازو (تریسپس)، تاندون‌های قدامی شانه (روتاتور کاف)، تاندون‌های مچ دست",
            stabilizingStructures = "کپسول قدامی شانه، ثبات‌دهنده‌های کتف (اسکاپولا)"
        )
        "pushup_knee" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون پشت‌بازو، تاندون‌های مچ دست، تاندون‌های قدام شانه",
            stabilizingStructures = "ثبات‌دهنده‌های کتف"
        )
        "pushup_diamond" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون پشت‌بازو، تاندون‌های فلکسور و اکستنسور مچ، تاندون سینه",
            stabilizingStructures = "رباط‌های محافظ مچ دست، کپسول قدامی شانه"
        )
        "dips_parallel" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون دیستال تریسپس، کپسول قدامی شانه، تاندون‌های روتاتور کاف",
            stabilizingStructures = "لابروم شانه، ثبات‌دهنده‌های کمربند شانه‌ای"
        )
        "dips_bench" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون تریسپس، کپسول قدامی شانه، تاندون قدام دلتوئید",
            stabilizingStructures = "ساپورت‌کننده‌های کتف"
        )
        "pullup_std" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ و انگشتان دست",
            tendonsAndStructures = "تاندون دیستال دوسربازو (بایسپس)، تاندون‌های فلکسور ساعد و پنجه، تاندون‌های روتاتور کاف",
            stabilizingStructures = "لابروم گلنوئید شانه، ثبات‌دهنده‌های کتف (اسکاپولا)"
        )
        "pullup_chinup" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ و انگشتان دست",
            tendonsAndStructures = "تاندون دوسربازو (بایسپس)، تاندون براکیالیس، تاندون‌های فلکسور ساعد (اپیکوندیل داخلی)",
            stabilizingStructures = "رباط‌های طرفی آرنج، ثبات‌دهنده‌های کتف"
        )
        "inverted_row" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون دوسربازو، تاندون‌های روتاتور کاف و متوازی‌الاضلاع، تاندون‌های ساعد",
            stabilizingStructures = "کتف (اسکاپولا)، زنجیره خلفی بالاتنه"
        )
        "dead_hang" -> return ExerciseAnatomyInfo(
            primaryJoints = "مفاصل انگشتان دست، مچ دست، شانه",
            tendonsAndStructures = "تاندون‌های فلکسور عمقی و سطحی انگشتان، قرقره‌های وتری (A2/A4)، تاندون‌های روتاتور کاف",
            stabilizingStructures = "کپسول فوقانی شانه، فاسیای کف دست"
        )
        "plank_std" -> return ExerciseAnatomyInfo(
            primaryJoints = "ستون فقرات، لگن، شانه",
            tendonsAndStructures = "فاسیای توراکولومبار، خط آپونوروز سفیدی شکم (Linea Alba)، فلکسورهای لگن",
            stabilizingStructures = "ثبات‌دهنده‌های لومبوپلویک، عضلات عمقی شکم"
        )
        "hollow_body" -> return ExerciseAnatomyInfo(
            primaryJoints = "ستون فقرات، لگن",
            tendonsAndStructures = "خط سفید شکم (Linea Alba)، فاسیای کمری-سینه‌ای، تاندون‌های فلکسور ران",
            stabilizingStructures = "عضله عرضی شکم، ثبات‌دهنده لگن"
        )
        "hanging_leg_raise" -> return ExerciseAnatomyInfo(
            primaryJoints = "لگن، ستون فقرات کمری، شانه و مفاصل دست",
            tendonsAndStructures = "تاندون‌های فلکسور ران (ایلئوپسواس و راست‌رانی)، فاسیای شکمی، فلکسورهای ساعد",
            stabilizingStructures = "ثبات‌دهنده‌های لومبوپلویک، کمربند شانه‌ای در حالت آویزان"
        )
        "lsit" -> return ExerciseAnatomyInfo(
            primaryJoints = "لگن، شانه، آرنج، مچ دست",
            tendonsAndStructures = "تاندون فلکسورهای ران، تاندون پشت‌بازو، تاندون‌های مچ دست",
            stabilizingStructures = "پایین‌کشنده‌های کتف، کپسول مچ دست"
        )
        "handstand" -> return ExerciseAnatomyInfo(
            primaryJoints = "مچ دست، آرنج، شانه",
            tendonsAndStructures = "تاندون‌های فلکسور و اکستنسور مچ دست، تاندون تریسپس، تاندون‌های شانه",
            stabilizingStructures = "رباط‌های کارپال مچ، ثبات‌دهنده‌های کتف"
        )
        "front_lever_tuck" -> return ExerciseAnatomyInfo(
            primaryJoints = "شانه، آرنج، مچ و انگشتان دست",
            tendonsAndStructures = "تاندون دیستال دوسربازو (بایسپس)، تاندون عضله پشتی بزرگ (لاتس)، تاندون‌های ساعد",
            stabilizingStructures = "کپسول تحتانی و خلفی شانه، ثبات‌دهنده‌های کتف"
        )
    }

    // 2. Fallback pattern matching for custom exercises (strictly based on clear movement families)
    val cat = exercise.category.uppercase()
    val pattern = exercise.movementPattern.uppercase()
    val eng = exercise.englishName.lowercase()
    val fa = exercise.persianName

    return when {
        cat == "LEGS" || pattern.contains("SQUAT") || pattern.contains("LUNGE") || eng.contains("squat") || fa.contains("اسکات") -> {
            ExerciseAnatomyInfo(
                primaryJoints = "زانو، لگن، مچ پا",
                tendonsAndStructures = "تاندون پاتلار (کشکک)، تاندون چهارسر ران، تاندون آشیل",
                stabilizingStructures = "رباط‌های محافظ زانو، کپسول مفصلی ران"
            )
        }
        cat == "PUSH" || pattern.contains("PUSH") || eng.contains("push") || eng.contains("dip") || fa.contains("شنا") || fa.contains("دیپ") -> {
            ExerciseAnatomyInfo(
                primaryJoints = "شانه، آرنج، مچ دست",
                tendonsAndStructures = "تاندون پشت‌بازو (تریسپس)، تاندون‌های قدامی شانه (روتاتور کاف)، تاندون‌های مچ دست",
                stabilizingStructures = "کپسول قدامی شانه، ثبات‌دهنده‌های کتف"
            )
        }
        cat == "PULL" || pattern.contains("PULL") || eng.contains("pull") || eng.contains("chin") || eng.contains("row") || fa.contains("بارفیکس") -> {
            ExerciseAnatomyInfo(
                primaryJoints = "شانه، آرنج، مچ و انگشتان دست",
                tendonsAndStructures = "تاندون دوسربازو (بایسپس)، تاندون‌های فلکسور ساعد و پنجه، تاندون‌های روتاتور کاف",
                stabilizingStructures = "لابروم گلنوئید شانه، ثبات‌دهنده‌های کتف"
            )
        }
        cat == "CORE" || pattern.contains("CORE") || pattern.contains("PLANK") || eng.contains("plank") || fa.contains("پلانک") -> {
            ExerciseAnatomyInfo(
                primaryJoints = "ستون فقرات، لگن",
                tendonsAndStructures = "فاسیای توراکولومبار، خط آپونوروز سفیدی شکم (Linea Alba)",
                stabilizingStructures = "ثبات‌دهنده‌های لومبوپلویک"
            )
        }
        cat == "GRIP" || pattern.contains("HANG") || eng.contains("hang") || fa.contains("آویزان") -> {
            ExerciseAnatomyInfo(
                primaryJoints = "مفاصل انگشتان دست، مچ دست، شانه",
                tendonsAndStructures = "تاندون‌های فلکسور عمقی و سطحی انگشتان، قرقره‌های وتری (Pulleys)",
                stabilizingStructures = "کپسول فوقانی شانه"
            )
        }
        else -> null // Do not guess if no validated anatomical data exists
    }
}
