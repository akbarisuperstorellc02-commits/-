package com.example.ui.screens

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.ui.theme.LocalZahraTheme
import com.example.util.SelfCareAlarm
import java.util.Locale

@Composable
fun SelfCareAlarmScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val theme = LocalZahraTheme.current.currentTheme
    val initial = remember { SelfCareAlarm.getTime(context) }
    var hour by remember { mutableStateOf(initial.first) }
    var minute by remember { mutableStateOf(initial.second) }
    var enabled by remember { mutableStateOf(SelfCareAlarm.isEnabled(context)) }
    val timeText = String.format(Locale.US, "%02d:%02d", hour, minute)

    Column(Modifier.fillMaxSize().background(theme.gradientBrush).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = theme.buttonBevel) }
            Text("زنگ یادآور خودسازی", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = theme.buttonBevel)
        }
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxWidth().background(theme.cardBackground, RoundedCornerShape(28.dp)).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = theme.primaryColor)
            Spacer(Modifier.height(12.dp))
            Text("زمان مخصوص رشد و پیشرفت تو", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = theme.buttonBevel)
            Spacer(Modifier.height(18.dp))
            Text(timeText, fontSize = 48.sp, fontWeight = FontWeight.Black, color = theme.primaryColor)
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, true).show()
            }, colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)) {
                Text("انتخاب ساعت")
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (enabled) "زنگ فعال است" else "زنگ خاموش است", fontWeight = FontWeight.Bold, color = theme.buttonBevel)
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Spacer(Modifier.height(18.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        if (!am.canScheduleExactAlarms()) {
                            try { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + context.packageName))) }
                            catch (_: Exception) { Toast.makeText(context, "اجازه زنگ دقیق را از تنظیمات برنامه فعال کن.", Toast.LENGTH_LONG).show() }
                            return@Button
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        if (context is Activity) ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7319)
                    }
                    SelfCareAlarm.set(context, hour, minute, enabled)
                    Toast.makeText(context, if (enabled) "زنگ روزانه برای $timeText تنظیم شد." else "زنگ یادآور خاموش شد.", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
            ) { Text("ذخیره تنظیمات زنگ") }
            Spacer(Modifier.height(10.dp))
            Text("این یادآور هر روز در ساعت تعیین‌شده اعلان و صدای زنگ می‌فرستد. برای عملکرد دقیق، اجازهٔ زنگ و اعلان را فعال کن.", fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
