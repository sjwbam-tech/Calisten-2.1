package com.example.ui.screens.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.ProfileEntity
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueGlow
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian800
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950

@Composable
fun WelcomeScreen(
    profiles: List<ProfileEntity>,
    onSelectProfile: (String) -> Unit,
    onCreateProfile: (
        name: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        gender: String,
        waistCm: Float?,
        activityLevel: String,
        experienceLevel: String,
        goals: List<String>,
        equipment: List<String>,
        trainingDays: Int,
        assessment: AssessmentEntity?
    ) -> Unit,
    onRestoreBackup: (String) -> Boolean,
    onStartDemoMode: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSelectDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf<String?>(null) }
    var restoreSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
    ) {
        // Nature Landscape Background (Alpine Mist & Mountain Ridge)
        Image(
            painter = painterResource(id = R.drawable.rocky_granite_cliff_1790772687837),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.22f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Obsidian950.copy(alpha = 0.85f),
                            Obsidian950.copy(alpha = 0.95f),
                            Obsidian950
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Emblem
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(IceBlueContainer, Obsidian900)
                        )
                    )
                    .border(2.dp, IceBluePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = IceBluePrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "به کالیستن خوش آمدید",
                style = MaterialTheme.typography.displayMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "مربی شخصی کالیستنیکس، برنامه‌ریزی علمی و ارزیابی پیشرفت",
                style = MaterialTheme.typography.bodyLarge,
                color = IceBlueLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "کاملاً آفلاین، متکی بر اصول بیومکانیک و اضافه‌بار تدریجی بدون داده‌های ساختگی",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Option 1: Create Profile
            IceButton(
                text = "ساخت پروفایل جدید",
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                testTag = "welcome_create_profile_btn"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Select Existing Profile
            if (profiles.isNotEmpty()) {
                IceOutlineButton(
                    text = "ورود به پروفایل موجود (${profiles.size} پروفایل ثبت‌شده)",
                    onClick = { showSelectDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "welcome_select_profile_btn"
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Option 3: Restore Backup
            IceOutlineButton(
                text = "بازیابی اطلاعات از فایل پشتیبان (Backup)",
                onClick = { showRestoreDialog = true },
                modifier = Modifier.fillMaxWidth(),
                testTag = "welcome_restore_backup_btn"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 4: Optional Demo Mode
            TextButton(
                onClick = onStartDemoMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MutedSlate,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ورود به حالت نمایشی اختیاری (Demo Mode)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                }
            }
        }
    }

    // Create Profile Dialog / Stepper
    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, age, height, weight, gender, waist, act, exp, goals, eq, days, assess ->
                onCreateProfile(name, age, height, weight, gender, waist, act, exp, goals, eq, days, assess)
                showCreateDialog = false
            }
        )
    }

    // Select Profile Dialog
    if (showSelectDialog) {
        AlertDialog(
            onDismissRequest = { showSelectDialog = false },
            title = {
                Text(
                    text = "انتخاب پروفایل",
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
                    profiles.forEach { p ->
                        LuxuryCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            onClick = {
                                onSelectProfile(p.id)
                                showSelectDialog = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = IceBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = p.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = FrostWhite,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "سن: ${p.age} سال • سطح: ${p.experienceLevel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedSlate
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = IceBlueLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = "بازیابی اطلاعات پشتیبان",
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "محتوای JSON نسخه پشتیبان (kalisten-backup-v1) را در کادر زیر وارد کنید:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = {
                            restoreJsonText = it
                            restoreError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = { Text("متن پشتیبان...", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )

                    if (restoreError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = restoreError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = CautionYellow
                        )
                    }

                    if (restoreSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اطلاعات با موفقیت بازیابی شد.",
                            style = MaterialTheme.typography.labelMedium,
                            color = IceBluePrimary
                        )
                    }
                }
            },
            confirmButton = {
                IceButton(
                    text = "اعتبارسنجی و بازیابی",
                    onClick = {
                        val success = onRestoreBackup(restoreJsonText)
                        if (success) {
                            restoreSuccess = true
                            restoreError = null
                            showRestoreDialog = false
                        } else {
                            restoreError = "فرمت فایل یا داده‌های پشتیبان نامعتبر است."
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        gender: String,
        waistCm: Float?,
        activityLevel: String,
        experienceLevel: String,
        goals: List<String>,
        equipment: List<String>,
        trainingDays: Int,
        assessment: AssessmentEntity?
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Bio, 2: Training & Equipment, 3: Assessment (Optional)

    // Step 1: Bio
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("PREFER_NOT_TO_SAY") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Step 2: Experience & Equipment
    var expLevel by remember { mutableStateOf("BEGINNER") }
    var trainingDays by remember { mutableIntStateOf(3) }
    val selectedEquipment = remember { mutableListOf<String>() }
    var pullupBarAvailable by remember { mutableStateOf(false) }
    var parallelBarsAvailable by remember { mutableStateOf(false) }
    var ringsAvailable by remember { mutableStateOf(false) }

    // Step 3: Assessment
    var pushupsMaxStr by remember { mutableStateOf("") }
    var pullupsMaxStr by remember { mutableStateOf("") }
    var squatsMaxStr by remember { mutableStateOf("") }
    var plankSecStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = when (step) {
                        1 -> "مشخصات پایه ورزشکار (گام ۱ از ۳)"
                        2 -> "اهداف و امکانات تمرینی (گام ۲ از ۳)"
                        else -> "ارزیابی اولیه رکوردها (اختیاری - گام ۳ از ۳)"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = FrostWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اطلاعات به طور کاملاً محلی و امن در حافظه دستگاه شما ذخیره می‌شود.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (step == 1) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام یا نام مستعار") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it.filter { char -> char.isDigit() } },
                        label = { Text("سن (سال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_profile_age"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )

                    // Under 18 notice
                    val parsedAge = ageStr.toIntOrNull() ?: 20
                    if (parsedAge in 1..17) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نکته ایمنی: برای افراد زیر ۱۸ سال سیستم بر سلامت، تقویت اصولی و تعادل غذایی تمرکز خواهد داشت.",
                            style = MaterialTheme.typography.labelSmall,
                            color = CautionYellow
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text("قد (سانتی‌متر)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_profile_height"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = FrostWhite,
                                unfocusedTextColor = FrostWhite,
                                focusedBorderColor = IceBluePrimary,
                                unfocusedBorderColor = Obsidian700
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("وزن (کیلوگرم)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_profile_weight"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = FrostWhite,
                                unfocusedTextColor = FrostWhite,
                                focusedBorderColor = IceBluePrimary,
                                unfocusedBorderColor = Obsidian700
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "جنسیت (اختیاری جهت محاسبات پایه):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSlate
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == "MALE",
                            onClick = { gender = "MALE" },
                            colors = RadioButtonDefaults.colors(selectedColor = IceBluePrimary)
                        )
                        Text("مرد", color = FrostWhite, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = gender == "FEMALE",
                            onClick = { gender = "FEMALE" },
                            colors = RadioButtonDefaults.colors(selectedColor = IceBluePrimary)
                        )
                        Text("زن", color = FrostWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (step == 2) {
                    Text(
                        text = "سطح تجربه در تمرینات کالیستنیکس:",
                        style = MaterialTheme.typography.labelMedium,
                        color = FrostWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf(
                            "BEGINNER" to "مبتدی",
                            "INTERMEDIATE" to "متوسط",
                            "ADVANCED" to "پیشرفته"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = expLevel == key,
                                onClick = { expLevel = key },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IceBluePrimary,
                                    selectedLabelColor = Obsidian950
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "تعداد روزهای تمرین در هفته: $trainingDays روز",
                        style = MaterialTheme.typography.labelMedium,
                        color = FrostWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (2..5).forEach { days ->
                            FilterChip(
                                selected = trainingDays == days,
                                onClick = { trainingDays = days },
                                label = { Text("$days روز") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IceBluePrimary,
                                    selectedLabelColor = Obsidian950
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "تجهیزات در دسترس:",
                        style = MaterialTheme.typography.labelMedium,
                        color = FrostWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = pullupBarAvailable,
                            onClick = { pullupBarAvailable = !pullupBarAvailable },
                            label = { Text("میله بارفیکس (Pull-up Bar)") }
                        )
                        FilterChip(
                            selected = parallelBarsAvailable,
                            onClick = { parallelBarsAvailable = !parallelBarsAvailable },
                            label = { Text("میله پارالل یا پارالتس (Parallettes)") }
                        )
                        FilterChip(
                            selected = ringsAvailable,
                            onClick = { ringsAvailable = !ringsAvailable },
                            label = { Text("حلقه ژیمناستیک (Gymnastic Rings)") }
                        )
                    }
                } else {
                    Text(
                        text = "اگر رکوردهای فعلی خود را می‌دانید وارد کنید (می‌توانید خالی بگذارید):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pushupsMaxStr,
                        onValueChange = { pushupsMaxStr = it },
                        label = { Text("حداکثر شنا متوالی (تکرار)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        value = pullupsMaxStr,
                        onValueChange = { pullupsMaxStr = it },
                        label = { Text("حداکثر بارفیکس متوالی (تکرار)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        value = squatsMaxStr,
                        onValueChange = { squatsMaxStr = it },
                        label = { Text("حداکثر اسکات متوالی (تکرار)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        value = plankSecStr,
                        onValueChange = { plankSecStr = it },
                        label = { Text("رکورد پلانک روی ساعد (ثانیه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = CautionYellow
                    )
                }
            }
        },
        confirmButton = {
            IceButton(
                text = if (step < 3) "مرحله بعد" else "ثبت نهایی و ورود",
                onClick = {
                    if (step == 1) {
                        if (name.isBlank()) {
                            errorText = "لطفاً نام یا نام مستعار را وارد کنید."
                            return@IceButton
                        }
                        val age = ageStr.toIntOrNull()
                        if (age == null || age !in 8..110) {
                            errorText = "لطفاً سن معتبر وارد کنید."
                            return@IceButton
                        }
                        val h = heightStr.toFloatOrNull()
                        val w = weightStr.toFloatOrNull()
                        if (h == null || h !in 60f..250f || w == null || w !in 20f..300f) {
                            errorText = "لطفاً قد و وزن معتبر وارد کنید."
                            return@IceButton
                        }
                        errorText = null
                        step = 2
                    } else if (step == 2) {
                        step = 3
                    } else {
                        val eqList = mutableListOf<String>()
                        if (pullupBarAvailable) eqList.add("pullup_bar")
                        if (parallelBarsAvailable) eqList.add("parallettes")
                        if (ringsAvailable) eqList.add("rings")

                        val assess = AssessmentEntity(
                            id = "",
                            profileId = "",
                            pushupsMax = pushupsMaxStr.toIntOrNull() ?: 0,
                            pullupsMax = pullupsMaxStr.toIntOrNull() ?: 0,
                            squatsMax = squatsMaxStr.toIntOrNull() ?: 0,
                            plankSec = plankSecStr.toIntOrNull() ?: 0
                        )

                        onConfirm(
                            name.trim(),
                            ageStr.toInt(),
                            heightStr.toFloat(),
                            weightStr.toFloat(),
                            gender,
                            null,
                            "MODERATE",
                            expLevel,
                            listOf("STRENGTH", "CALISTHENICS_SKILLS"),
                            eqList,
                            trainingDays,
                            assess
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                testTag = "create_profile_next_btn"
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (step > 1) {
                        step -= 1
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (step > 1) "مرحله قبل" else "انصراف", color = MutedSlate)
            }
        },
        containerColor = Obsidian900
    )
}
