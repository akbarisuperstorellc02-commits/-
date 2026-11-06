package com.example.data

import com.example.model.ClassroomType
import com.example.model.GameStage
import com.example.model.GoldenMedal
import com.example.model.LessonItem
import com.example.model.Level
import com.example.model.QuestionType
import com.example.model.QuizQuestion
import com.example.model.TierGroup

object CurriculumData {

    val goldenMedals: List<GoldenMedal> = listOf(
        GoldenMedal("m1", "مدال صورتی شروع مقتدرانه", "آغاز مسیر خودسازی و مطالعه اولین درس", "🌸", true, 1),
        GoldenMedal("m2", "تاج پفکی فونیکس و صداها", "تسلط کامل بر تمام صداها و حروف انگلیسی", "👑", false, 6),
        GoldenMedal("m3", "مدال استمرار ۷ روزه", "هفت روز متوالی پایبندی به هدف روزانه", "🔥", false, 12),
        GoldenMedal("m4", "نشان استاد مکالمه طلایی", "کسب نمره ۹۰+ در آزمون‌های صوتی اسپیکینگ", "🎙️", false, 18),
        GoldenMedal("m5", "الماس درخشان سری B", "گذر از مراحل B1 تا B3 با نمرات عالی", "💎", false, 25),
        GoldenMedal("m6", "ستاره طلایی سری C و D", "تسلط بر داستان‌سرایی و فن بیان بدون لهجه", "🌟", false, 35),
        GoldenMedal("m7", "جام بلورین خودسازی سری G", "مطالعه و تحلیل متون روانشناسی خودسازی به انگلیسی", "🏆", false, 50),
        GoldenMedal("m8", "تاج اعظم تسلط مطلق Z9", "رسیدن به اوج قله یادگیری از A1 تا Z9 زهرا", "👑", false, 75)
    )

    // Extensive Level Series from A1 to Z9
    val levels: List<Level> = listOf(
        // === TIER A ===
        Level(
            id = 1,
            levelCode = "A1",
            tierGroup = TierGroup.TIER_A,
            titleFa = "سطح A1: الفبا، فونیکس و اصطلاحات اولیه",
            titleEn = "A1: Phonics & Absolute Foundations",
            iconEmoji = "🔤",
            descriptionFa = "آواشناسی دقیق حروف، ترکیب SH/CH و احوالپرسی اولیه",
            accentColorHex = 0xFFFF6584,
            isUnlocked = true,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("a1_1", "Hello, I am Zahra", "/həˈloʊ aɪ æm ˈzæhrə/", "سلام، من زهرا هستم", "Hello, I am Zahra and I love learning.", "سلام، من زهرا هستم و عاشق یادگیری‌ام.", "🌸", ClassroomType.PHONICS, "A1", "معرفی با فعل am"),
                LessonItem("a1_2", "SH - Shine like gold", "/ʃaɪn laɪk ɡoʊld/", "ش - مثل طلا بدرخش", "You shine like pure gold.", "تو مثل طلای ناب می‌درخشی.", "✨", ClassroomType.PRONUNCIATION, "A1", "ترکیب sh همیشه صدای ش می‌دهد."),
                LessonItem("a1_3", "CH - Champion Zahra", "/ˈtʃæmpiən/", "چ - زهرای قهرمان", "Zahra is a true champion.", "زهرا یک قهرمان واقعی است.", "🏆", ClassroomType.VOCABULARY, "A1", "ترکیب ch صدای چ می‌دهد.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_a1_1", QuestionType.MULTIPLE_CHOICE, "What sound does 'SH' make in 'Shine'?", "ترکیب SH چه صدایی می‌دهد؟", listOf("صدای چ", "صدای ش", "صدای س", "صدای ف"), 1, "ترکیب SH صدای ش /ʃ/ می‌دهد."),
                QuizQuestion("q_a1_2", QuestionType.SPEAKING, "Say clearly: 'Hello, I am Zahra'", "با صدای رسا بگو: 'Hello, I am Zahra'", listOf("Hello, I am Zahra", "I am Zahra", "Hello Zahra", "Zahra"), 0, "تلفظ جمله را تمرین کنید.", "Hello I am Zahra")
            )
        ),
        Level(
            id = 2,
            levelCode = "A2",
            tierGroup = TierGroup.TIER_A,
            titleFa = "سطح A2: افعال To Be، خانواده و دوستان",
            titleEn = "A2: To Be Verbs & Loved Ones",
            iconEmoji = "👭",
            descriptionFa = "تسلط بر am/is/are، معرفی بستگان و کلمات مهربانی",
            accentColorHex = 0xFFFF85A1,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("a2_1", "She is my best friend", "/ʃiː ɪz maɪ bɛst frɛnd/", "او بهترین دوست من است", "She is my best friend forever.", "او بهترین دوست همیشگی من است.", "💖", ClassroomType.GRAMMAR, "A2", "استفاده از is برای سوم شخص."),
                LessonItem("a2_2", "We are a happy family", "/wiː ɑːr ə ˈhæpi ˈfæməli/", "ما یک خانواده شاد هستیم", "We are a happy family filled with love.", "ما یک خانواده شاد و سرشار از عشقیم.", "👨‍👩‍👧", ClassroomType.CONVERSATION, "A2", "فعل are برای ضمیر We.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_a2_1", QuestionType.GRAMMAR_FILL, "Choose verb: 'She ____ very kind.'", "فعل مناسب را انتخاب کنید:", listOf("am", "is", "are", "be"), 1, "برای She از فعل is استفاده می‌شود.")
            )
        ),
        Level(
            id = 3,
            levelCode = "A3",
            tierGroup = TierGroup.TIER_A,
            titleFa = "سطح A3: رنگ‌ها، خوراکی‌ها، خرید و اعداد",
            titleEn = "A3: Colors, Sweets & Daily Numbers",
            iconEmoji = "🍓",
            descriptionFa = "سفارش بابل تی، شمارش تا صد و لباس‌های پفکی صورتی",
            accentColorHex = 0xFFFF4D6D,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("a3_1", "Strawberry boba tea", "/ˈstrɔːˌbɛri ˈboʊbə tiː/", "چای بابل تی توت‌فرنگی", "I would like a strawberry boba tea, please.", "یک بابل تی توت‌فرنگی می‌خواهم، لطفاً.", "🧋", ClassroomType.CONVERSATION, "A3", "سفارش مودبانه با I would like"),
                LessonItem("a3_2", "A lovely puffy dress", "/ə ˈlʌvli ˈpʌfi drɛs/", "یک پیراهن پفکی دوست‌داشتنی", "Zahra wears a lovely puffy dress.", "زهرا یک پیراهن پفکی زیبا پوشیده.", "👗", ClassroomType.VOCABULARY, "A3", "صفت قبل از اسم می‌آید.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_a3_1", QuestionType.TRANSLATION, "Translate: 'Strawberry boba tea'", "ترجمه کلمه چیست؟", listOf("کیک شکلاتی", "چای بابل تی توت‌فرنگی", "آب‌میوه هلو", "بستنی وانیلی"), 1, "توت‌فرنگی = Strawberry")
            )
        ),

        // === TIER B ===
        Level(
            id = 4,
            levelCode = "B1",
            tierGroup = TierGroup.TIER_B,
            titleFa = "سطح B1: زمان‌های گذشته و آینده (Past & Future)",
            titleEn = "B1: Past Achievements & Future Dreams",
            iconEmoji = "🌱",
            descriptionFa = "بیان دستاوردهای دیروز و برنامه‌های درخشان فردا",
            accentColorHex = 0xFFFF758F,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("b1_1", "Yesterday I studied well", "/ˈjɛstərdeɪ aɪ ˈstʌdid wɛl/", "دیروز به خوبی مطالعه کردم", "Yesterday I studied well and felt inspired.", "دیروز به خوبی مطالعه کردم و الهام گرفتم.", "📖", ClassroomType.GRAMMAR, "B1", "قاعده ed برای گذشته باقاعده."),
                LessonItem("b1_2", "Tomorrow I will shine", "/təˈmɑːroʊ aɪ wɪl ʃaɪn/", "فردا خواهم درخشید", "Tomorrow I will shine even brighter.", "فردا حتی درخشان‌تر خواهم بود.", "⭐", ClassroomType.CONVERSATION, "B1", "استفاده از will برای آینده.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_b1_1", QuestionType.GRAMMAR_FILL, "Future simple: 'I _____ achieve my dream.'", "فعل آینده کدام است؟", listOf("will", "was", "did", "have"), 0, "will برای زمان آینده به کار می‌رود.")
            )
        ),
        Level(
            id = 5,
            levelCode = "B2",
            tierGroup = TierGroup.TIER_B,
            titleFa = "سطح B2: جهت‌یابی، مدرسه و کارهای روزمره",
            titleEn = "B2: Daily Routine & Navigation",
            iconEmoji = "🧭",
            descriptionFa = "آدرس پرسیدن در سفر، ساعات روز و برنامه‌ریزی شخصی",
            accentColorHex = 0xFFFF6584,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("b2_1", "Excuse me, where is the library?", "/ɪkˈskjuːz miː wɛər ɪz ðə ˈlaɪˌbrɛri/", "ببخشید، کتابخانه کجاست؟", "Excuse me, where is the pink library?", "ببخشید، کتابخانه صورتی کجاست؟", "📍", ClassroomType.CONVERSATION, "B2", "جمله سوالی جهت‌یابی.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_b2_1", QuestionType.TRANSLATION, "What is 'Library'?", "معنی Library چیست؟", listOf("کتابخانه", "فروشگاه", "ایستگاه", "کافه"), 0, "Library = کتابخانه")
            )
        ),
        Level(
            id = 6,
            levelCode = "B3",
            tierGroup = TierGroup.TIER_B,
            titleFa = "سطح B3: احساسات عمیق، امید و انرژی مثبت",
            titleEn = "B3: Deep Emotions & Radiant Hope",
            iconEmoji = "💖",
            descriptionFa = "بیان احساسات قلبی، تقویت خودباوری و ابراز محبت",
            accentColorHex = 0xFFFF85A1,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("b3_1", "I feel immensely grateful", "/aɪ fiːl ɪˈmɛnsli ˈɡreɪtfəl/", "من بی‌نهایت سپاسگزارم", "I feel immensely grateful for this journey.", "من بی‌نهایت سپاسگزار این مسیر زیبایم.", "💌", ClassroomType.VOCABULARY, "B3", "قید immensely برای تاکید بسیار بالا.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_b3_1", QuestionType.SPEAKING, "Say: 'I feel immensely grateful'", "بگو: 'I feel immensely grateful'", listOf("I feel immensely grateful", "Grateful I feel", "I am grateful", "Grateful"), 0, "تمرین تلفظ قید و صفت.", "I feel immensely grateful")
            )
        ),

        // === TIER C ===
        Level(
            id = 7,
            levelCode = "C1",
            tierGroup = TierGroup.TIER_C,
            titleFa = "سطح C1: زمان حال کامل (Present Perfect)",
            titleEn = "C1: Present Perfect & Life Milestones",
            iconEmoji = "🎀",
            descriptionFa = "بیان تجربیاتی که تاکنون در زندگی و خودسازی داشته‌اید",
            accentColorHex = 0xFF9D4EDD,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("c1_1", "I have accomplished so much", "/aɪ hæv əˈkɑːmplɪʃt soʊ mʌtʃ/", "من دستاوردهای فراوانی داشته‌ام", "I have accomplished so much this year.", "من امسال دستاوردهای فراوانی داشته‌ام.", "🏆", ClassroomType.GRAMMAR, "C1", "حال کامل: have + p.p.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_c1_1", QuestionType.GRAMMAR_FILL, "Present perfect: 'I _____ finished my lesson.'", "جای خالی:", listOf("have", "has", "am", "was"), 0, "برای I از have استفاده می‌شود.")
            )
        ),
        Level(
            id = 8,
            levelCode = "C2",
            tierGroup = TierGroup.TIER_C,
            titleFa = "سطح C2: جملات شرطی و استدلال نظرات",
            titleEn = "C2: Conditionals & Logical Reasoning",
            iconEmoji = "💡",
            descriptionFa = "تحلیل رویدادها، اگرها و بیان دیدگاه‌های منطقی",
            accentColorHex = 0xFFC77DFF,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("c2_1", "If you persist, you will succeed", "/ɪf juː pərˈsɪst juː wɪl səkˈsiːd/", "اگر پایداری کنی، موفق خواهی شد", "If you persist, you will succeed beyond words.", "اگر پایداری کنی، فراتر از تصور موفق می‌شوی.", "💎", ClassroomType.READING_WRITING, "C2", "شرطی نوع اول.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_c2_1", QuestionType.TRANSLATION, "Translate: 'If you persist, you will succeed'", "ترجمه:", listOf("اگر عجله کنی می‌رسی", "اگر پایداری کنی، موفق خواهی شد", "موفقیت دور است", "تمرین لازم نیست"), 1, "persist = پایداری ورزیدن")
            )
        ),
        Level(
            id = 9,
            levelCode = "C3",
            tierGroup = TierGroup.TIER_C,
            titleFa = "سطح C3: اصطلاحات روزمره و عبارات نیتیو",
            titleEn = "C3: Everyday Idioms & Natural Flow",
            iconEmoji = "☕",
            descriptionFa = "اصطلاحات رایج فیلم‌ها، آهنگ‌ها و گفتگوی روان انگلیسی",
            accentColorHex = 0xFFFF69B4,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("c3_1", "Piece of cake", "/piːs əv keɪk/", "مثل آب خوردن / خیلی آسان", "Learning this with Zahra is a piece of cake!", "یادگیری این با زهرا مثل آب خوردن است!", "🍰", ClassroomType.VOCABULARY, "C3", "اصطلاح بسیار پرکاربرد.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_c3_1", QuestionType.MULTIPLE_CHOICE, "What does 'Piece of cake' mean?", "معنی اصطلاح:", listOf("یک تکه کیک", "بسیار آسان و راحت", "سخت و دشوار", "شیرین و تلخ"), 1, "Piece of cake یعنی بسیار آسان.")
            )
        ),

        // === TIER D ===
        Level(
            id = 10,
            levelCode = "D1",
            tierGroup = TierGroup.TIER_D,
            titleFa = "سطح D1: فن بیان، اعتماد به نفس و لحن گیرا",
            titleEn = "D1: Confident Voice & Public Delivery",
            iconEmoji = "🎙️",
            descriptionFa = "نحوه ادای جملات با وقار، کنترل نفس و صدای رسا",
            accentColorHex = 0xFFFF7B54,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("d1_1", "Speak with unshakable poise", "/spiːk wɪð ʌnˈʃeɪkəbəl pɔɪz/", "با وقار و آرامش تزلزل‌ناپذیر سخن بگو", "Zahra speaks with unshakable poise and warmth.", "زهرا با وقاری تزلزل‌ناپذیر و گرمایی دلنشین سخن می‌گوید.", "👑", ClassroomType.PRONUNCIATION, "D1", "کلمه poise یعنی متانت و وقار.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_d1_1", QuestionType.SPEAKING, "Say: 'Speak with unshakable poise'", "با اعتماد به نفس بگو:", listOf("Speak with unshakable poise", "Poise speak", "With poise", "Unshakable"), 0, "تلفظ با لحن رسا.", "Speak with unshakable poise")
            )
        ),
        Level(
            id = 11,
            levelCode = "D2",
            tierGroup = TierGroup.TIER_D,
            titleFa = "سطح D2: داستان‌سرایی و انتقال هیجان کلامی",
            titleEn = "D2: Captivating Storytelling",
            iconEmoji = "📖",
            descriptionFa = "روایت جذاب اتفاقات و به وجد آوردن شنوندگان",
            accentColorHex = 0xFFFFB26B,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("d2_1", "Once upon a dream", "/wʌns əˈpɑːn ə driːm/", "روزی در دنیای رویاها", "Once upon a dream, she decided to conquer fluency.", "روزی در دنیای رویاها، او تصمیم گرفت زبان را فتح کند.", "✨", ClassroomType.READING_WRITING, "D2", "آغاز کلاسیک داستان‌گویی.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_d2_1", QuestionType.TRANSLATION, "Meaning of 'Once upon a dream':", "معنی عبارت:", listOf("یک بار در خواب", "روزی در دنیای رویاها", "همیشه در رویا", "خواب شیرین"), 1, "بیان شاعرانه داستانی.")
            )
        ),
        Level(
            id = 12,
            levelCode = "D3",
            tierGroup = TierGroup.TIER_D,
            titleFa = "سطح D3: کاهش لهجه و ادای فونتیک نیتیو",
            titleEn = "D3: Flawless Accent & Intonation",
            iconEmoji = "🗣️",
            descriptionFa = "تکیه هجاها (Stress) و ملودی گفتاری زبان انگلیسی",
            accentColorHex = 0xFFFF5C8D,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("d3_1", "Intonation creates music", "/ˌɪntəˈneɪʃən kriˈeɪts ˈmjuːzɪk/", "آهنگ کلام موسیقی می‌آفریند", "English intonation creates natural music in speech.", "آهنگ کلام انگلیسی در گفتار موسیقی طبیعی خلق می‌کند.", "🎵", ClassroomType.PRONUNCIATION, "D3", "تاکید بر ریتم کلام.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_d3_1", QuestionType.SPEAKING, "Say: 'Intonation creates music'", "بگو:", listOf("Intonation creates music", "Music intonation", "Intonation", "Creates music"), 0, "تمرین ملودی کلام.", "Intonation creates music")
            )
        ),

        // === TIER E ===
        Level(
            id = 13,
            levelCode = "E1",
            tierGroup = TierGroup.TIER_E,
            titleFa = "سطح E1: متون آکادمیک و درک مقالات",
            titleEn = "E1: Academic Reading & Precision",
            iconEmoji = "🏛️",
            descriptionFa = "خوانش مقالات علمی، روانشناسی و تحلیل گزاره‌ها",
            accentColorHex = 0xFF70D6FF,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("e1_1", "Empirical evidence suggests", "/ɛmˈpɪrɪkəl ˈɛvədəns səɡˈdʒɛsts/", "شواهد تجربی نشان می‌دهد که", "Empirical evidence suggests habits shape identity.", "شواهد تجربی نشان می‌دهد عادات هویت ما را شکل می‌دهند.", "📊", ClassroomType.READING_WRITING, "E1", "جمله ساختاری مقالات پژوهشی.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_e1_1", QuestionType.MULTIPLE_CHOICE, "What does 'Empirical' mean?", "معنی کلمه:", listOf("خیالی", "تجربی و مبتنی بر مشاهده", "ساده", "قدیمی"), 1, "Empirical = تجربی.")
            )
        ),
        Level(
            id = 14,
            levelCode = "E2",
            tierGroup = TierGroup.TIER_E,
            titleFa = "سطح E2: نامه‌نگاری رسمی و مهارت‌های حرفه‌ای",
            titleEn = "E2: Professional Correspondence",
            iconEmoji = "💼",
            descriptionFa = "ایمیل‌های تجاری، رزومه بین‌المللی و نامه‌های اداری",
            accentColorHex = 0xFFFF85A1,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("e2_1", "I look forward to collaborating", "/aɪ lʊk ˈfɔːrwərd tuː kəˈlæbəˌreɪtɪŋ/", "مشتاقانه چشم‌انتظار همکاری هستم", "I look forward to collaborating with your esteemed team.", "مشتاقانه منتظر همکاری با تیم محترم شما هستم.", "🤝", ClassroomType.READING_WRITING, "E2", "بعد از look forward to فعل ing می‌گیرد.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_e2_1", QuestionType.GRAMMAR_FILL, "Correct form: 'I look forward to _____ you.'", "شکل صحیح:", listOf("see", "seeing", "saw", "seen"), 1, "به علت حرف اضافه to در این عبارت، ing می‌آید.")
            )
        ),
        Level(
            id = 15,
            levelCode = "E3",
            tierGroup = TierGroup.TIER_E,
            titleFa = "سطح E3: مذاکره، اقناع و دیالوگ پیشرفته",
            titleEn = "E3: Persuasive Dialogue & High Stakes",
            iconEmoji = "👑",
            descriptionFa = "مذاکرات سطح بالا، اقناع شنوندگان و رهبری گفتگو",
            accentColorHex = 0xFFFF6584,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("e3_1", "Mutually beneficial outcome", "/ˈmjuːtʃuəli ˌbɛnəˈfɪʃəl ˈaʊtˌkʌm/", "نتیجه دارای سود دوطرفه (برد-برد)", "We aim for a mutually beneficial outcome.", "هدف ما دستیابی به نتیجه‌ای برد-برد است.", "💎", ClassroomType.CONVERSATION, "E3", "اصطلاح کلیدی مذاکرات.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_e3_1", QuestionType.TRANSLATION, "Translate: 'Mutually beneficial'", "ترجمه:", listOf("یک‌طرفه", "دارای سود دوطرفه (متقابل)", "زیان‌آور", "بیهوده"), 1, "دارای سود برای هر دو طرف.")
            )
        ),

        // === TIER F ===
        Level(
            id = 16,
            levelCode = "F1",
            tierGroup = TierGroup.TIER_F,
            titleFa = "سطح F1: اصطلاحات عامیانه معاصر (Modern Slang)",
            titleEn = "F1: Contemporary Native Slang",
            iconEmoji = "🕶️",
            descriptionFa = "کلمات و عبارات پرطرفدار شبکه‌های اجتماعی و فرهنگ جوانان",
            accentColorHex = 0xFFFF4D6D,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("f1_1", "No cap", "/noʊ kæp/", "بدون شوخی / واقعاً راست می‌گم", "Zahra is the most dedicated student, no cap!", "زهرا متعهدترین دانشجو است، بدون شوخی و کاملاً واقعی!", "🧢", ClassroomType.VOCABULARY, "F1", "اسلنگ پرکاربرد به معنی راستش را بخواهی.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_f1_1", QuestionType.MULTIPLE_CHOICE, "What does 'No cap' mean?", "معنی No cap:", listOf("بدون کلاه", "کاملاً جدی و بدون اغراق", "دروغ", "خواب‌آلود"), 1, "معنی حقیقت محض است.")
            )
        ),
        Level(
            id = 17,
            levelCode = "F2",
            tierGroup = TierGroup.TIER_F,
            titleFa = "سطح F2: ضرب‌المثل‌های پرکاربرد انگلیسی",
            titleEn = "F2: Timeless English Proverbs",
            iconEmoji = "📜",
            descriptionFa = "حکمت‌های کهن انگلیسی که در مکالمات بزرگان به کار می‌رود",
            accentColorHex = 0xFFFF758F,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("f2_1", "Where there's a will, there's a way", "/wɛər ðɛrz ə wɪl ðɛrz ə weɪ/", "خواستن، توانستن است", "Remember Zahra, where there's a will, there's a way.", "یادت باشد زهرا، خواستن توانستن است.", "🌟", ClassroomType.READING_WRITING, "F2", "ضرب‌المثل جاودانه اراده.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_f2_1", QuestionType.SPEAKING, "Say: 'Where there is a will there is a way'", "بگو:", listOf("Where there is a will there is a way", "Will is way", "Way and will", "Where will"), 0, "ضرب‌المثل انگیزشی.", "Where there is a will there is a way")
            )
        ),
        Level(
            id = 18,
            levelCode = "F3",
            tierGroup = TierGroup.TIER_F,
            titleFa = "سطح F3: شوخ‌طبعی و شوخی‌های ظریف انگلیسی",
            titleEn = "F3: Subtle Wit & Cultural Humor",
            iconEmoji = "🌸",
            descriptionFa = "درک طنز، شوخی‌های موقعیتی و شیرین‌زبانی در مکالمه",
            accentColorHex = 0xFFFF85A1,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("f3_1", "Lighthearted banter", "/ˈlaɪtˌhɑːrtəd ˈbæntər/", "گپ‌وگفت شوخ‌طبعانه و شیرین", "Engaging in lighthearted banter brightens the day.", "گپ‌وگفت صمیمی و شوخ‌طبعانه روز را روشن می‌کند.", "😄", ClassroomType.CONVERSATION, "F3", "واژه banter یعنی شوخی‌های دوستانه.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_f3_1", QuestionType.TRANSLATION, "Translate 'Banter':", "معنی:", listOf("مشاجره", "شوخی دوستانه و شوخ‌طبعی", "خشم", "سکوت"), 1, "گپ شیرین و شوخی.")
            )
        ),

        // === TIER G ===
        Level(
            id = 19,
            levelCode = "G1",
            tierGroup = TierGroup.TIER_G,
            titleFa = "سطح G1: روانشناسی خودسازی و ذهن‌آگاهی",
            titleEn = "G1: Mindfulness & Inner Growth",
            iconEmoji = "🧘‍♀️",
            descriptionFa = "یادگیری مفاهیم آرامش درون و صلح با خود به زبان انگلیسی",
            accentColorHex = 0xFFFF6584,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("g1_1", "Inner serenity is priceless", "/ˈɪnər səˈrɛnəti ɪz ˈpraɪsləs/", "آرامش درون ارزشی بی‌انتها دارد", "Cultivating inner serenity is the foundation of growth.", "پرورش آرامش درون پایه و اساس رشد است.", "🕊️", ClassroomType.READING_WRITING, "G1", "کلمه serenity یعنی آرامش ژرف.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_g1_1", QuestionType.SPEAKING, "Say: 'Inner serenity is priceless'", "بگو:", listOf("Inner serenity is priceless", "Serenity priceless", "Inner peace", "Priceless serenity"), 0, "آرامش درون.", "Inner serenity is priceless")
            )
        ),
        Level(
            id = 20,
            levelCode = "G2",
            tierGroup = TierGroup.TIER_G,
            titleFa = "سطح G2: انضباط فردی و مدیریت اهداف بزرگ",
            titleEn = "G2: Radical Self-Discipline",
            iconEmoji = "🎯",
            descriptionFa = "واژگان و گزاره‌های طلایی برای موفقیت‌های بزرگ روزانه",
            accentColorHex = 0xFFFF4D6D,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("g2_1", "Discipline equals absolute freedom", "/ˈdɪsəplɪn ˈiːkwəlz ˈæbsəluːt ˈfriːdəm/", "انضباط مساوی است با آزادی مطلق", "Discipline equals absolute freedom in life.", "انضباط فردی برابر با آزادی واقعی و مطلق است.", "🗝️", ClassroomType.CONVERSATION, "G2", "جمله انگیزشی معروف.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_g2_1", QuestionType.TRANSLATION, "Translate:", "ترجمه:", listOf("انضباط آزادی می‌آورد", "آزادی سخت است", "انضباط کار دشواری است", "تلاش بی‌فایده است"), 0, "انضباط مساوی آزادی است.")
            )
        ),
        Level(
            id = 21,
            levelCode = "G3",
            tierGroup = TierGroup.TIER_G,
            titleFa = "سطح G3: کتاب‌خوانی و خلاصه متون برتر جهان",
            titleEn = "G3: Global Wisdom & Literature",
            iconEmoji = "📚",
            descriptionFa = "خلاصه شاهکارهای رشد فردی دنیا به زبان انگلیسی",
            accentColorHex = 0xFFFF758F,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("g3_1", "Atomic habits create compounding results", "/əˈtɑːmɪk ˈhæbɪts kriˈeɪt kəmˈpaʊndɪŋ rɪˈzʌlts/", "عادت‌های کوچک نتایج شگفت‌انگیز مرکب می‌سازند", "Atomic habits create massive compounding results.", "عادت‌های اتمی نتایج تصاعدی فوق‌العاده می‌سازند.", "🌱", ClassroomType.READING_WRITING, "G3", "برگرفته از مفاهیم رشد فردی.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_g3_1", QuestionType.MULTIPLE_CHOICE, "What does 'Compounding' mean?", "معنی compounding:", listOf("ساده", "تصاعدی و فزاینده مرکب", "نابودشونده", "ثابت"), 1, "اثر مرکب تصاعدی.")
            )
        ),

        // === TIER Z: ULTIMATE MASTERY (Z1 to Z9) ===
        Level(
            id = 22,
            levelCode = "Z1",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z1: مکالمه بی‌درنگ و ناخودآگاه",
            titleEn = "Z1: Spontaneous Fluidity",
            iconEmoji = "⚡",
            descriptionFa = "پاسخگویی سریع بدون نیاز به ترجمه در ذهن",
            accentColorHex = 0xFFFF5C8D,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z1_1", "Spontaneous and effortless", "/spɑːnˈteɪniəs ænd ˈɛfərtləs/", "بداهه، روان و بدون تلاش اضافی", "Her English is spontaneous and effortless.", "انگلیسی او روان و بی‌نیاز از فکر کردن است.", "🌸", ClassroomType.CONVERSATION, "Z1", "تسلط سطح Z1.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z1_1", QuestionType.SPEAKING, "Say: 'Spontaneous and effortless'", "بگو:", listOf("Spontaneous and effortless", "Effortless", "Spontaneous", "Fluent"), 0, "تسلط بی‌درنگ.", "Spontaneous and effortless")
            )
        ),
        Level(
            id = 23,
            levelCode = "Z2",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z2: درک کامل تمام لهجه‌های بین‌المللی",
            titleEn = "Z2: Global Accent Mastery",
            iconEmoji = "🌍",
            descriptionFa = "درک روان لهجه‌های بریتانیایی، آمریکایی، استرالیایی و کانادایی",
            accentColorHex = 0xFFFF6584,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z2_1", "A cosmopolitan perspective", "/ə ˌkɑːzməˈpɑːlətən pərˈspɛktɪv/", "دیدگاه جهان‌شمول و بین‌المللی", "Zahra embraces a cosmopolitan perspective.", "زهرا دیدگاهی بین‌المللی و جهان‌وطنی دارد.", "🗺️", ClassroomType.LISTENING, "Z2", "واژه سطح بالا.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z2_1", QuestionType.TRANSLATION, "Cosmopolitan means:", "معنی:", listOf("محلی", "جهان‌شمول و بین‌المللی", "ساده", "روستایی"), 1, "بین‌المللی و جهان‌وطنی.")
            )
        ),
        Level(
            id = 24,
            levelCode = "Z3",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z3: فصاحت و شیوایی شاعرانه (Articulacy)",
            titleEn = "Z3: Poetic Eloquence",
            iconEmoji = "🌹",
            descriptionFa = "استفاده از زیباترین کلمات و آرایه‌های ادبی در کلام روزمره",
            accentColorHex = 0xFFFF85A1,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z3_1", "Eloquence of thought", "/ˈɛləkwəns əv θɔːt/", "فصاحت و شیوایی اندیشه", "Her words carry the eloquence of thought.", "کلمات او نشان‌دهنده شیوایی تفکرش است.", "✨", ClassroomType.PRONUNCIATION, "Z3", "شیوایی و فصاحت.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z3_1", QuestionType.SPEAKING, "Say: 'Eloquence of thought'", "بگو:", listOf("Eloquence of thought", "Thought eloquence", "Eloquence", "Thought"), 0, "فصاحت اندیشه.", "Eloquence of thought")
            )
        ),
        Level(
            id = 25,
            levelCode = "Z4",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z4: بداهه‌گویی و پاسخ در هر موقعیتی",
            titleEn = "Z4: Improvisational Mastery",
            iconEmoji = "🪄",
            descriptionFa = "توانایی صحبت درباره هر موضوع ناشناخته با کلمات فاخر",
            accentColorHex = 0xFFFF758F,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z4_1", "Impeccable impromptu speech", "/ɪmˈpɛkəbəl ɪmˈprɑːmptuː spiːtʃ/", "سخنرانی بداهه و بی‌نقص", "She delivered an impeccable impromptu speech.", "او یک سخنرانی بداهه و کاملاً بی‌نقص ارائه داد.", "🎤", ClassroomType.CONVERSATION, "Z4", "کلمه impeccable یعنی بی‌نقص.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z4_1", QuestionType.TRANSLATION, "Impeccable means:", "معنی:", listOf("دارای نقص", "بی‌نقص و عالی", "کوتاه", "کند"), 1, "کاملاً بی‌نقص.")
            )
        ),
        Level(
            id = 26,
            levelCode = "Z5",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z5: مناظره، منطق و تحلیل فلسفی",
            titleEn = "Z5: Philosophical Debate & Logic",
            iconEmoji = "⚖️",
            descriptionFa = "بیان استدلال‌های محکم در برابر بزرگ‌ترین متفکران انگلیسی‌زبان",
            accentColorHex = 0xFF9D4EDD,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z5_1", "Intellectual rigor", "/ˌɪntəˈlɛktʃuəl ˈrɪɡər/", "دقت و انسجام فکری عمیق", "Her arguments possess intellectual rigor.", "استدلال‌های او دارای انسجام و دقت فکری عمیقی است.", "🧠", ClassroomType.READING_WRITING, "Z5", "انسجام عقلی و منطقی.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z5_1", QuestionType.MULTIPLE_CHOICE, "Rigor implies:", "کلمه Rigor دلالت دارد بر:", listOf("بی‌دقتی", "دقت و استواری بالا", "سستی", "ضعف"), 1, "دقت و استحکام.")
            )
        ),
        Level(
            id = 27,
            levelCode = "Z6",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z6: تدریس و مربی‌گری به زبان انگلیسی",
            titleEn = "Z6: Mentorship & Pedagogy",
            iconEmoji = "🎓",
            descriptionFa = "توانایی آموزش مفاهیم پیچیده به دیگران با زبانی شیرین",
            accentColorHex = 0xFFFF69B4,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z6_1", "Inspiring the next generation", "/ɪnˈspaɪərɪŋ ðə nɛkst ˌdʒɛnəˈreɪʃən/", "الهام‌بخشی به نسل آینده", "Zahra inspires through teaching and empathy.", "زهرا از طریق آموزش و همدلی الهام‌بخش است.", "🌟", ClassroomType.CONVERSATION, "Z6", "الهام‌بخشی به دیگران.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z6_1", QuestionType.SPEAKING, "Say: 'Inspiring the next generation'", "بگو:", listOf("Inspiring the next generation", "Inspire next", "Generation next", "Inspire"), 0, "الهام‌بخشی.", "Inspiring the next generation")
            )
        ),
        Level(
            id = 28,
            levelCode = "Z7",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z7: تفکر مستقیم به زبان انگلیسی",
            titleEn = "Z7: Direct English Cognition",
            iconEmoji = "💭",
            descriptionFa = "جایی که مکالمات درونی ذهن شما نیز انگلیسی می‌شود",
            accentColorHex = 0xFFFF5C8D,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z7_1", "Thinking without translation", "/ˈθɪŋkɪŋ wɪˈðaʊt trænzˈleɪʃən/", "اندیشیدن بدون ترجمه ذهنی", "She thinks directly in English with pure clarity.", "او مستقیماً و با شفافیت ناب به انگلیسی فکر می‌کند.", "💡", ClassroomType.VOCABULARY, "Z7", "تفکر مستقیم.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z7_1", QuestionType.TRANSLATION, "Translate:", "ترجمه:", listOf("تفکر با ترجمه", "اندیشیدن بدون ترجمه ذهنی", "فراموشی", "سکوت"), 1, "تفکر بدون ترجمه.")
            )
        ),
        Level(
            id = 29,
            levelCode = "Z8",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z8: پیوند فرهنگی عمیق و زبان دوزبانه بومی",
            titleEn = "Z8: True Bilingual Eminence",
            iconEmoji = "💎",
            descriptionFa = "درک طنزهای تاریخی، ادبیات فاخر و لحن شکوهمند نیتیو",
            accentColorHex = 0xFFFF85A1,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z8_1", "Bilingual mastery of the highest order", "/baɪˈlɪŋɡwəl ˈmæstəri əv ðə ˈhaɪɪst ˈɔːrdər/", "تسلط دوزبانه در بالاترین رده ممکن", "Zahra attained bilingual mastery of the highest order.", "زهرا به تسلط دوزبانه در والاترین مرتبه ممکن دست یافت.", "👑", ClassroomType.CONVERSATION, "Z8", "بالاترین رده تسلط.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z8_1", QuestionType.SPEAKING, "Say: 'Bilingual mastery of the highest order'", "بگو:", listOf("Bilingual mastery of the highest order", "Bilingual mastery", "Highest order", "Mastery"), 0, "تسلط دوزبانه.", "Bilingual mastery of the highest order")
            )
        ),
        Level(
            id = 30,
            levelCode = "Z9",
            tierGroup = TierGroup.TIER_Z,
            titleFa = "سطح Z9: تاج نهایی تسلط مطلق و استادی زهرا 👑",
            titleEn = "Z9: The Crown of Supreme Mastery",
            iconEmoji = "👑",
            descriptionFa = "قله بی‌انتهای خودسازی: «از صفر شروع کردی و به تسلط کامل رسیدی!»",
            accentColorHex = 0xFFFFD166,
            isUnlocked = false,
            bestScore = 0,
            starsEarned = 0,
            items = listOf(
                LessonItem("z9_1", "From zero to absolute mastery", "/frʌm ˈzɪroʊ tuː ˈæbsəluːt ˈmæstəri/", "از صفر تا تسلط مطلق و نهایی", "Start from zero, continue until absolute mastery!", "از صفر شروع کن، تا تسلط کامل ادامه بده!", "🏆", ClassroomType.CONVERSATION, "Z9", "شعار رسمی آکادمی اختصاصی BH.")
            ),
            examQuestions = listOf(
                QuizQuestion("q_z9_1", QuestionType.SPEAKING, "Recite our motto: 'Start from zero, continue until absolute mastery!'", "شعار پرشکوه آکادمی را بخوان:", listOf("Start from zero, continue until absolute mastery!", "Start from zero", "Continue until absolute mastery", "Zahra is queen"), 0, "شعار ماندگار آکادمی اختصاصی BH.", "Start from zero continue until absolute mastery")
            )
        )
    )

    // Continuous Word Game: 30 Stages
    val wordGameStages: List<GameStage> = listOf(
        GameStage(1, "PINK", "رنگ زیبای مورد علاقه دخترها (صورتی)", "🌸", listOf('K', 'P', 'N', 'I')),
        GameStage(2, "GROW", "رشد کردن و هر روز بهتر شدن", "🌱", listOf('W', 'G', 'R', 'O')),
        GameStage(3, "SMILE", "لبخند درخشان و پرانرژی", "😊", listOf('E', 'L', 'I', 'M', 'S')),
        GameStage(4, "SHINE", "درخشش و پرتوافشانی دائمی", "✨", listOf('E', 'S', 'N', 'I', 'H')),
        GameStage(5, "FLOWER", "شکوفه بهاری خوشبو", "🌷", listOf('R', 'E', 'W', 'O', 'L', 'F')),
        GameStage(6, "CROWN", "تاج پادشاهی و پیروزی", "👑", listOf('N', 'C', 'W', 'R', 'O')),
        GameStage(7, "BUTTERFLY", "پروانه زیبای بال‌زن", "🦋", listOf('Y', 'B', 'U', 'T', 'T', 'E', 'R', 'F', 'L')),
        GameStage(8, "PRINCESS", "پرنسس مهربان و شیک", "👸", listOf('S', 'P', 'R', 'I', 'N', 'C', 'E', 'S')),
        GameStage(9, "BRAVE", "شجاع، نترس و مصمم", "🦁", listOf('E', 'B', 'R', 'A', 'V')),
        GameStage(10, "MAGIC", "جادو و شگفتی در یادگیری", "🪄", listOf('C', 'M', 'A', 'G', 'I')),
        GameStage(11, "PEACH", "هلوی آبدار تابستانی", "🍑", listOf('H', 'C', 'A', 'E', 'P')),
        GameStage(12, "DREAM", "رویاهای بزرگ و دست‌یافتنی", "💭", listOf('M', 'D', 'R', 'E', 'A')),
        GameStage(13, "SWEET", "شیرین، دلنشین و پفکی", "🧁", listOf('T', 'S', 'W', 'E', 'E')),
        GameStage(14, "FLUFFY", "نرم، پشمالو و ابرمانند", "☁️", listOf('Y', 'F', 'L', 'U', 'F', 'F')),
        GameStage(15, "ADORABLE", "بسیار دوست‌داشتنی و ملوس", "🐱", listOf('E', 'A', 'D', 'O', 'R', 'A', 'B', 'L')),
        GameStage(16, "SPARKLE", "برق‌برق زدن و تلألؤ الماس", "💎", listOf('E', 'S', 'P', 'A', 'R', 'K', 'L')),
        GameStage(17, "FOCUS", "تمرکز عمیق روی اهداف خودسازی", "🎯", listOf('S', 'F', 'C', 'U', 'O')),
        GameStage(18, "MASTER", "تسلط کامل و استادی", "🎓", listOf('R', 'M', 'T', 'A', 'E', 'S')),
        GameStage(19, "SUCCESS", "موفقیت درخشان و چشمگیر", "🏆", listOf('S', 'S', 'U', 'C', 'C', 'E', 'S')),
        GameStage(20, "VICTORY", "پیروزی شیرین پس از تلاش", "✌️", listOf('Y', 'V', 'I', 'C', 'T', 'O', 'R')),
        GameStage(21, "BRILLIANT", "فوق‌العاده، درخشان و باهوش", "💡", listOf('T', 'B', 'R', 'I', 'L', 'L', 'I', 'A', 'N')),
        GameStage(22, "PERSIST", "استمرار ورزیدن و پایداری", "🔥", listOf('T', 'P', 'E', 'R', 'S', 'I', 'S')),
        GameStage(23, "ELOQUENT", "فصیح و خوش‌بیان", "🎙️", listOf('T', 'E', 'L', 'O', 'Q', 'U', 'E', 'N')),
        GameStage(24, "HARMONY", "هماهنگی و آرامش درون", "🕊️", listOf('Y', 'H', 'A', 'R', 'M', 'O', 'N')),
        GameStage(25, "RADIANT", "تابان، درخشان و سرشار از نور", "☀️", listOf('T', 'R', 'A', 'D', 'I', 'A', 'N')),
        GameStage(26, "TRIUMPH", "فتح قله‌ها و افتخار بزرگ", "🌟", listOf('H', 'T', 'U', 'R', 'I', 'P', 'M')),
        GameStage(27, "QUEEN", "ملکه خودسازی و سرنوشت خود", "👑", listOf('N', 'Q', 'U', 'E', 'E')),
        GameStage(28, "WISDOM", "خرد و دانایی جاودان", "🦉", listOf('M', 'W', 'D', 'I', 'S', 'O')),
        GameStage(29, "EXCELLENCE", "تعالی، برتری و بالاترین کیفیت", "🥇", listOf('E', 'E', 'X', 'C', 'E', 'L', 'L', 'E', 'N', 'C')),
        GameStage(30, "INFINITY", "بی‌انتها بودن توانمندی‌های تو", "♾️", listOf('Y', 'I', 'N', 'F', 'I', 'N', 'I', 'T')),
        GameStage(31, "APPLE", "میوهٔ سیب", "🍎", listOf('P', 'L', 'E', 'A', 'P')),
        GameStage(32, "BOOK", "کتاب برای خواندن", "📚", listOf('O', 'K', 'B', 'O')),
        GameStage(33, "WATER", "مایع ضروری برای زندگی", "💧", listOf('T', 'E', 'R', 'W', 'A')),
        GameStage(34, "FRIEND", "کسی که با او دوستی داری", "🤝", listOf('I', 'E', 'N', 'D', 'F', 'R')),
        GameStage(35, "FAMILY", "افراد نزدیک و عزیز خانه", "👨‍👩‍👧", listOf('M', 'I', 'L', 'Y', 'F', 'A')),
        GameStage(36, "SCHOOL", "جایی برای یادگیری", "🏫", listOf('H', 'O', 'O', 'L', 'S', 'C')),
        GameStage(37, "HAPPY", "شاد و خوشحال", "😊", listOf('P', 'P', 'Y', 'H', 'A')),
        GameStage(38, "STRONG", "نیرومند و باقدرت", "💪", listOf('R', 'O', 'N', 'G', 'S', 'T')),
        GameStage(39, "LEARN", "چیزی را آموختن", "🧠", listOf('A', 'R', 'N', 'L', 'E')),
        GameStage(40, "CREATE", "چیزی تازه ساختن یا آفریدن", "🎨", listOf('E', 'A', 'T', 'E', 'C', 'R')),
        GameStage(41, "COURAGE", "شجاعت در زمان دشواری", "🦁", listOf('U', 'R', 'A', 'G', 'E', 'C', 'O')),
        GameStage(42, "HABIT", "کاری که مرتب تکرار می‌شود", "🔁", listOf('B', 'I', 'T', 'H', 'A')),
        GameStage(43, "GOAL", "هدفی که می‌خواهی به آن برسی", "🎯", listOf('A', 'L', 'G', 'O')),
        GameStage(44, "HEALTH", "وضعیت سلامت بدن و ذهن", "🌿", listOf('A', 'L', 'T', 'H', 'H', 'E')),
        GameStage(45, "KINDNESS", "مهربانی با دیگران", "💗", listOf('N', 'D', 'N', 'E', 'S', 'S', 'K', 'I')),
        GameStage(46, "PATIENCE", "توانایی آرام ماندن و صبر کردن", "🌸", listOf('T', 'I', 'E', 'N', 'C', 'E', 'P', 'A')),
        GameStage(47, "JOURNAL", "دفتر ثبت فکرها و رویدادها", "📔", listOf('U', 'R', 'N', 'A', 'L', 'J', 'O')),
        GameStage(48, "MORNING", "بخش آغازین روز", "🌅", listOf('R', 'N', 'I', 'N', 'G', 'M', 'O')),
        GameStage(49, "ENERGY", "توان و نیروی انجام کار", "⚡", listOf('E', 'R', 'G', 'Y', 'E', 'N')),
        GameStage(50, "CONFIDENT", "باور داشتن به توانایی خود", "🌟", listOf('N', 'F', 'I', 'D', 'E', 'N', 'T', 'C', 'O')),
        GameStage(51, "DISCIPLINE", "پایبندی منظم به تصمیم‌ها", "📅", listOf('S', 'C', 'I', 'P', 'L', 'I', 'N', 'E', 'D', 'I')),
        GameStage(52, "MINDFUL", "با توجه و آگاهی از لحظه اکنون", "🧘", listOf('N', 'D', 'F', 'U', 'L', 'M', 'I')),
        GameStage(53, "GROWTH", "رشد و بهتر شدن", "🌱", listOf('O', 'W', 'T', 'H', 'G', 'R')),
        GameStage(54, "RESILIENT", "توان بازگشت پس از دشواری", "🌳", listOf('S', 'I', 'L', 'I', 'E', 'N', 'T', 'R', 'E')),
        GameStage(55, "MOTIVATION", "انگیزه برای آغاز و ادامه کار", "🔥", listOf('T', 'I', 'V', 'A', 'T', 'I', 'O', 'N', 'M', 'O')),
        GameStage(56, "GRATITUDE", "سپاسگزاری از داشته‌ها", "🙏", listOf('A', 'T', 'I', 'T', 'U', 'D', 'E', 'G', 'R')),
        GameStage(57, "CONSISTENT", "منظم و پیوسته در انجام کار", "⏳", listOf('N', 'S', 'I', 'S', 'T', 'E', 'N', 'T', 'C', 'O')),
        GameStage(58, "ACHIEVE", "به دست آوردن یک هدف", "🏅", listOf('H', 'I', 'E', 'V', 'E', 'A', 'C')),
        GameStage(59, "TRANSFORM", "دگرگون و متحول شدن", "🦋", listOf('A', 'N', 'S', 'F', 'O', 'R', 'M', 'T', 'R')),
        GameStage(60, "BALANCE", "تعادل میان بخش‌های زندگی", "⚖️", listOf('L', 'A', 'N', 'C', 'E', 'B', 'A'))
    )
}
