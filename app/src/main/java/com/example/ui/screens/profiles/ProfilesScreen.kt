package com.example.ui.screens.profiles

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import com.example.data.local.entity.ProfileEntity
import com.example.ui.components.IceButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.screens.welcome.CreateProfileDialog
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

@Composable
fun ProfilesScreen(
    profiles: List<ProfileEntity>,
    activeProfileId: String?,
    onSwitchProfile: (String) -> Unit,
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
        trainingDays: Int
    ) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onBack: () -> Unit
) {
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<ProfileEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .padding(bottom = 80.dp)
    ) {
        PersianTopBar(
            title = "مدیریت پروفایل‌های کاربری",
            subtitle = "اطلاعات هر پروفایل به طور کاملاً مستقل نگهداری می‌شود",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { showCreateProfileDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IceBlueContainer)
                        .testTag("add_profile_top_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ساخت پروفایل جدید",
                        tint = IceBluePrimary
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(profiles) { p ->
                val isActive = p.id == activeProfileId

                LuxuryCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    borderColor = if (isActive) IceBluePrimary else GlassCardBorderSubtle,
                    onClick = { onSwitchProfile(p.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) IceBlueContainer else Obsidian800)
                                    .border(1.dp, if (isActive) IceBluePrimary else GlassCardBorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isActive) IceBluePrimary else FrostWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FrostWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(IceBlueContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "فعال",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = IceBluePrimary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "سن: ${p.age} سال • قد: ${p.heightCm.toInt()}cm • وزن: ${p.weightKg}kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate
                                )
                                Text(
                                    text = "سطح: ${p.experienceLevel} • ${p.trainingDaysPerWeek} روز در هفته",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IceBlueLight
                                )
                            }
                        }

                        // Delete button
                        IconButton(
                            onClick = { profileToDelete = p },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف پروفایل",
                                tint = MutedSlate
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateProfileDialog = false },
            onConfirm = { name, age, height, weight, gender, waist, act, exp, goals, eq, days, assess ->
                onCreateProfile(name, age, height, weight, gender, waist, act, exp, goals, eq, days)
                showCreateProfileDialog = false
            }
        )
    }

    // Safe Delete Profile Confirmation Dialog
    if (profileToDelete != null) {
        val target = profileToDelete!!
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = {
                Text(
                    text = "حذف قطعی پروفایل «${target.name}»",
                    style = MaterialTheme.typography.titleLarge,
                    color = AlertRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "آیا از حذف کامل این پروفایل اطمینان دارید؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FrostWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اطلاعات وابسته شامل تمام جلسات تمرینی، ست‌های ثبت‌شده، رکوردهای شخصی (PRs) و تاریخچه ریکاوری این کاربر به طور دائم پاک خواهند شد و سایر پروفایل‌ها تغییری نخواهند کرد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CautionYellow
                    )
                }
            },
            confirmButton = {
                IceButton(
                    text = "بله، حذف دائمی",
                    onClick = {
                        onDeleteProfile(target.id)
                        profileToDelete = null
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    testTag = "confirm_delete_profile_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("انصراف", color = MutedSlate)
                }
            },
            containerColor = Obsidian900
        )
    }
}
