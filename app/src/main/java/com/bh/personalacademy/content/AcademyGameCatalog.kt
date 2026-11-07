package com.bh.personalacademy.content

import com.example.academy.domain.model.AcademyLocale

enum class GameStyle {
    MEANING, SPELLING, PICTURE, MISSING_WORD, SENTENCE, ODD_ONE_OUT,
    TRANSLATION, WORD_ORDER, MATCHING, CLUE, MEMORY, SPEED,
    CROSSWORD, WORD_BUILD, MISTAKE_REVIEW, SPELLING_CHECK, VOCAB_RACE, LISTENING
}

data class GameMode(
    val id: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val style: GameStyle,
    val timed: Boolean = false
)

data class GameQuestion(
    val prompt: LocalizedText,
    val options: List<LocalizedText>,
    val correctIndex: Int,
    val answer: String,
    val hint: LocalizedText,
    val explanation: LocalizedText
)

private data class GameWord(
    val english: String,
    val fa: String,
    val ps: String,
    val ar: String,
    val emoji: String,
    val clueFa: String,
    val clueEn: String
) {
    fun meaning() = LocalizedText(english, fa, ps, ar)
    fun englishWord() = LocalizedText(english, english, english, english)
}

object AcademyGameCatalog {
    private fun t(en: String, fa: String, ps: String, ar: String) = LocalizedText(en, fa, ps, ar)

    val modes = listOf(
        GameMode("meaning", t("Guess the meaning", "حدس معنی واژه", "د کلمې مانا ومومئ", "خمن معنى الكلمة"), t("Choose the correct Dari meaning.", "معنی درست واژه را انتخاب کن.", "سمه مانا وټاکئ.", "اختر المعنى الصحيح."), GameStyle.MEANING),
        GameMode("correct_word", t("Find the correct word", "پیدا کردن واژه درست", "سمه کلمه ومومئ", "اعثر على الكلمة الصحيحة"), t("Match a meaning to its English word.", "معنی را با واژه انگلیسی وصل کن.", "مانا له انګلیسي کلمې سره ونښلوئ.", "طابق المعنى مع الكلمة الإنجليزية."), GameStyle.SPELLING),
        GameMode("matching", t("Word and meaning match", "وصل واژه و معنی", "کلمه او مانا سره نښلول", "مطابقة الكلمة والمعنى"), t("Choose the matching word pair.", "جفت درست واژه و معنی را انتخاب کن.", "سمه جوړه وټاکئ.", "اختر زوج الكلمة والمعنى."), GameStyle.MATCHING),
        GameMode("scramble", t("Scrambled letters", "حروف به‌هم‌ریخته", "ګډوډ توري", "الحروف المبعثرة"), t("Find the word from mixed letters.", "از حروف درهم، واژه را پیدا کن.", "له ګډوډو تورو کلمه جوړه کړئ.", "كوّن الكلمة من الحروف المبعثرة."), GameStyle.WORD_BUILD),
        GameMode("build_word", t("Build the word", "ساختن واژه", "کلمه جوړول", "كوّن الكلمة"), t("Select the correctly built word.", "شکل درست واژه را انتخاب کن.", "سمه جوړه شوې کلمه وټاکئ.", "اختر الكلمة المكتوبة بشكل صحيح."), GameStyle.SPELLING_CHECK),
        GameMode("sentence", t("Complete the sentence", "تکمیل جمله", "جمله بشپړول", "أكمل الجملة"), t("Choose the word that completes the sentence.", "واژه مناسب برای جمله را انتخاب کن.", "د جملې لپاره سمه کلمه وټاکئ.", "اختر الكلمة المناسبة لإكمال الجملة."), GameStyle.SENTENCE),
        GameMode("missing", t("Missing word", "واژه گمشده", "ورکه کلمه", "الكلمة المفقودة"), t("Fill the missing word in a short phrase.", "واژه حذف‌شده را پیدا کن.", "ورکه شوې کلمه ومومئ.", "اعثر على الكلمة الناقصة."), GameStyle.MISSING_WORD),
        GameMode("memory", t("Vocabulary memory", "حافظه واژگان", "د لغتونو حافظه", "ذاكرة المفردات"), t("Remember the word and select its meaning.", "واژه را به خاطر بسپار و معنی‌اش را انتخاب کن.", "کلمه په یاد وساتئ او مانا یې وټاکئ.", "تذكّر الكلمة واختر معناها."), GameStyle.MEMORY),
        GameMode("speed", t("Speed answer", "پاسخ سریع", "چټک ځواب", "الإجابة السريعة"), t("Answer as many as you can before time runs out.", "پیش از پایان زمان پاسخ بده.", "د وخت تر پای مخکې ځواب ورکړئ.", "أجب قبل انتهاء الوقت."), GameStyle.SPEED, timed = true),
        GameMode("odd_one", t("Odd one out", "پیدا کردن واژه متفاوت", "بېله کلمه ومومئ", "اكتشف الكلمة المختلفة"), t("Find the word that does not belong.", "واژه نامرتبط را پیدا کن.", "هغه کلمه ومومئ چې تړاو نه لري.", "اعثر على الكلمة التي لا تنتمي للمجموعة."), GameStyle.ODD_ONE_OUT),
        GameMode("crossword", t("Word clue challenge", "چالش راهنمای جدول", "د کلمې د نښې لوبه", "تحدي تلميحات الكلمات"), t("Solve a word using its clue.", "با استفاده از راهنما واژه را حدس بزن.", "د نښې له مخې کلمه ومومئ.", "اكتشف الكلمة باستخدام التلميح."), GameStyle.CROSSWORD),
        GameMode("clue", t("Guess from a clue", "حدس با راهنمایی", "له نښې څخه اټکل", "خمن من التلميح"), t("Read a clue and choose the word.", "راهنما را بخوان و واژه را انتخاب کن.", "نښه ولولئ او کلمه وټاکئ.", "اقرأ التلميح واختر الكلمة."), GameStyle.CLUE),
        GameMode("translation", t("Translation game", "بازی ترجمه", "د ژباړې لوبه", "لعبة الترجمة"), t("Translate a useful English word.", "یک واژه کاربردی را ترجمه کن.", "یوه ګټوره انګلیسي کلمه وژباړئ.", "ترجم كلمة إنجليزية مفيدة."), GameStyle.TRANSLATION),
        GameMode("word_order", t("Sentence order", "ترتیب جمله", "د جملې ترتیب", "ترتيب الجملة"), t("Choose the correctly ordered sentence.", "ترتیب درست جمله را انتخاب کن.", "د جملې سم ترتیب وټاکئ.", "اختر ترتيب الجملة الصحيح."), GameStyle.WORD_ORDER),
        GameMode("vocab_race", t("Vocabulary race", "مسابقه واژگان", "د لغتونو سیالي", "سباق المفردات"), t("Answer a quick series of vocabulary questions.", "به پرسش‌های واژگانی سریع پاسخ بده.", "د لغتونو چټکو پوښتنو ته ځواب ورکړئ.", "أجب عن سلسلة سريعة من أسئلة المفردات."), GameStyle.VOCAB_RACE, timed = true),
        GameMode("mistakes", t("Review difficult words", "مرور واژه‌های دشوار", "د سختو کلمو بیاکتنه", "مراجعة الكلمات الصعبة"), t("Practise important words again.", "واژه‌های مهم را دوباره تمرین کن.", "مهمې کلمې بیا تمرین کړئ.", "تدرّب على الكلمات المهمة مرة أخرى."), GameStyle.MISTAKE_REVIEW),
        GameMode("spelling", t("Correct spelling", "املای درست", "سمه املا", "الإملاء الصحيح"), t("Choose the word with correct spelling.", "املای درست را انتخاب کن.", "سمه لیکدود وټاکئ.", "اختر الكلمة ذات الإملاء الصحيح."), GameStyle.SPELLING_CHECK),
        GameMode("picture", t("Picture vocabulary", "واژگان تصویری", "انځوریز لغتونه", "مفردات بالصور"), t("Choose the word that matches the picture.", "واژه متناسب با تصویر را انتخاب کن.", "له انځور سره برابره کلمه وټاکئ.", "اختر الكلمة المناسبة للصورة."), GameStyle.PICTURE)
    )

    private val words = listOf(
        GameWord("apple","سیب","مڼه","تفاحة","🍎","میوه‌ای سرخ یا سبز","A fruit that can be red or green"),
        GameWord("book","کتاب","کتاب","كتاب","📘","چیزی که برای خواندن استفاده می‌شود","Something you read"),
        GameWord("water","آب","اوبه","ماء","💧","نوشیدنی ضروری و بی‌رنگ","A clear liquid people drink"),
        GameWord("school","مکتب","ښوونځی","مدرسة","🏫","جایی که شاگردان درس می‌خوانند","A place where students learn"),
        GameWord("family","خانواده","کورنۍ","عائلة","👨‍👩‍👧","مادر، پدر، خواهر و برادر","Parents, children and relatives"),
        GameWord("garden","باغ","باغ","حديقة","🌷","جایی که گل و گیاه می‌روید","A place where flowers grow"),
        GameWord("teacher","معلم","ښوونکی","معلّم","🧑‍🏫","شخصی که درس می‌دهد","A person who teaches"),
        GameWord("window","کلکین","کړکۍ","نافذة","🪟","از آن بیرون را می‌بینیم","You look through it"),
        GameWord("happy","خوشحال","خوشحاله","سعيد","😊","احساس شادی و رضایت","Feeling pleasure or joy"),
        GameWord("travel","سفر کردن","سفر کول","يسافر","🧳","رفتن از یک مکان به مکان دیگر","Going from one place to another"),
        GameWord("careful","محتاط","احتیاط کوونکی","حذر","🛡️","با دقت و توجه عمل کردن","Taking care to avoid mistakes"),
        GameWord("decide","تصمیم گرفتن","پرېکړه کول","يقرّر","🧭","پس از فکر کردن انتخاب کردن","To make a choice after thinking"),
        GameWord("beautiful","زیبا","ښکلی","جميل","🌺","دارای ظاهر دلپذیر","Very pleasing to look at"),
        GameWord("knowledge","دانش","پوهه","معرفة","📚","اطلاعات و فهمی که یاد گرفته‌ایم","Information and understanding"),
        GameWord("reliable","قابل اعتماد","د باور وړ","موثوق","🤝","کسی یا چیزی که می‌توان به آن اعتماد کرد","Able to be trusted"),
        GameWord("opportunity","فرصت","فرصت","فرصة","🌟","زمان یا موقعیت مناسب برای انجام کاری","A good chance to do something"),
        GameWord("encourage","تشویق کردن","هڅول","يشجّع","💪","به کسی اعتماد و انگیزه دادن","To give someone confidence"),
        GameWord("evidence","شواهد","شواهد","دليل","🔎","اطلاعاتی که چیزی را ثابت می‌کند","Facts that help prove something")
    )

    fun questionFor(mode: GameMode, round: Int): GameQuestion {
        val modeIndex = modes.indexOfFirst { it.id == mode.id }.coerceAtLeast(0)
        val wordIndex = (round * 3 + modeIndex) % words.size
        val word = words[wordIndex]
        val distractors = listOf(words[(wordIndex + 3) % words.size], words[(wordIndex + 7) % words.size])
        val scrambled = if (word.english.length > 2) {
            word.english.last() + word.english.drop(1).dropLast(1) + word.english.first()
        } else word.english
        val options = when (mode.style) {
            GameStyle.ODD_ONE_OUT -> listOf(
                t("apple", "سیب", "مڼه", "تفاحة"),
                t("banana", "کیله", "کېله", "موز"),
                t("book", "کتاب", "کتاب", "كتاب")
            )
            GameStyle.MATCHING -> (listOf(word) + distractors).map {
                t("${it.english} — ${it.fa}", "${it.english} — ${it.fa}", "${it.english} — ${it.ps}", "${it.english} — ${it.ar}")
            }
            GameStyle.WORD_ORDER -> listOf(
                t("I know the word ${word.english}.", "من واژه ${word.fa} را می‌دانم.", "زه د ${word.ps} کلمه پېژنم.", "أنا أعرف كلمة ${word.ar}."),
                t("Know I the word ${word.english}.", "می‌دانم من واژه ${word.fa} را.", "پېژنم زه کلمه ${word.ps} د.", "أعرف أنا كلمة ${word.ar}."),
                t("The word I know ${word.english}.", "واژه من می‌دانم ${word.fa} را.", "کلمه زه پېژنم ${word.ps} د.", "الكلمة أنا أعرف ${word.ar}.")
            )
            GameStyle.SENTENCE -> listOf(
                t("I know the word ${word.english}.", "من واژه ${word.fa} را می‌دانم.", "زه د ${word.ps} کلمه پېژنم.", "أنا أعرف كلمة ${word.ar}."),
                t("I the word know ${word.english}.", "من واژه را می‌دانم ${word.fa}.", "زه کلمه پېژنم د ${word.ps}.", "أنا كلمة أعرف ${word.ar}."),
                t("Know the word I ${word.english}.", "می‌دانم واژه من ${word.fa} را.", "پېژنم کلمه زه ${word.ps} د.", "أعرف الكلمة أنا ${word.ar}.")
            )
            else -> (listOf(word) + distractors).map { it.englishWord() }
        }
        val prompt = when (mode.style) {
            GameStyle.MEANING, GameStyle.MEMORY, GameStyle.MISTAKE_REVIEW ->
                t("Choose the meaning of “${word.english}”.", "معنی «${word.english}» را انتخاب کن.", "د «${word.english}» مانا وټاکئ.", "اختر معنى «${word.english}».")
            GameStyle.MATCHING ->
                t("Choose the correct word-and-meaning pair.", "جفت درست واژه و معنی را انتخاب کن.", "د کلمې او مانا سمه جوړه وټاکئ.", "اختر زوج الكلمة والمعنى الصحيح.")
            GameStyle.WORD_BUILD ->
                t("Unscramble these letters: $scrambled", "این حروف را مرتب کن: $scrambled", "دا توري سم ترتیب کړئ: $scrambled", "رتّب هذه الحروف: $scrambled")
            GameStyle.SPELLING, GameStyle.TRANSLATION, GameStyle.SPELLING_CHECK ->
                t("Which English word means “${word.fa}”?", "کدام واژه انگلیسی به معنی «${word.fa}» است؟", "کومه انګلیسي کلمه د «${word.ps}» مانا لري؟", "ما الكلمة الإنجليزية التي تعني «${word.ar}»؟")
            GameStyle.PICTURE ->
                t("Which word matches this picture: ${word.emoji}?", "کدام واژه با این تصویر برابر است: ${word.emoji}؟", "له دې انځور سره کومه کلمه برابره ده: ${word.emoji}؟", "أي كلمة تناسب هذه الصورة: ${word.emoji}؟")
            GameStyle.CLUE, GameStyle.CROSSWORD ->
                t("Read the clue: ${word.clueEn}. Choose the word.", "راهنما: ${word.clueFa}. واژه را انتخاب کن.", "نښه: ${word.clueFa}. کلمه وټاکئ.", "التلميح: ${word.clueEn}. اختر الكلمة.")
            GameStyle.ODD_ONE_OUT ->
                t("Which word is different from the others?", "کدام واژه با دو واژه دیگر فرق دارد؟", "کومه کلمه له نورو سره توپیر لري؟", "أي كلمة تختلف عن الكلمتين الأخريين؟")
            GameStyle.MISSING_WORD ->
                t("Complete the phrase: “I need ${word.english}.”", "جمله را کامل کن: «I need ${word.english}.»", "جمله بشپړه کړئ: «I need ${word.english}.»", "أكمل العبارة: «I need ${word.english}.»")
            GameStyle.SENTENCE ->
                t("Choose the sentence that uses “${word.english}” correctly.", "جمله‌ای را انتخاب کن که «${word.english}» را درست به‌کار برده باشد.", "هغه جمله وټاکئ چې «${word.english}» پکې سمه کارېدلې وي.", "اختر الجملة التي تستخدم «${word.english}» بشكل صحيح.")
            GameStyle.WORD_ORDER ->
                t("Choose the correct sentence order for “${word.english}”.", "ترتیب درست جمله با «${word.english}» را انتخاب کن.", "د «${word.english}» لپاره د جملې سم ترتیب وټاکئ.", "اختر ترتيب الجملة الصحيح باستخدام «${word.english}».")
            GameStyle.SPEED, GameStyle.VOCAB_RACE ->
                t("Quick! What does “${word.english}” mean?", "سریع! معنی «${word.english}» چیست؟", "ژر! د «${word.english}» مانا څه ده؟", "بسرعة! ما معنى «${word.english}»؟")
            else ->
                t("Choose the best answer for “${word.english}”.", "بهترین پاسخ برای «${word.english}» را انتخاب کن.", "د «${word.english}» لپاره غوره ځواب وټاکئ.", "اختر أفضل إجابة عن «${word.english}».")
        }
        val hint = if (mode.style == GameStyle.WORD_BUILD) {
            t("Mixed letters: $scrambled", "حروف به‌هم‌ریخته: $scrambled", "ګډوډ توري: $scrambled", "حروف مبعثرة: $scrambled")
        } else {
            t("Each hint reveals one more letter.", "هر راهنما یک حرف دیگر را آشکار می‌کند.", "هره لارښوونه یو بل توری ښکاره کوي.", "كل تلميح يكشف حرفاً إضافياً.")
        }
        val explanation = t("${word.english} means ${word.fa}.", "واژه ${word.english} به معنی ${word.fa} است.", "${word.english} د ${word.ps} مانا لري.", "${word.english} تعني ${word.ar}.")
        val correctIndex = if (mode.style == GameStyle.ODD_ONE_OUT) 2 else 0
        return GameQuestion(prompt, options, correctIndex, word.english, hint, explanation)
    }
}
