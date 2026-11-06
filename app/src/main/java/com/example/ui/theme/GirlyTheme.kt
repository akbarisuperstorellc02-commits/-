package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Puffy Girly Pastel Palette Constants
val PuffyPinkPrimary = Color(0xFFFF6584)
val PuffyPinkSecondary = Color(0xFFFF85A1)
val PuffyPinkBevel = Color(0xFFD94565)
val PuffyBgGradientTop = Color(0xFFFFF0F5)
val PuffyBgGradientBottom = Color(0xFFFFDEE9)
val PuffyCardBg = Color(0xFFFFFFFF)
val PuffyGoldAccent = Color(0xFFFFD166)
val PuffyTextDark = Color(0xFF2D2529)
val PuffyTextMuted = Color(0xFF7A6B72)

enum class ZahraThemeMode(
    val titleFa: String,
    val iconEmoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val cardBackground: Color,
    val buttonBevel: Color,
    val accentGold: Color,
    val gradientBrush: Brush
) {
    SWEET_SAKURA_PUFFY(
        titleFa = "شکوفه صورتی پفکی (اصلی)",
        iconEmoji = "🌸",
        primaryColor = Color(0xFFFF6584),
        secondaryColor = Color(0xFFFF85A1),
        backgroundColor = Color(0xFFFFF0F5),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFFD94565),
        accentGold = Color(0xFFFFD166),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFFFF0F5), Color(0xFFFFDFEA), Color(0xFFFFE8F0))
        )
    ),
    STRAWBERRY_CREAM(
        titleFa = "شیر توت‌فرنگی خامه‌ای",
        iconEmoji = "🍓",
        primaryColor = Color(0xFFFF4D6D),
        secondaryColor = Color(0xFFFF758F),
        backgroundColor = Color(0xFFFFF5F7),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFFD92D50),
        accentGold = Color(0xFFFFB703),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFFFF5F7), Color(0xFFFFCCD5), Color(0xFFFFE3E8))
        )
    ),
    LAVENDER_PRINCESS(
        titleFa = "پرنسسی یاسی و بنفش",
        iconEmoji = "💜",
        primaryColor = Color(0xFF9D4EDD),
        secondaryColor = Color(0xFFC77DFF),
        backgroundColor = Color(0xFFF9F3FC),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFF7B2CBF),
        accentGold = Color(0xFFFFD166),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFF9F3FC), Color(0xFFEADBFC), Color(0xFFF4EBFF))
        )
    ),
    COTTON_CANDY_CLOUD(
        titleFa = "پشمک ابری صورتی و آبی",
        iconEmoji = "🍬",
        primaryColor = Color(0xFFFF69B4),
        secondaryColor = Color(0xFF70D6FF),
        backgroundColor = Color(0xFFF7F9FF),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFFDB5097),
        accentGold = Color(0xFFFFB703),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFFFF0F7), Color(0xFFE7F3FE), Color(0xFFFDE8F3))
        )
    ),
    ROYAL_PINK_GOLD(
        titleFa = "صورتی سلطنتی و طلای ناب",
        iconEmoji = "👑",
        primaryColor = Color(0xFFFF5C8D),
        secondaryColor = Color(0xFFD4AF37),
        backgroundColor = Color(0xFFFFF8FA),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFFC93D6B),
        accentGold = Color(0xFFD4AF37),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFFFF8FA), Color(0xFFFFE4EE), Color(0xFFFFF0F6))
        )
    ),
    OCEAN_BLUE_MASCULINE(
        titleFa = "آبی اقیانوسی (پسرانه)",
        iconEmoji = "🌊",
        primaryColor = Color(0xFF168AAD),
        secondaryColor = Color(0xFF52B6CF),
        backgroundColor = Color(0xFFEAF6FA),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFF126782),
        accentGold = Color(0xFFFFC857),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFEAF6FA), Color(0xFFD5EDF5), Color(0xFFF2FAFC))
        )
    ),
    GRAPHITE_EMERALD_MASCULINE(
        titleFa = "گرافیتی و زمردی (پسرانه)",
        iconEmoji = "🛡️",
        primaryColor = Color(0xFF20A477),
        secondaryColor = Color(0xFF5BC99A),
        backgroundColor = Color(0xFFEEF5F1),
        cardBackground = Color(0xFFFFFFFF),
        buttonBevel = Color(0xFF147653),
        accentGold = Color(0xFFD6B36A),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFFEEF5F1), Color(0xFFDDEBE3), Color(0xFFF4F8F5))
        )
    ),
    OBSIDIAN_BLACK_GOLD(
        titleFa = "مشکی و طلایی سلطنتی",
        iconEmoji = "🖤",
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFFF5D76E),
        backgroundColor = Color(0xFF100E0A),
        cardBackground = Color(0xFF1C1912),
        buttonBevel = Color(0xFF8F6B16),
        accentGold = Color(0xFFFFD700),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFF0A0907), Color(0xFF19150C), Color(0xFF272012))
        )
    )
}

enum class ZahraFontStyle(val titleFa: String, val sampleText: String) {
    FANTASY_ROUND(titleFa = "گرد و فانتزی پفکی", sampleText = "Zahra Self Development 🌸"),
    PRINCESS_CUTE(titleFa = "پرنسسی کیوت", sampleText = "Start From Zero to Z9 👑"),
    CHIC_ELEGANT(titleFa = "شیک و مدرن دخترانه", sampleText = "Zahra English Academy ✨")
}

data class ZahraThemeState(
    val currentTheme: ZahraThemeMode = ZahraThemeMode.SWEET_SAKURA_PUFFY,
    val fontStyle: ZahraFontStyle = ZahraFontStyle.FANTASY_ROUND,
    val learnerName: String = "زهرا",
    val learnerAvatar: String = "👑",
    val learnerBio: String = "پرنسس خودسازی و تسلط بر زبان انگلیسی 🌸",
    val dailyGoalMinutes: Int = 30
)

val LocalZahraTheme = compositionLocalOf { ZahraThemeState() }
