package com.example.data

import com.example.model.GameStage

/** Thirty additional vocabulary-game rounds, continuing the original 1-30 sequence. */
object AdvancedWordGameStages {
    private val words = listOf(
        "CURIOUS" to ("کنجکاو و مشتاق دانستن" to "🔍"),
        "EXPLORE" to ("کاوش کردن و بررسی نمودن" to "🧭"),
        "DISCOVER" to ("کشف کردن و دریافتن" to "💡"),
        "IMPROVE" to ("بهتر ساختن و پیشرفت دادن" to "📈"),
        "ACHIEVE" to ("به دست آوردن و موفق شدن" to "🏆"),
        "COURAGE" to ("شجاعت و دلیری" to "🦁"),
        "RESPECT" to ("احترام گذاشتن" to "🤝"),
        "PATIENT" to ("صبور و شکیبا" to "🌿"),
        "INSIGHT" to ("بینش و درک عمیق" to "🧠"),
        "BALANCE" to ("تعادل و توازن" to "⚖️"),
        "CREATIVE" to ("خلاق و نوآور" to "🎨"),
        "CONFIDENT" to ("بااعتمادبه‌نفس" to "✨"),
        "FOCUSED" to ("متمرکز و هدفمند" to "🎯"),
        "KINDNESS" to ("مهربانی و نیک‌رفتاری" to "💗"),
        "FLEXIBLE" to ("انعطاف‌پذیر" to "🌈"),
        "EVIDENCE" to ("شواهد و مدارک" to "🔬"),
        "RELIABLE" to ("قابل اعتماد" to "🛡️"),
        "EFFECTIVE" to ("مؤثر و نتیجه‌بخش" to "⚡"),
        "INSPIRED" to ("الهام‌گرفته" to "🌟"),
        "PERSISTENT" to ("باپشتکار و پایدار" to "🔥"),
        "ACCURATE" to ("دقیق و درست" to "📐"),
        "THOUGHTFUL" to ("با ملاحظه و سنجیده" to "💭"),
        "GENEROUS" to ("سخاوتمند" to "🎁"),
        "MOTIVATE" to ("انگیزه دادن" to "🚀"),
        "COMMUNICATE" to ("ارتباط برقرار کردن" to "🗣️"),
        "KNOWLEDGE" to ("دانش و آگاهی" to "📚"),
        "OPPORTUNITY" to ("فرصت و مجال" to "🚪"),
        "UNDERSTAND" to ("درک کردن و فهمیدن" to "💬"),
        "RESPONSIBLE" to ("مسئولیت‌پذیر" to "🧩"),
        "INDEPENDENT" to ("مستقل و خوداتکا" to "👑")
    )

    val stages: List<GameStage> = words.mapIndexed { index, entry ->
        val word = entry.first
        GameStage(
            stageNumber = index + 31,
            targetWordEn = word,
            hintFa = entry.second.first,
            emoji = entry.second.second,
            jumbledLetters = word.reversed().toList()
        )
    }
}
