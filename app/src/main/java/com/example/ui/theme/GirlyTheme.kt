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
    FLORAL_GARDEN("گل‌گلی لطیف", "🌷", Color(0xFFB85C8A), Color(0xFFE7A8C2), Color(0xFFFFF4F8), Color.White, Color(0xFF8E3B67), Color(0xFFE7C66B), Brush.verticalGradient(listOf(Color(0xFFFFF4F8), Color(0xFFFCE2EC)))),
    NAVY_BLUE("آبی و سرمه‌ای", "🌌", Color(0xFF315B91), Color(0xFF6D93C4), Color(0xFFEAF1FA), Color.White, Color(0xFF24436B), Color(0xFFE5C56B), Brush.verticalGradient(listOf(Color(0xFFEAF1FA), Color(0xFFD8E5F5)))),
    BLUE_GRAY("آبی و خاکستری", "🩵", Color(0xFF527A9A), Color(0xFF91AFC2), Color(0xFFF0F4F7), Color.White, Color(0xFF3E6079), Color(0xFFC6D4DE), Brush.verticalGradient(listOf(Color(0xFFF0F4F7), Color(0xFFDCE7EE)))),
    GREEN_BLACK("سبز و مشکی", "🌿", Color(0xFF2F855A), Color(0xFF68B984), Color(0xFFEAF4ED), Color.White, Color(0xFF205B3D), Color(0xFFD2B66D), Brush.verticalGradient(listOf(Color(0xFFEAF4ED), Color(0xFFD5E9DB)))),
    SPORT_MODERN("اسپرت مدرن", "⚡", Color(0xFF1769AA), Color(0xFFFF8A3D), Color(0xFFF0F5FA), Color.White, Color(0xFF104B7A), Color(0xFFFFB45B), Brush.verticalGradient(listOf(Color(0xFFF0F5FA), Color(0xFFDCEAF7)))),
    SPACE_TECH("فضایی و تکنولوژی", "🚀", Color(0xFF6155A6), Color(0xFF31B7C8), Color(0xFFEEF0FA), Color.White, Color(0xFF3D347B), Color(0xFFFFD166), Brush.verticalGradient(listOf(Color(0xFFEEF0FA), Color(0xFFDADDF3)))),
    BLACK_GOLD("سیاه و طلایی لوکس", "🖤", Color(0xFFD4AF37), Color(0xFFB9942E), Color(0xFF171717), Color(0xFF252525), Color(0xFF8F7424), Color(0xFFE7C65B), Brush.verticalGradient(listOf(Color(0xFF111111), Color(0xFF29251A)))),
    WHITE_GOLD("سفید و طلایی", "✨", Color(0xFFB28A2E), Color(0xFFD6B65E), Color(0xFFFFFCF3), Color.White, Color(0xFF8B6A20), Color(0xFFE4C56A), Brush.verticalGradient(listOf(Color(0xFFFFFCF3), Color(0xFFF5EBD0)))),
    ROYAL_PURPLE("بنفش سلطنتی", "💜", Color(0xFF6D3BA8), Color(0xFFA77AD1), Color(0xFFF5EFFB), Color.White, Color(0xFF4D287A), Color(0xFFE5C56B), Brush.verticalGradient(listOf(Color(0xFFF5EFFB), Color(0xFFE4D6F3)))),
    NIGHT_MODE("شب آرام", "🌙", Color(0xFF8796D8), Color(0xFF6574B8), Color(0xFF171B2A), Color(0xFF242A3D), Color(0xFF4C5A9A), Color(0xFFE5C56B), Brush.verticalGradient(listOf(Color(0xFF171B2A), Color(0xFF252B40)))),
    LIGHT_MODE("روشن", "☀️", Color(0xFF5576A8), Color(0xFF8EAAD0), Color(0xFFF6F8FC), Color.White, Color(0xFF3D5C8B), Color(0xFFE5C56B), Brush.verticalGradient(listOf(Color(0xFFF6F8FC), Color(0xFFEAF0F8)))),
    DARK_MODE("تاریک", "🌑", Color(0xFF9A8BD0), Color(0xFF7465AA), Color(0xFF181818), Color(0xFF292929), Color(0xFF5A4D8C), Color(0xFFE5C56B), Brush.verticalGradient(listOf(Color(0xFF181818), Color(0xFF303030))))
}

enum class ZahraFontStyle(val titleFa: String, val sampleText: String) {
    SYSTEM_SANS("Sans Serif — سیستمی", "BH Personal Academy"),
    SERIF("Serif — کلاسیک", "BH Personal Academy"),
    CURSIVE("Cursive — دست‌نویس", "BH Personal Academy")
}

data class ZahraThemeState(
    val currentTheme: ZahraThemeMode = ZahraThemeMode.SWEET_SAKURA_PUFFY,
    val fontStyle: ZahraFontStyle = ZahraFontStyle.SYSTEM_SANS,
    val learnerName: String = "زهرا",
    val learnerAvatar: String = "👑",
    val learnerBio: String = "پرنسس خودسازی و تسلط بر زبان انگلیسی 🌸",
    val dailyGoalMinutes: Int = 30
)

val LocalZahraTheme = compositionLocalOf { ZahraThemeState() }
