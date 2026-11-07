package com.example.data

import com.example.model.ClassroomType
import com.example.model.LessonItem
import com.example.model.Level
import com.example.model.QuestionType
import com.example.model.QuizQuestion

/**
 * Adds a consistent second layer of practice to every existing A1-Z9 stage.
 * The original curriculum and saved level IDs remain unchanged.
 */
fun expandLearningCurriculum(levels: List<Level>): List<Level> = levels.map { level ->
    val additions = supplementalLessons[level.levelCode].orEmpty()
    val extraQuestions = additions.flatMap { item ->
        listOf(
            QuizQuestion(
                id = "${item.id}_meaning",
                type = QuestionType.TRANSLATION,
                promptEn = "Choose the correct meaning: ${item.englishText}",
                promptFa = "معنی درست عبارت را انتخاب کن: ${item.englishText}",
                options = listOf(item.persianTranslation, "این عبارت درباره زمان گذشته است", "این عبارت یک پرسش درباره قیمت است", "این عبارت یک دستور منفی است"),
                correctIndex = 0,
                explanationFa = "معنی درست: ${item.persianTranslation}"
            ),
            QuizQuestion(
                id = "${item.id}_speaking",
                type = QuestionType.SPEAKING,
                promptEn = "Read aloud: ${item.exampleSentenceEn}",
                promptFa = "این جمله را با صدای بلند بخوان:",
                options = listOf(item.exampleSentenceEn, "I am learning every day.", "Could you help me, please?", "The weather is beautiful."),
                correctIndex = 0,
                explanationFa = "جمله نمونه را چند بار با تلفظ روشن تکرار کن.",
                targetSpeechText = item.exampleSentenceEn
            )
        )
    }
    level.copy(
        items = level.items + additions,
        examQuestions = level.examQuestions + extraQuestions
    )
}

private fun lesson(
    code: String,
    number: Int,
    english: String,
    phonetic: String,
    translation: String,
    example: String,
    exampleTranslation: String,
    emoji: String,
    type: ClassroomType,
    tip: String
) = LessonItem(
    id = "${code.lowercase()}_practice_$number",
    englishText = english,
    phonetic = phonetic,
    persianTranslation = translation,
    exampleSentenceEn = example,
    exampleSentenceFa = exampleTranslation,
    emoji = emoji,
    classroomType = type,
    levelCode = code,
    grammarRuleFa = tip
)

private val supplementalLessons: Map<String, List<LessonItem>> = mapOf(
    "A1" to listOf(
        lesson("A1", 1, "Apple, ant, and animal", "/ˈæpəl ænt ˈænɪməl/", "سیب، مورچه و حیوان", "An ant is a small animal.", "مورچه یک حیوان کوچک است.", "🍎", ClassroomType.PHONICS, "صدای کوتاه A در apple و ant شبیه /æ/ است."),
        lesson("A1", 2, "This is my book", "/ðɪs ɪz maɪ bʊk/", "این کتاب من است", "This is my English book.", "این کتاب انگلیسی من است.", "📘", ClassroomType.GRAMMAR, "This برای اشاره به یک چیز نزدیک استفاده می‌شود.")
    ),
    "A2" to listOf(
        lesson("A2", 1, "These are my parents", "/ðiːz ɑːr maɪ ˈpɛrənts/", "این‌ها پدر و مادر من هستند", "These are my parents.", "این‌ها پدر و مادر من هستند.", "👨‍👩‍👧", ClassroomType.GRAMMAR, "These شکل جمع this است و برای چند چیز نزدیک به کار می‌رود."),
        lesson("A2", 2, "I have two sisters", "/aɪ hæv tuː ˈsɪstərz/", "من دو خواهر دارم", "I have two sisters and one brother.", "من دو خواهر و یک برادر دارم.", "👭", ClassroomType.VOCABULARY, "بعد از عدد بیشتر از یک، اسم قابل شمارش معمولاً جمع می‌آید.")
    ),
    "A3" to listOf(
        lesson("A3", 1, "How much does it cost?", "/haʊ mʌtʃ dʌz ɪt kɔːst/", "قیمت آن چند است؟", "How much does this bag cost?", "قیمت این بکس چند است؟", "🛍️", ClassroomType.CONVERSATION, "برای پرسیدن قیمت از How much استفاده می‌کنیم."),
        lesson("A3", 2, "I usually have breakfast at seven", "/aɪ ˈjuːʒuəli hæv ˈbrɛkfəst æt ˈsɛvən/", "من معمولاً ساعت هفت صبحانه می‌خورم", "I usually have breakfast at seven.", "من معمولاً ساعت هفت صبحانه می‌خورم.", "⏰", ClassroomType.GRAMMAR, "قید تکرار usually پیش از فعل اصلی می‌آید.")
    ),
    "B1" to listOf(
        lesson("B1", 1, "I visited my friend yesterday", "/aɪ ˈvɪzɪtɪd maɪ frɛnd ˈjɛstərdeɪ/", "دیروز به دیدن دوستم رفتم", "I visited my friend yesterday.", "دیروز به دیدن دوستم رفتم.", "🚶", ClassroomType.GRAMMAR, "برای فعل باقاعده در گذشته معمولاً ed اضافه می‌شود."),
        lesson("B1", 2, "I am going to study tonight", "/aɪ æm ˈɡoʊɪŋ tə ˈstʌdi təˈnaɪt/", "امشب قصد دارم درس بخوانم", "I am going to study English tonight.", "امشب قصد دارم انگلیسی بخوانم.", "📚", ClassroomType.CONVERSATION, "ساختار be going to برای برنامه یا قصد آینده استفاده می‌شود.")
    ),
    "B2" to listOf(
        lesson("B2", 1, "Turn left at the next street", "/tɜːrn lɛft æt ðə nɛkst striːt/", "در سرک بعدی به چپ بپیچ", "Turn left at the next street.", "در سرک بعدی به چپ بپیچ.", "🧭", ClassroomType.CONVERSATION, "Turn left و turn right برای راهنمایی مسیر هستند."),
        lesson("B2", 2, "Could you show me the way?", "/kʊd juː ʃoʊ miː ðə weɪ/", "می‌توانید راه را به من نشان بدهید؟", "Could you show me the way to the station?", "می‌توانید راه ایستگاه را به من نشان بدهید؟", "📍", ClassroomType.CONVERSATION, "Could you درخواست مؤدبانه‌تری از Can you است.")
    ),
    "B3" to listOf(
        lesson("B3", 1, "I feel nervous before exams", "/aɪ fiːl ˈnɜːrvəs bɪˈfɔːr ɪɡˈzæmz/", "پیش از امتحان‌ها احساس اضطراب می‌کنم", "I feel nervous before exams, but I keep trying.", "پیش از امتحان‌ها اضطراب دارم، اما به تلاشم ادامه می‌دهم.", "💗", ClassroomType.VOCABULARY, "Feel + adjective برای بیان احساسات به کار می‌رود."),
        lesson("B3", 2, "I am proud of my progress", "/aɪ æm praʊd əv maɪ ˈprɑːɡrɛs/", "به پیشرفت خود افتخار می‌کنم", "I am proud of my progress this month.", "به پیشرفت خود در این ماه افتخار می‌کنم.", "🌟", ClassroomType.CONVERSATION, "بعد از proud از حرف اضافه of استفاده می‌شود.")
    ),
    "C1" to listOf(
        lesson("C1", 1, "She has worked here for five years", "/ʃiː hæz wɜːrkt hɪr fər faɪv jɪrz/", "او پنج سال است که این‌جا کار می‌کند", "She has worked here for five years.", "او پنج سال است که این‌جا کار می‌کند.", "🗓️", ClassroomType.GRAMMAR, "Present Perfect با for مدت زمان را نشان می‌دهد."),
        lesson("C1", 2, "I have never tried sushi", "/aɪ hæv ˈnɛvər traɪd ˈsuːʃi/", "من هرگز سوشی را امتحان نکرده‌ام", "I have never tried sushi before.", "من قبلاً هرگز سوشی را امتحان نکرده‌ام.", "🍽️", ClassroomType.GRAMMAR, "Never در Present Perfect پیش از قسمت سوم فعل می‌آید.")
    ),
    "C2" to listOf(
        lesson("C2", 1, "If I had more time, I would read", "/ɪf aɪ hæd mɔːr taɪm aɪ wʊd riːd/", "اگر وقت بیشتری می‌داشتم، مطالعه می‌کردم", "If I had more time, I would read every day.", "اگر وقت بیشتری می‌داشتم، هر روز مطالعه می‌کردم.", "💡", ClassroomType.GRAMMAR, "شرطی نوع دوم برای حالت فرضی حال یا آینده است."),
        lesson("C2", 2, "Although it was difficult, I continued", "/ɔːlˈðoʊ ɪt wəz ˈdɪfɪkəlt aɪ kənˈtɪnjuːd/", "با آن‌که دشوار بود، ادامه دادم", "Although it was difficult, I continued learning.", "با آن‌که دشوار بود، به یادگیری ادامه دادم.", "⛰️", ClassroomType.READING_WRITING, "Although دو جمله متضاد را به هم پیوند می‌دهد.")
    ),
    "C3" to listOf(
        lesson("C3", 1, "I ran out of time", "/aɪ ræn aʊt əv taɪm/", "وقتم تمام شد", "I ran out of time before the meeting.", "پیش از جلسه وقتم تمام شد.", "⌛", ClassroomType.VOCABULARY, "Run out of یعنی تمام شدن یا به پایان رسیدن."),
        lesson("C3", 2, "Please look after the children", "/pliːz lʊk ˈæftər ðə ˈtʃɪldrən/", "لطفاً از کودکان مراقبت کن", "Please look after the children for an hour.", "لطفاً یک ساعت از کودکان مراقبت کن.", "🤝", ClassroomType.CONVERSATION, "Look after یک فعل عبارتی به معنی مراقبت کردن است.")
    ),
    "D1" to listOf(
        lesson("D1", 1, "In my opinion, practice matters most", "/ɪn maɪ əˈpɪnjən ˈpræktɪs ˈmætərz moʊst/", "به نظر من، تمرین از همه مهم‌تر است", "In my opinion, daily practice matters most.", "به نظر من، تمرین روزانه از همه مهم‌تر است.", "🎙️", ClassroomType.CONVERSATION, "In my opinion برای بیان نظر شخصی استفاده می‌شود."),
        lesson("D1", 2, "First, let me explain the reason", "/fɜːrst lɛt miː ɪkˈspleɪn ðə ˈriːzən/", "نخست اجازه بده دلیل را توضیح بدهم", "First, let me explain the reason clearly.", "نخست اجازه بده دلیل را به روشنی توضیح بدهم.", "🧩", ClassroomType.READING_WRITING, "First, next و finally ترتیب ایده‌ها را روشن می‌کنند.")
    ),
    "D2" to listOf(
        lesson("D2", 1, "At first, she felt uncertain", "/æt fɜːrst ʃiː fɛlt ʌnˈsɜːrtən/", "در آغاز، او مطمئن نبود", "At first, she felt uncertain about the journey.", "در آغاز، او درباره این مسیر مطمئن نبود.", "📖", ClassroomType.READING_WRITING, "At first برای آغاز روایت و معرفی وضعیت اولیه است."),
        lesson("D2", 2, "Eventually, everything changed", "/ɪˈvɛntʃuəli ˈɛvrɪθɪŋ tʃeɪndʒd/", "سرانجام، همه چیز تغییر کرد", "Eventually, her hard work brought results.", "سرانجام، تلاش سخت او نتیجه داد.", "🌅", ClassroomType.VOCABULARY, "Eventually یعنی پس از گذشت زمان، سرانجام.")
    ),
    "D3" to listOf(
        lesson("D3", 1, "PHOtograph and phoTOGraphy", "/ˈfoʊtəɡræf/ /fəˈtɑːɡrəfi/", "عکس و عکاسی", "The stress changes in photograph and photography.", "جای تکیه در دو واژه photograph و photography تغییر می‌کند.", "🔊", ClassroomType.PRONUNCIATION, "در انگلیسی جای تکیه واژه‌ها مهم است و گاهی با تغییر پسوند جابه‌جا می‌شود."),
        lesson("D3", 2, "Would you like a cup of tea?", "/wʊd juː laɪk ə kʌp əv tiː/", "یک پیاله چای میل دارید؟", "Would you like a cup of tea?", "یک پیاله چای میل دارید؟", "☕", ClassroomType.PRONUNCIATION, "در گفتار طبیعی، کلمات کم‌تکیه کوتاه و پیوسته ادا می‌شوند.")
    ),
    "E1" to listOf(
        lesson("E1", 1, "The study examines three factors", "/ðə ˈstʌdi ɪɡˈzæmɪnz θriː ˈfæktərz/", "این پژوهش سه عامل را بررسی می‌کند", "The study examines three factors that affect learning.", "این پژوهش سه عاملی را بررسی می‌کند که بر یادگیری اثر دارند.", "🔬", ClassroomType.READING_WRITING, "Examine در متن علمی به معنی بررسی دقیق است."),
        lesson("E1", 2, "The findings indicate a clear pattern", "/ðə ˈfaɪndɪŋz ˈɪndɪkeɪt ə klɪr ˈpætərn/", "یافته‌ها یک الگوی روشن را نشان می‌دهند", "The findings indicate a clear pattern.", "یافته‌ها یک الگوی روشن را نشان می‌دهند.", "📊", ClassroomType.VOCABULARY, "Indicate در نوشتار علمی یعنی نشان دادن یا حاکی بودن.")
    ),
    "E2" to listOf(
        lesson("E2", 1, "I am writing to enquire about the position", "/aɪ æm ˈraɪtɪŋ tə ɪnˈkwaɪər əˈbaʊt ðə pəˈzɪʃən/", "برای پرس‌وجو درباره این بست می‌نویسم", "I am writing to enquire about the position.", "برای پرس‌وجو درباره این بست می‌نویسم.", "✉️", ClassroomType.READING_WRITING, "I am writing to یک آغاز رسمی و رایج برای ایمیل است."),
        lesson("E2", 2, "Thank you for your consideration", "/θæŋk juː fər jʊr kənˌsɪdəˈreɪʃən/", "از توجه و بررسی شما سپاسگزارم", "Thank you for your consideration.", "از توجه و بررسی شما سپاسگزارم.", "🤝", ClassroomType.VOCABULARY, "این عبارت برای پایان محترمانه نامه رسمی مناسب است.")
    ),
    "E3" to listOf(
        lesson("E3", 1, "Could we explore another option?", "/kʊd wiː ɪkˈsplɔːr əˈnʌðər ˈɑːpʃən/", "آیا می‌توانیم گزینه دیگری را بررسی کنیم؟", "Could we explore another option before deciding?", "پیش از تصمیم‌گیری، آیا می‌توانیم گزینه دیگری را بررسی کنیم؟", "🤝", ClassroomType.CONVERSATION, "Could we برای پیشنهاد مؤدبانه در مذاکره مناسب است."),
        lesson("E3", 2, "Let us find common ground", "/lɛt ʌs faɪnd ˈkɑːmən ɡraʊnd/", "بیایید به نقطه مشترک برسیم", "Let us find common ground and move forward.", "بیایید نقطه مشترکی پیدا کنیم و پیش برویم.", "⚖️", ClassroomType.CONVERSATION, "Common ground یعنی زمینه یا نقطه توافق مشترک.")
    ),
    "F1" to listOf(
        lesson("F1", 1, "That sounds awesome", "/ðæt saʊndz ˈɔːsəm/", "این واقعاً عالی به نظر می‌رسد", "Your idea sounds awesome.", "فکر تو واقعاً عالی به نظر می‌رسد.", "😎", ClassroomType.CONVERSATION, "Awesome محاوره‌ای و غیررسمی است؛ در نامه رسمی کمتر استفاده می‌شود."),
        lesson("F1", 2, "I am just kidding", "/aɪ æm dʒʌst ˈkɪdɪŋ/", "فقط شوخی می‌کنم", "Relax, I am just kidding.", "آرام باش، فقط شوخی می‌کنم.", "😄", ClassroomType.VOCABULARY, "Just kidding برای روشن کردن شوخی استفاده می‌شود.")
    ),
    "F2" to listOf(
        lesson("F2", 1, "Practice makes perfect", "/ˈpræktɪs meɪks ˈpɜːrfɪkt/", "تمرین انسان را ماهر می‌سازد", "Practice makes perfect, so keep going.", "تمرین انسان را ماهر می‌سازد، پس ادامه بده.", "🏅", ClassroomType.VOCABULARY, "این ضرب‌المثل بر تکرار و تمرین تأکید دارد."),
        lesson("F2", 2, "Better late than never", "/ˈbɛtər leɪt ðæn ˈnɛvər/", "دیر رسیدن بهتر از هرگز نرسیدن است", "You started today; better late than never.", "امروز شروع کردی؛ دیر رسیدن بهتر از هرگز نرسیدن است.", "⏳", ClassroomType.READING_WRITING, "این ضرب‌المثل برای تشویق شروع دیرهنگام اما ارزشمند است.")
    ),
    "F3" to listOf(
        lesson("F3", 1, "That was a clever joke", "/ðæt wəz ə ˈklɛvər dʒoʊk/", "آن یک شوخی هوشمندانه بود", "That was a clever joke about daily life.", "آن یک شوخی هوشمندانه درباره زندگی روزمره بود.", "😄", ClassroomType.CONVERSATION, "Clever یعنی هوشمندانه؛ joke یعنی شوخی."),
        lesson("F3", 2, "I did not see that coming", "/aɪ dɪd nɑːt siː ðæt ˈkʌmɪŋ/", "اصلاً انتظارش را نداشتم", "Your funny answer surprised me; I did not see that coming.", "جواب خنده‌دارت مرا غافلگیر کرد؛ اصلاً انتظارش را نداشتم.", "🎭", ClassroomType.CONVERSATION, "این عبارت برای بیان شگفتی از یک اتفاق غیرمنتظره است.")
    ),
    "G1" to listOf(
        lesson("G1", 1, "Take a deep breath and pause", "/teɪk ə diːp brɛθ ænd pɔːz/", "یک نفس عمیق بکش و مکث کن", "Take a deep breath before you respond.", "پیش از پاسخ دادن یک نفس عمیق بکش.", "🌿", ClassroomType.VOCABULARY, "Take a breath یک ترکیب رایج برای نفس کشیدن است."),
        lesson("G1", 2, "I can notice my thoughts without judging them", "/aɪ kæn ˈnoʊtɪs maɪ θɔːts wɪˈðaʊt ˈdʒʌdʒɪŋ ðɛm/", "می‌توانم افکارم را بدون قضاوت مشاهده کنم", "I can notice my thoughts without judging them.", "می‌توانم افکارم را بدون قضاوت مشاهده کنم.", "🕊️", ClassroomType.READING_WRITING, "Without + verb-ing ساختار درست برای بیان «بدون انجام دادن» است.")
    ),
    "G2" to listOf(
        lesson("G2", 1, "I break big goals into small steps", "/aɪ breɪk bɪɡ ɡoʊlz ˈɪntuː smɔːl stɛps/", "هدف‌های بزرگ را به گام‌های کوچک تقسیم می‌کنم", "I break big goals into small daily steps.", "هدف‌های بزرگ را به گام‌های کوچک روزانه تقسیم می‌کنم.", "🪜", ClassroomType.CONVERSATION, "Break something into parts یعنی چیزی را به بخش‌ها تقسیم کردن."),
        lesson("G2", 2, "Consistency is more important than intensity", "/kənˈsɪstənsi ɪz mɔːr ɪmˈpɔːrtənt ðæn ɪnˈtɛnsəti/", "پیوستگی از شدت تلاش مهم‌تر است", "Consistency is more important than intensity.", "پیوستگی از شدت تلاش مهم‌تر است.", "🔥", ClassroomType.READING_WRITING, "Consistency یعنی استمرار؛ intensity یعنی شدت.")
    ),
    "G3" to listOf(
        lesson("G3", 1, "The main idea of the chapter is clear", "/ðə meɪn aɪˈdiːə əv ðə ˈtʃæptər ɪz klɪr/", "ایده اصلی فصل روشن است", "The main idea of the chapter is clear.", "ایده اصلی فصل روشن است.", "📚", ClassroomType.READING_WRITING, "برای خلاصه‌نویسی، ابتدا main idea یا ایده اصلی را پیدا کن."),
        lesson("G3", 2, "A useful summary keeps only key points", "/ə ˈjuːsfəl ˈsʌməri kiːps ˈoʊnli kiː pɔɪnts/", "یک خلاصه مفید فقط نکته‌های کلیدی را نگه می‌دارد", "A useful summary keeps only key points.", "یک خلاصه مفید فقط نکته‌های کلیدی را نگه می‌دارد.", "📝", ClassroomType.READING_WRITING, "در خلاصه‌نویسی مثال‌های فرعی و تکرارها را حذف کن.")
    ),
    "Z1" to listOf(
        lesson("Z1", 1, "Let me put it another way", "/lɛt miː pʊt ɪt əˈnʌðər weɪ/", "اجازه بده آن را به شکل دیگری بیان کنم", "Let me put it another way so it is clear.", "اجازه بده آن را به شکل دیگری بیان کنم تا روشن شود.", "⚡", ClassroomType.CONVERSATION, "این عبارت هنگام توضیح دوباره و روان‌تر مفید است."),
        lesson("Z1", 2, "That is exactly what I mean", "/ðæt ɪz ɪɡˈzæktli wʌt aɪ miːn/", "دقیقاً منظورم همین است", "That is exactly what I mean.", "دقیقاً منظورم همین است.", "🎙️", ClassroomType.CONVERSATION, "Exactly برای تأکید بر دقت و توافق به کار می‌رود.")
    ),
    "Z2" to listOf(
        lesson("Z2", 1, "Could you clarify the speaker's point?", "/kʊd juː ˈklærɪfaɪ ðə ˈspiːkərz pɔɪnt/", "می‌توانید منظور گوینده را روشن سازید؟", "Could you clarify the speaker's point?", "می‌توانید منظور گوینده را روشن سازید؟", "🎧", ClassroomType.LISTENING, "Clarify یعنی روشن و واضح ساختن."),
        lesson("Z2", 2, "The speaker contrasts two viewpoints", "/ðə ˈspiːkər kənˈtræsts tuː ˈvjuːpɔɪnts/", "گوینده دو دیدگاه را با هم مقایسه و متضاد می‌سازد", "The speaker contrasts two viewpoints.", "گوینده دو دیدگاه را با هم مقایسه و متضاد می‌سازد.", "🌍", ClassroomType.LISTENING, "Contrast یعنی تفاوت‌ها یا تضادها را نشان دادن.")
    ),
    "Z3" to listOf(
        lesson("Z3", 1, "Her argument is both elegant and precise", "/hɜːr ˈɑːrɡjəmənt ɪz boʊθ ˈɛləɡənt ænd prɪˈsaɪs/", "استدلال او هم سنجیده و هم دقیق است", "Her argument is both elegant and precise.", "استدلال او هم سنجیده و هم دقیق است.", "🌹", ClassroomType.READING_WRITING, "Both ... and برای پیوند دو ویژگی هم‌زمان استفاده می‌شود."),
        lesson("Z3", 2, "Choose words that convey your meaning clearly", "/tʃuːz wɜːrdz ðæt kənˈveɪ jʊr ˈmiːnɪŋ ˈklɪrli/", "واژه‌هایی انتخاب کن که منظورت را روشن برسانند", "Choose words that convey your meaning clearly.", "واژه‌هایی انتخاب کن که منظورت را روشن برسانند.", "✍️", ClassroomType.VOCABULARY, "Convey یعنی منتقل کردن یا رساندن مفهوم.")
    ),
    "Z4" to listOf(
        lesson("Z4", 1, "Let me consider the question carefully", "/lɛt miː kənˈsɪdər ðə ˈkwɛstʃən ˈkɛrfəli/", "اجازه بده پرسش را با دقت بررسی کنم", "Let me consider the question carefully.", "اجازه بده پرسش را با دقت بررسی کنم.", "🪄", ClassroomType.CONVERSATION, "این جمله برای خریدن زمان در پاسخ بداهه مؤدبانه است."),
        lesson("Z4", 2, "There are several ways to approach this issue", "/ðɛr ɑːr ˈsɛvərəl weɪz tə əˈproʊtʃ ðɪs ˈɪʃuː/", "چندین راه برای پرداختن به این موضوع وجود دارد", "There are several ways to approach this issue.", "چندین راه برای پرداختن به این موضوع وجود دارد.", "🧠", ClassroomType.CONVERSATION, "Approach به معنی روش برخورد یا پرداختن به موضوع است.")
    ),
    "Z5" to listOf(
        lesson("Z5", 1, "The distinction is subtle but important", "/ðə dɪˈstɪŋkʃən ɪz ˈsʌtəl bət ɪmˈpɔːrtənt/", "این تفاوت ظریف اما مهم است", "The distinction is subtle but important.", "این تفاوت ظریف اما مهم است.", "🔍", ClassroomType.VOCABULARY, "Distinction تفاوت مشخص؛ subtle یعنی ظریف و غیرآشکار."),
        lesson("Z5", 2, "Please define the term precisely", "/pliːz dɪˈfaɪn ðə tɜːrm prɪˈsaɪsli/", "لطفاً اصطلاح را دقیق تعریف کن", "Please define the term precisely.", "لطفاً اصطلاح را دقیق تعریف کن.", "📐", ClassroomType.READING_WRITING, "Define یعنی تعریف کردن؛ precisely یعنی با دقت.")
    ),
    "Z6" to listOf(
        lesson("Z6", 1, "The evidence supports the conclusion", "/ði ˈɛvɪdəns səˈpɔːrts ðə kənˈkluːʒən/", "شواهد از نتیجه‌گیری پشتیبانی می‌کند", "The evidence supports the conclusion.", "شواهد از نتیجه‌گیری پشتیبانی می‌کند.", "🧪", ClassroomType.READING_WRITING, "Evidence اسم غیرقابل‌شمارش است و معمولاً بدون s می‌آید."),
        lesson("Z6", 2, "Further research is needed to verify the claim", "/ˈfɜːrðər ˈriːsɜːrtʃ ɪz ˈniːdɪd tə ˈvɛrɪfaɪ ðə kleɪm/", "برای تأیید این ادعا به پژوهش بیشتری نیاز است", "Further research is needed to verify the claim.", "برای تأیید این ادعا به پژوهش بیشتری نیاز است.", "🔬", ClassroomType.READING_WRITING, "Verify یعنی راستی‌آزمایی یا تأیید با شواهد.")
    ),
    "Z7" to listOf(
        lesson("Z7", 1, "A good leader listens before deciding", "/ə ɡʊd ˈliːdər ˈlɪsənz bɪˈfɔːr dɪˈsaɪdɪŋ/", "یک رهبر خوب پیش از تصمیم‌گیری گوش می‌دهد", "A good leader listens before deciding.", "یک رهبر خوب پیش از تصمیم‌گیری گوش می‌دهد.", "🧭", ClassroomType.CONVERSATION, "Listen به حرف اضافه to نیاز دارد، اما در این جمله مفعول مستقیم ندارد."),
        lesson("Z7", 2, "We should acknowledge different perspectives", "/wiː ʃʊd əkˈnɑːlɪdʒ ˈdɪfərənt pərˈspɛktɪvz/", "باید دیدگاه‌های متفاوت را به رسمیت بشناسیم", "We should acknowledge different perspectives.", "باید دیدگاه‌های متفاوت را به رسمیت بشناسیم.", "🤝", ClassroomType.VOCABULARY, "Acknowledge یعنی پذیرفتن یا به رسمیت شناختن.")
    ),
    "Z8" to listOf(
        lesson("Z8", 1, "Meaning depends on context", "/ˈmiːnɪŋ dɪˈpɛndz ɑːn ˈkɑːntɛkst/", "معنا به بافت و زمینه بستگی دارد", "Meaning depends on context, not words alone.", "معنا فقط به واژه‌ها نه، بلکه به زمینه نیز بستگی دارد.", "🌐", ClassroomType.READING_WRITING, "در ترجمه، بافت جمله گاهی از معنی جداگانه واژه مهم‌تر است."),
        lesson("Z8", 2, "Translate the idea, not each word", "/trænzˈleɪt ði aɪˈdiːə nɑːt iːtʃ wɜːrd/", "مفهوم را ترجمه کن، نه تک‌تک واژه‌ها را", "Translate the idea, not each word.", "مفهوم را ترجمه کن، نه تک‌تک واژه‌ها را.", "📝", ClassroomType.READING_WRITING, "ترجمه طبیعی باید مفهوم و لحن متن را منتقل کند.")
    ),
    "Z9" to listOf(
        lesson("Z9", 1, "I can communicate with clarity and confidence", "/aɪ kæn kəˈmjuːnɪkeɪt wɪð ˈklærəti ænd ˈkɑːnfɪdəns/", "می‌توانم با روشنی و اعتمادبه‌نفس ارتباط برقرار کنم", "I can communicate with clarity and confidence.", "می‌توانم با روشنی و اعتمادبه‌نفس ارتباط برقرار کنم.", "👑", ClassroomType.CONVERSATION, "Clarity یعنی روشنی بیان؛ confidence یعنی اعتمادبه‌نفس."),
        lesson("Z9", 2, "My learning journey continues beyond this course", "/maɪ ˈlɜːrnɪŋ ˈdʒɜːrni kənˈtɪnjuːz bɪˈjɑːnd ðɪs kɔːrs/", "مسیر یادگیری من پس از این دوره نیز ادامه می‌یابد", "My learning journey continues beyond this course.", "مسیر یادگیری من پس از این دوره نیز ادامه می‌یابد.", "🏆", ClassroomType.READING_WRITING, "Beyond یعنی فراتر از؛ continues یعنی ادامه می‌یابد.")
    )
)
