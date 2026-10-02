package com.example.ui.screens.nutrition

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NutritionEntryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.ui.components.IceButton
import com.example.ui.components.IceOutlineButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.components.StatBadge
import com.example.ui.theme.CautionYellow
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceBlueLight
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian700
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950

@Composable
fun NutritionScreen(
    profile: ProfileEntity,
    entries: List<NutritionEntryEntity>,
    onLogNutrition: (
        foodName: String,
        mealType: String,
        calories: Int,
        proteinG: Float,
        carbsG: Float,
        fatG: Float,
        waterMl: Int,
        notes: String
    ) -> Unit,
    onDeleteNutrition: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddMealDialog by remember { mutableStateOf(false) }

    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.map { it.proteinG }.sum()
    val totalWaterMl = entries.sumOf { it.waterMl }

    val isMinor = profile.age < 18

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "تغذیه، سوخت‌رسانی و آب",
            subtitle = if (isMinor) "طرح سلامت‌محور متناسب با سن رشد" else "توازن ماکروها برای هایپرتروفی و قدرت",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { showAddMealDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer)
                        .testTag("add_meal_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ثبت وعده",
                        tint = IceBluePrimary
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Minor Protection Banner
            if (isMinor) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Obsidian900)
                        .border(1.dp, CautionYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CautionYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "راهنمای سلامت‌محور نوجوانان (زیر ۱۸ سال)",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تمرکز این بخش صرفاً بر دریافت پروتئین باکیفیت برای ترمیم بافت، انرژی کافی برای فعالیت و هیدراتاسیون مناسب است. از اعمال رژیم‌های محدودکننده شدید کالری یا اهداف کاهش وزن تهاجمی خودداری می‌شود.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Daily Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(
                    title = "پروتئین دریافتی",
                    value = String.format("%.0f", totalProtein),
                    unit = "g",
                    highlightColor = IceBluePrimary
                )
                StatBadge(
                    title = "انرژی مصرفی",
                    value = "$totalCalories",
                    unit = "kcal",
                    highlightColor = FrostWhite
                )
                StatBadge(
                    title = "آب مصرفی",
                    value = "$totalWaterMl",
                    unit = "ml",
                    highlightColor = IceBlueLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Water Add Bar
            LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = IceBlueLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ثبت سریع نوشیدن آب",
                                style = MaterialTheme.typography.titleMedium,
                                color = FrostWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "هیدراتاسیون کافی برای الاستیسیته تاندون‌ها حیاتی است",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IceOutlineButton(
                            text = "+۲۵۰ml",
                            onClick = {
                                onLogNutrition("آب", "WATER", 0, 0f, 0f, 0f, 250, "")
                            },
                            modifier = Modifier.height(36.dp)
                        )
                        IceOutlineButton(
                            text = "+۵۰۰ml",
                            onClick = {
                                onLogNutrition("آب", "WATER", 0, 0f, 0f, 0f, 500, "")
                            },
                            modifier = Modifier.height(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Entries List
            Text(
                text = "وعده‌ها و موارد ثبت‌شده امروز:",
                style = MaterialTheme.typography.titleLarge,
                color = FrostWhite,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (entries.isEmpty()) {
                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Obsidian900
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "امروز هنوز وعده‌ای ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate
                        )
                    }
                }
            } else {
                entries.forEach { entry ->
                    LuxuryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        backgroundColor = Obsidian900
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.foodName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FrostWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                val details = if (entry.waterMl > 0) {
                                    "${entry.waterMl} میلی‌لیتر آب"
                                } else {
                                    "${entry.calories} kcal • پروتئین: ${entry.proteinG}g • کربوهیدرات: ${entry.carbsG}g"
                                }
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }

                            IconButton(onClick = { onDeleteNutrition(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = MutedSlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
        var foodName by remember { mutableStateOf("") }
        var mealType by remember { mutableStateOf("LUNCH") }
        var caloriesStr by remember { mutableStateOf("") }
        var proteinStr by remember { mutableStateOf("") }
        var carbsStr by remember { mutableStateOf("") }
        var fatStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMealDialog = false },
            title = {
                Text(
                    text = "ثبت وعده غذایی",
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
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("نام غذا یا خوراک") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = FrostWhite,
                            unfocusedTextColor = FrostWhite,
                            focusedBorderColor = IceBluePrimary,
                            unfocusedBorderColor = Obsidian700
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "نوع وعده:", style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BREAKFAST" to "صبحانه", "LUNCH" to "ناهار", "DINNER" to "شام", "SNACK" to "میان‌وعده").forEach { (type, label) ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("پروتئین (گرم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it },
                        label = { Text("کالری تخمینی (kcal)") },
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
            },
            confirmButton = {
                IceButton(
                    text = "ذخیره وعده",
                    onClick = {
                        if (foodName.isNotBlank()) {
                            onLogNutrition(
                                foodName.trim(),
                                mealType,
                                caloriesStr.toIntOrNull() ?: 0,
                                proteinStr.toFloatOrNull() ?: 0f,
                                carbsStr.toFloatOrNull() ?: 0f,
                                fatStr.toFloatOrNull() ?: 0f,
                                0,
                                ""
                            )
                            showAddMealDialog = false
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_log_nutrition_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddMealDialog = false }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
