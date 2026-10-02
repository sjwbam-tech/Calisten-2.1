package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PersianTopBar
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.IceBluePrimary
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.SafeGreen

@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    val expandedSections = remember {
        mutableStateMapOf<String, Boolean>().apply {
            // First section open by default
            put("about", true)
        }
    }

    fun toggleSection(key: String) {
        expandedSections[key] = !(expandedSections[key] ?: false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian950)
            .testTag("help_screen")
    ) {
        PersianTopBar(
            title = "راهنمای جامع کالیستن",
            subtitle = "مستندات کامل سیستم تمرینی، بیومکانیک و کارکرد آفلاین",
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "دسته‌بندی موضوعات راهنما",
                style = MaterialTheme.typography.titleMedium,
                color = FrostWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 1. What Calisten is
            ExpandableHelpCard(
                id = "about",
                title = "۱. کالیستن چیست؟ (فلسفه و اهداف)",
                icon = Icons.Default.Info,
                isExpanded = expandedSections["about"] == true,
                onToggle = { toggleSection("about") }
            ) {
                Text(
                    text = "کالیستن یک پلتفرم جامع، علمی و کاملاً آفلاین برای تمرینات وزن بدن (Calisthenics) است. این برنامه بدون نیاز به اشتراک ابری یا هوش مصنوعی سرورمحور، با استفاده از فرمول‌های استاندارد فیزیولوژی ورزشی و نمودارهای بیومکانیکی مفاصل، ساختار تمرینی شما را متناسب با ظرفیت بدنی‌تان طراحی و هدایت می‌کند.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. How workout generation works
            ExpandableHelpCard(
                id = "generation",
                title = "۲. نحوه تولید و زمان‌بندی برنامه تمرینی",
                icon = Icons.Default.Speed,
                isExpanded = expandedSections["generation"] == true,
                onToggle = { toggleSection("generation") }
            ) {
                Text(
                    text = "موتور برنامه‌ریزی داده‌محور (Planning Engine) با ترکیب ارزیابی اولیه، روزهای انتخابی و سطح خستگی، ساختار تمرینی را تولید می‌کند:\n\n" +
                            "• تفکیک حرکات به الگوهای کشش، فشار، پا و هسته بدن\n" +
                            "• اعمال حجم بهینه ست‌ها جهت تحریک هیپرتروفی و قدرت بدون خستگی مفرط\n" +
                            "• زمان‌بندی هوشمند استراحت میان عضلات همکار (حداقل ۴۸ ساعت)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. User profile and personalization
            ExpandableHelpCard(
                id = "profile",
                title = "۳. پروفایل، اهداف و شخصی‌سازی",
                icon = Icons.Default.Person,
                isExpanded = expandedSections["profile"] == true,
                onToggle = { toggleSection("profile") }
            ) {
                Text(
                    text = "برنامه بر اساس پارامترهای اختصاصی شما تنظیم می‌شود:\n\n" +
                            "• اهداف تمرینی: قدرت خالص، هایپرتروفی، یادگیری مهارت یا استقامت عضلانی\n" +
                            "• سطح تجربه: مبتدی (تثبیت الگوهای پایه)، متوسط (افزایش بار تدریجی) یا پیشرفته (تمرکز روی لورهای پیشرفته)\n" +
                            "• تجهیزات در دسترس: بدون وسیله (زمین)، میله بارفیکس، پارالل، کش مقاومتی یا وزنه‌های مچ\n" +
                            "• روزهای تمرینی: ۱ تا ۶ روز در هفته با چینش هوشمند فول بادی، بالاتنه/پایین‌تنه یا پوش/پول/لگز\n" +
                            "• زمان جلسه: انتخاب بازه ۳۰ الی ۹۰ دقیقه جهت مدیریت استراحت‌ها و تعداد کل ست‌ها",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Exercise Library & Details
            ExpandableHelpCard(
                id = "library",
                title = "۴. کتابخانه حرکات و جزئیات بیومکانیکی",
                icon = Icons.Default.MenuBook,
                isExpanded = expandedSections["library"] == true,
                onToggle = { toggleSection("library") }
            ) {
                Text(
                    text = "بانک جامع اطلاعاتی شامل بیش از ۲۵۰ حرکت کالیستنیکس همراه با:\n\n" +
                            "• عضلات اصلی و ثانویه درگیر\n" +
                            "• درجه سختی و سطح مهارت (تیر ۱ تا تیر ۶)\n" +
                            "• فاکتورهای ریسک و بارگذاری روی مفاصل حساس (شانه، مچ دست، آرنج)\n" +
                            "• دستورالعمل فارسی اجرا، نکات مربی‌گری (Cues) و خطاهای متداول",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Progression & Regression
            ExpandableHelpCard(
                id = "progression",
                title = "۵. زنجیره پیشرفت (Progression) و تعدیل (Regression)",
                icon = Icons.Default.FitnessCenter,
                isExpanded = expandedSections["progression"] == true,
                onToggle = { toggleSection("progression") }
            ) {
                Text(
                    text = "در کالیستنیکس وزن وزنه تغییر نمی‌کند؛ بلکه اهرم بدن (Lever Arm) تغییر می‌کند:\n\n" +
                            "• پیشرفت: انتقال از شنا با شیب مثبت ➔ شنا روی زمین ➔ شنا الماسی ➔ شنا مایل (Pseudo Planche Pushup)\n" +
                            "• تعدیل: در صورت خستگی یا درد مفصل، حرکت با تغییر زاویه به نسخه ایمن‌تر و سبک‌تر رگرس می‌شود بدون اینکه ست حذف گردد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Skill Progression (Front Lever, Planche, etc.)
            ExpandableHelpCard(
                id = "skills",
                title = "۶. مهارت‌های شاخص کالیستنیکس",
                icon = Icons.Default.SelfImprovement,
                isExpanded = expandedSections["skills"] == true,
                onToggle = { toggleSection("skills") }
            ) {
                Column {
                    SkillDetailItem("فرانت لور (Front Lever)", "مهارت کششی ایزومتریک با تکیه بر قدرت لتیسیموس و هسته بدن. نیازمند حداقل ۸ بارفیکس کامل و ۳۰ ثانیه آویزان ماندن اکتیو.")
                    SkillDetailItem("پلانچ (Planche)", "اوج قدرت فشاری شانه قدامی و سراتوس. پیش‌نیاز: شنا الماسی پر تعداد و هالو بادی عمیق.")
                    SkillDetailItem("ماسل‌آپ (Muscle-Up)", "ترکیب کشش انفجاری و دیپ بر روی میله بارفیکس. نیازمند ۱۰ الی ۱۲ بارفیکس سرعتی تا سینه.")
                    SkillDetailItem("پرچم انسانی (Human Flag)", "ایزومتریک لترال تنه با فشار همزمان کشش و فشار دست‌ها بر روی میله عمودی.")
                    SkillDetailItem("هنداستند / بالانس (Handstand)", "تعادل روی دست‌ها با پایداری کامل تیغه‌های کتف و کشش مچ دست.")
                    SkillDetailItem("بک لور (Back Lever)", "کشش کپسول قدامی شانه در وضعیت اکستنشن کامل با انقباض باسن و هسته بدن.")
                    SkillDetailItem("ال‌سیت (L-Sit)", "پایداری اسکاپولا در وضعیت دپرشن همزمان با فلکشن ۹۰ درجه لگن.")
                    SkillDetailItem("وی‌سیت (V-Sit)", "توسعه پیشرفته ال‌سیت با انقباض شدیدتر عضلات شکم و پایداری شانه‌ها.")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Workout History, Progress & Replacement
            ExpandableHelpCard(
                id = "history",
                title = "۷. تاریخچه، ثبت ست‌ها، جایگزینی و پلاتو",
                icon = Icons.Default.History,
                isExpanded = expandedSections["history"] == true,
                onToggle = { toggleSection("history") }
            ) {
                Text(
                    text = "• ثبت دقیق ست‌ها، تکرارها، زمان مکث و مقیاس سختی RPE\n" +
                            "• جایگزینی حرکت: با نگه داشتن انگشت روی هر حرکت، می‌توانید از بین حرکات هم‌الگو با بار شانه مشابه جایگزین انتخاب کنید.\n" +
                            "• تشخیص پلاتو (Plateau): در صورت استپ در تکرارها طی ۳ جلسه، موتور تحلیل پلاتو پیشنهاد تغییر تمپو، مکث، یا دی‌لود یک‌هفته‌ای ارائه می‌دهد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 8. Offline Mode & Data Package
            ExpandableHelpCard(
                id = "offline",
                title = "۸. عملکرد آفلاین و بسته داده محلی",
                icon = Icons.Default.WifiOff,
                isExpanded = expandedSections["offline"] == true,
                onToggle = { toggleSection("offline") }
            ) {
                Column {
                    Text(
                        text = "کالیستن بر اساس فلسفه اولویت ۱۰۰٪ آفلاین (Offline-First) ساخته شده است:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FrostWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🟢 مواردی که ۱۰۰٪ آفلاین کار می‌کنند:\n" +
                                "• تولید برنامه‌های تمرینی هفتگی و شخصی‌سازی کامل\n" +
                                "• تمامی محاسبات اضافه بار تدریجی، RPE و پلاتو\n" +
                                "• کتابخانه کامل ۲۵۰+ حرکت همراه جزئیات و نکات بیومکانیکی\n" +
                                "• ثبت تمرینات، تاریخچه، گزارش ریکاوری و رکوردهای شخصی\n" +
                                "• نقشه پیشرفت مهارت‌ها و بررسی پیش‌نیازها\n" +
                                "• خروجی و ورود فایل پشتیبان استاندارد\n\n" +
                                "🌐 تنها مواردی که به اتصال شبکه وابسته هستند:\n" +
                                "• دانلود یکباره بسته جامع آفلاین (اختیاری جهت بروزرسانی منابع محلی)\n" +
                                "• ذخیره ابری اختیاری فایل پشتیبان با حساب کاربری",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ExpandableHelpCard(
    id: String,
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    LuxuryCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("help_card_$id"),
        backgroundColor = Obsidian900
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = IceBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = FrostWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MutedSlate,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(
                        color = GlassCardBorderSubtle,
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun SkillDetailItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodyMedium,
            color = IceBluePrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlate,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 4.dp),
            lineHeight = 20.sp
        )
    }
}
