package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgressManager
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyCard
import com.example.ui.theme.LocalZahraTheme
import com.example.ui.theme.ZahraFontStyle
import com.example.ui.theme.ZahraThemeMode

@Composable
fun SettingsAndThemeScreen(
    userProgressManager: UserProgressManager,
    onBack: () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val currentThemeMode by userProgressManager.currentTheme.collectAsState()
    val currentFontStyle by userProgressManager.fontStyle.collectAsState()
    val learnerAvatar by userProgressManager.learnerAvatar.collectAsState()
    val dailyGoalMinutes by userProgressManager.dailyGoalMinutes.collectAsState()

    val availableAvatars = listOf("👑", "👸🏻", "💎", "🥇", "🌟", "🏆", "📖", "✨")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.gradientBrush)
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = theme.primaryColor
                )
            }
            PuffyBadge(text = "تنظیمات اختصاصی خودسازی BH", emoji = "👑")
            Spacer(modifier = Modifier.width(36.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Study Goal Setting
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = theme.primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "هدف روزانه مطالعه:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                val isSelected = dailyGoalMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) theme.primaryColor else Color(0xFF1E1E26))
                                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(16.dp))
                                        .clickable { userProgressManager.setDailyGoal(mins) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$mins دقیقه",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Theme selector
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ColorLens, contentDescription = null, tint = theme.primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "انتخاب تم و رنگ‌بندی برنامه:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ZahraThemeMode.values().forEach { mode ->
                                val isSelected = currentThemeMode == mode
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isSelected) Color(0xFF241F10) else Color(0xFF181820))
                                        .border(
                                            1.5.dp,
                                            if (isSelected) mode.primaryColor else Color(0xFF2C2C36),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable { userProgressManager.setTheme(mode) }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = mode.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mode.titleFa,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(mode.primaryColor))
                                            Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(mode.secondaryColor))
                                            Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(mode.buttonBevel))
                                        }

                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "انتخاب شده",
                                                tint = mode.primaryColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Font Style
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FontDownload, contentDescription = null, tint = theme.primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سبک تایپوگرافی:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ZahraFontStyle.values().forEach { style ->
                                val isSelected = currentFontStyle == style
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(0xFF241F10) else Color(0xFF181820))
                                        .border(
                                            1.5.dp,
                                            if (isSelected) theme.primaryColor else Color(0xFF2C2C36),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { userProgressManager.setFontStyle(style) }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = style.titleFa,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = style.sampleText,
                                                fontSize = 11.sp,
                                                color = theme.primaryColor
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = theme.primaryColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Learner Avatar
            item {
                PuffyCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = theme.primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "آواتار دخترانه اختصاصی:",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(availableAvatars) { av ->
                                val isSel = learnerAvatar == av
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(if (isSel) theme.primaryColor else Color(0xFF1E1E26))
                                        .border(2.dp, if (isSel) Color.White else Color.Transparent, CircleShape)
                                        .clickable { userProgressManager.setAvatar(av) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = av, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
