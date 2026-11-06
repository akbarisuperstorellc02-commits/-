package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PuffyBadge
import com.example.ui.components.PuffyButton
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onEnterApp: () -> Unit
) {
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        delay(1400)
        onEnterApp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0A0C),
                        Color(0xFF131317),
                        Color(0xFF08080A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Royal Gold Monogram Emblem
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141418))
                    .border(
                        2.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFFFDF73),
                                Color(0xFFD4AF37),
                                Color(0xFF8A6508)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.zahra_gold_logo_1793948682133),
                    contentDescription = "Zahra Self Development Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main App Name in Persian
            Text(
                text = "خودسازی",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFDF73),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // English Official Name
            Text(
                text = "ZAHRA SELF DEVELOPMENT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4AF37),
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Academy Badge
            PuffyBadge(
                text = "BH Exclusive Academy",
                emoji = "👑",
                backgroundColor = Color(0xFF242014),
                textColor = Color(0xFFFFDF73)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Official Slogan
            Text(
                text = "«از صفر شروع کن، تا تسلط کامل ادامه بده!»",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEEEEEE),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            PuffyButton(
                onClick = onEnterApp,
                text = "ورود به آکادمی اختصاصی BH",
                emoji = "✨",
                height = 50.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
