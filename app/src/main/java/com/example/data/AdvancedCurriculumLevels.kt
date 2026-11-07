package com.example.data

import com.example.model.ClassroomType
import com.example.model.Level
import com.example.model.LessonItem
import com.example.model.QuestionType
import com.example.model.QuizQuestion
import com.example.model.TierGroup

/**
 * Fifteen additional advanced stages. Original A1-Z9 IDs and content remain intact.
 * New stages continue after Z9 and use stable IDs 31-45.
 */
object AdvancedCurriculumLevels {
    val levels: List<Level> = listOf(
        advanced(31, "Z10", "تفکر انتقادی و ارزیابی ادعاها", "Critical Thinking", "evaluate", "ارزیابی کردن", "We should evaluate every claim carefully.", "باید هر ادعا را با دقت ارزیابی کنیم.", "Evaluate means to judge the quality or value of something.", "برای ارزیابی ادعا، شواهد و منبع آن را بررسی کن.", "🔎"),
        advanced(32, "Z11", "استدلال و نتیجه‌گیری منطقی", "Logical Reasoning", "therefore", "بنابراین", "The evidence is strong; therefore, the conclusion is reliable.", "شواهد قوی است؛ بنابراین نتیجه‌گیری قابل اعتماد است.", "Therefore introduces a logical result.", "Therefore برای نشان‌دادن نتیجه منطقی استفاده می‌شود.", "🧠"),
        advanced(33, "Z12", "حل مسئله و تصمیم‌گیری", "Problem Solving", "alternative", "گزینه بدیل", "We need an alternative solution.", "ما به یک راه‌حل بدیل نیاز داریم.", "Alternative describes another possible choice.", "برای تصمیم بهتر، گزینه‌های بدیل را مقایسه کن.", "🧩"),
        advanced(34, "Z13", "ارتباط حرفه‌ای و بازخورد", "Professional Communication", "constructive", "سازنده", "Thank you for your constructive feedback.", "از بازخورد سازنده شما سپاسگزارم.", "Constructive feedback helps someone improve.", "بازخورد سازنده بر رفتار و راه‌حل تمرکز می‌کند.", "💬"),
        advanced(35, "Z14", "نوشتار تحلیلی و انسجام متن", "Analytical Writing", "coherent", "منسجم", "A coherent paragraph presents connected ideas.", "یک پاراگراف منسجم ایده‌های مرتبط را بیان می‌کند.", "Coherent means clear and logically connected.", "برای انسجام متن از پیونددهنده‌های مناسب استفاده کن.", "✍️"),
        advanced(36, "Z15", "مهارت ارائه و سخنرانی", "Public Speaking", "engage", "جذب و درگیر ساختن", "A good speaker can engage the audience.", "یک سخنران خوب می‌تواند توجه مخاطبان را جلب کند.", "Engage the audience means keep listeners interested.", "برای جلب توجه، با مخاطب تماس چشمی برقرار کن.", "🎤"),
        advanced(37, "Z16", "درک مطلب و استنباط", "Reading Inference", "imply", "به‌طور غیرمستقیم رساندن", "The writer implies that change takes time.", "نویسنده به‌طور غیرمستقیم می‌رساند که تغییر زمان می‌برد.", "Imply means suggest without stating directly.", "در استنباط، از نشانه‌های متن نتیجه بگیر نه حدس بی‌پایه.", "📖"),
        advanced(38, "Z17", "شنیدار پیشرفته و تشخیص لحن", "Advanced Listening", "emphasis", "تأکید", "The speaker places emphasis on the final point.", "گوینده بر نکته پایانی تأکید می‌کند.", "Emphasis is extra importance given to a word or idea.", "به تکیه، مکث و آهنگ صدا توجه کن.", "🎧"),
        advanced(39, "Z18", "واژگان آکادمیک و کاربرد دقیق", "Academic Vocabulary", "significant", "قابل توجه و مهم", "The study found a significant difference.", "این پژوهش تفاوت قابل توجهی یافت.", "Significant means important or large enough to matter.", "واژه‌های آکادمیک را در جمله واقعی یاد بگیر.", "📚"),
        advanced(40, "Z19", "مذاکره و حل اختلاف", "Negotiation", "compromise", "سازش و توافق میانه", "Both sides agreed to a fair compromise.", "هر دو طرف با یک سازش منصفانه موافقت کردند.", "A compromise is an agreement in which each side adjusts.", "در مذاکره، خواسته‌ها و اولویت‌ها را روشن بیان کن.", "🤝"),
        advanced(41, "Z20", "گرامر پیشرفته و ساختارهای پیچیده", "Advanced Grammar", "despite", "با وجودِ", "Despite the challenge, she completed the task.", "با وجود دشواری، او کار را تکمیل کرد.", "Despite is followed by a noun or gerund.", "بعد از despite اسم یا فعل ing می‌آید، نه جمله کامل.", "🧱"),
        advanced(42, "Z21", "ترجمه مفهومی و بازنویسی", "Translation & Paraphrasing", "paraphrase", "بازنویسی با بیان دیگر", "Please paraphrase the sentence without changing its meaning.", "لطفاً جمله را بدون تغییر معنا بازنویسی کن.", "Paraphrase means express the same idea in different words.", "در بازنویسی، معنا را نگه دار و ساختار را تغییر بده.", "🌐"),
        advanced(43, "Z22", "پژوهش و سواد اطلاعاتی", "Research Literacy", "credible", "قابل اعتماد", "Use credible sources for your research.", "برای پژوهش خود از منابع قابل اعتماد استفاده کن.", "Credible sources have trustworthy evidence and authorship.", "اعتبار نویسنده، تاریخ و شواهد منبع را بررسی کن.", "🔬"),
        advanced(44, "Z23", "رهبری و همکاری گروهی", "Leadership & Teamwork", "delegate", "واگذار کردن مسئولیت", "Effective leaders delegate tasks fairly.", "رهبران مؤثر مسئولیت‌ها را منصفانه واگذار می‌کنند.", "Delegate means assign a task to another person.", "واگذاری درست کار، مسئولیت و نتیجه مورد انتظار را روشن می‌کند.", "🌟"),
        advanced(45, "Z24", "تسلط نهایی و ارتباط مستقل", "Independent Mastery", "articulate", "روشن و شیوا بیان کردن", "She can articulate complex ideas with confidence.", "او می‌تواند ایده‌های پیچیده را با اعتمادبه‌نفس و روشنی بیان کند.", "Articulate means express ideas clearly and effectively.", "تسلط یعنی بتوانی مفهوم را روشن، دقیق و مستقل منتقل کنی.", "👑")
    )

    private fun advanced(
        id: Int,
        code: String,
        title: String,
        titleEn: String,
        word: String,
        meaning: String,
        example: String,
        exampleFa: String,
        definition: String,
        tip: String,
        emoji: String
    ) = Level(
        id = id,
        levelCode = code,
        tierGroup = TierGroup.TIER_Z,
        titleFa = "سطح $code: $title",
        titleEn = "$code: $titleEn",
        iconEmoji = emoji,
        descriptionFa = "مرحله پیشرفته برای تقویت مهارت مستقل زبان انگلیسی",
        accentColorHex = 0xFF9D4EDD,
        items = listOf(
            LessonItem("${code.lowercase()}_1", word, "", meaning, example, exampleFa, emoji, ClassroomType.VOCABULARY, code, tip),
            LessonItem("${code.lowercase()}_2", "Use $word in context", "", "واژه را در بافت جمله به کار ببر", example, exampleFa, "📝", ClassroomType.READING_WRITING, code, definition),
            LessonItem("${code.lowercase()}_3", "Explain the idea clearly", "", "مفهوم را به روشنی توضیح بده", "I can explain this idea in my own words.", "می‌توانم این مفهوم را با واژه‌های خودم توضیح بدهم.", "💡", ClassroomType.CONVERSATION, code, "برای توضیح روشن، جمله‌های کوتاه و پیوند منطقی ایده‌ها را تمرین کن.")
        ),
        examQuestions = listOf(
            QuizQuestion("${code.lowercase()}_q1", QuestionType.MULTIPLE_CHOICE, "What does '$word' mean?", "معنی درست واژه را انتخاب کن: $word", listOf(meaning, "فراموش کردن", "مخالفت کردن", "پنهان ساختن"), 0, "معنی درست: $meaning"),
            QuizQuestion("${code.lowercase()}_q2", QuestionType.TRANSLATION, "Choose the correct meaning of the example.", "معنی درست جمله را انتخاب کن: $example", listOf(exampleFa, "او فردا به خانه می‌رود.", "این کتاب بسیار قدیمی است.", "من هنوز تصمیم نگرفته‌ام."), 0, "ترجمه درست: $exampleFa"),
            QuizQuestion("${code.lowercase()}_q3", QuestionType.GRAMMAR_FILL, "Complete: The idea is ____.", "جمله را با واژه مناسب کامل کن.", listOf("clear", "clearly", "clearness", "clearing"), 0, "پس از is در این ساختار صفت می‌آید."),
            QuizQuestion("${code.lowercase()}_q4", QuestionType.SPEAKING, "Read aloud: $example", "جمله را با صدای بلند و واضح بخوان.", listOf(example, "I am ready to learn.", "Could you repeat that?", "Practice every day."), 0, "جمله هدف را با تلفظ روشن تمرین کن.", example),
            QuizQuestion("${code.lowercase()}_q5", QuestionType.LISTENING, "Listen and identify the key word: $word", "واژه کلیدی را پس از شنیدن تشخیص بده: $word", listOf(word, "yesterday", "beautiful", "between"), 0, "واژه کلیدی این تمرین: $word")
        )
    )
}
