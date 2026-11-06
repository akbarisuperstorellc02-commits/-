package com.example.data

import com.example.model.ChatMessage
import java.util.UUID

object MasterGohariAI {

    // In-memory tracked weak points for Zahra
    private val recordedWeakPoints = mutableSetOf<String>()

    data class TutorResponse(
        val english: String,
        val persian: String,
        val grammarTip: String? = null,
        val weaknessNoted: String? = null,
        val quickReplies: List<String> = emptyList()
    )

    fun getInitialGreeting(): ChatMessage {
        return ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromTutor = true,
            englishText = "Welcome to Zahra English Academy, dear Zahra! 👑 I am your dedicated private AI tutor. Our motto is: 'Start from zero, continue until absolute mastery!' I will walk with you patiently, step-by-step from zero to C2. Shall we start with Phonics and sounds or would you like to review your foundations?",
            persianTranslation = "به آکادمی زبان انگلیسی زهرا خوش آمدی زهرای عزیزم! 👑 من معلم خصوصی هوش مصنوعی تو هستم. شعار ما این است: «از صفر شروع کن، تا تسلط کامل ادامه بده!» من با صبوری، احترام و گام‌به‌گام از صفر تا سطح C2 کنارت خواهم بود. دوست داری با حروف و فونیکس شروع کنیم یا مباحث پایه را با هم مرور کنیم؟",
            grammarTipFa = "نکته استاد: یادگیری واقعی با تکرار مداوم و بدون ترس از اشتباه شکل می‌گیرد. من همیشه در کنارت هستم.",
            quickReplies = listOf(
                "از صفر برام فونیکس رو بگو 🔤",
                "درس امروز رو از اول توضیح بده 📖",
                "یک تمرین برای نقاط ضعف من بده 🎯",
                "Let's practice conversation! 🎙️"
            )
        )
    }

    fun generateReply(userMessage: String, currentCefrLevel: Int = 0): ChatMessage {
        val cleanInput = userMessage.trim().lowercase()

        val response = when {
            // Request to explain from scratch ("از اول توضیح بده")
            cleanInput.contains("از اول") || cleanInput.contains("از صفر") || cleanInput.contains("توضیح مجدد") || cleanInput.contains("explain from start") -> {
                TutorResponse(
                    english = "With greatest pleasure, Zahra! Let us start from the absolute ground up. Every English word is made of sounds. For example, letter 'A' sounds like /æ/ in Apple, and 'SH' makes /ʃ/ like in Shine. Listen carefully: 'Zahra will shine like pure gold!' Now you repeat after me.",
                    persian = "با کمال میل و با تمام صبوری زهرای عزیزم! بیا از نقطه صفرِ صفر شروع کنیم. هر کلمه انگلیسی از صداها تشکیل شده است. مثلاً حرف A صدای «اَ» می‌دهد مانند Apple، و ترکیب S و H همیشه صدای «ش» می‌دهد مانند Shine (درخشیدن). با دقت گوش بده: 'زهرا مانند طلای ناب خواهد درخشید!' حالا تو بعد از من تکرار کن.",
                    grammarTip = "قانون فونیکس: هرگز بدون یادگیری دقیق صدای حروف به مرحله بعد نرو. آواشناسی ریشه لهجه درست است.",
                    quickReplies = listOf("A is for Apple 🍎", "SH makes /ʃ/ in Shine ✨", "یک مثال دیگر برای من بزن 💡")
                )
            }

            // Request for weak points practice
            cleanInput.contains("ضعف") || cleanInput.contains("اشتباه") || cleanInput.contains("تمرین اختصاصی") || cleanInput.contains("weak") -> {
                val weaknessesStr = if (recordedWeakPoints.isEmpty()) {
                    "تا این لحظه عملکردت فوق‌العاده بوده! اما روی افعال زمان گذشته و تلفظ صدای TH تمرین بیشتری ثبت کرده‌ایم."
                } else {
                    "نقاط ضعفی که در حافظه دارم: " + recordedWeakPoints.joinToString("، ")
                }
                TutorResponse(
                    english = "I remember your learning journey precisely. As your tutor, I never rush until you reach 100% mastery. Here is a custom practice drill: say 'Think through the plan, Zahra!'",
                    persian = "من مسیر یادگیری تو را به دقت در حافظه دارم. به عنوان معلم خصوصی‌ات، هرگز پیش از یادگیری واقعی به درس بعد نمی‌رویم. $weaknessesStr. بیا این تمرین اختصاصی را تکرار کنیم: 'Think through the plan, Zahra!'",
                    grammarTip = "تلفظ TH: نوک زبانت را به آرامی بین دندان‌ها قرار بده تا صدایی شبیه «ث» اما با لطافت خارج شود.",
                    quickReplies = listOf("Think through the plan 💡", "یک تمرین گرامر هم بده 📖", "چطور تلفظم را بهتر کنم؟ 🗣️")
                )
            }

            // Common Grammar / Sentence Errors correction respectfully
            cleanInput.contains("i is") || cleanInput.contains("he are") || cleanInput.contains("she have") || cleanInput.contains("they is") -> {
                recordedWeakPoints.add("تطابق فاعل و فعل To Be")
                TutorResponse(
                    english = "You expressed a wonderful thought, Zahra! Allow me to respectfully fine-tune the grammar: with 'I', we always use 'am' (I am). With 'He/She', we use 'is' and 'has'. Let's say together: 'I am strong, and she has wisdom.'",
                    persian = "ایده بسیار زیبایی را بیان کردی زهرای عزیزم! با احترام اجازه بده یک تصحیح کوچک در گرامر انجام دهیم: با فاعل 'I' همیشه از 'am' استفاده می‌کنیم (من هستم)، و برای سوم شخص از 'is' و 'has'. بیا با هم این جمله درست را تکرار کنیم: 'I am strong, and she has wisdom.' (من قوی هستم و او دانایی دارد.)",
                    grammarTip = "نکته تصحیح: تطابق فاعل و فعل کلید یک مکالمه فاخر و اشرافی است.",
                    weaknessNoted = "افعال ربطی To Be",
                    quickReplies = listOf("I am strong 👑", "She has wisdom 💎", "متشکرم که اشتباهم را گفتی 🌸")
                )
            }

            // Greetings & Respectful conversation
            cleanInput.contains("hello") || cleanInput.contains("hi") || cleanInput.contains("سلام") -> {
                TutorResponse(
                    english = "Greetings, my esteemed student Zahra! 👑 Every single minute you invest in self-development elevates your future. What specific skill shall we conquer today: Phonics, Grammar, or Fluency?",
                    persian = "درود بر شاگرد فرهیخته و پرتلاشم، زهرا! 👑 هر دقیقه‌ای که برای خودسازی و یادگیری زبان می‌گذاری، آینده‌ات را درخشان‌تر می‌کند. امروز روی کدام مهارت متمرکز شویم: کلاس فونیکس، گرامر، یا مکالمه روان؟",
                    grammarTip = "استمرار روزانه: حتی ۱۵ دقیقه مطالعه با تمرکز، معجزه می‌کند.",
                    quickReplies = listOf("کلاس جامع گرامر 📖", "کلاس مکالمه و اسپیکینگ 🎙️", "از سطح صفر فونیکس شروع کنیم 🔤")
                )
            }

            // Slogan & Motivation
            cleanInput.contains("شعار") || cleanInput.contains("هدف") || cleanInput.contains("انگیزه") || cleanInput.contains("motto") -> {
                TutorResponse(
                    english = "Our golden motto is: 'Start from zero, continue until absolute mastery!' Remember Zahra, masters were once beginners who never quit. I believe in your royal potential.",
                    persian = "شعار زرین آکادمی ما: «از صفر شروع کن، تا تسلط کامل ادامه بده!» یادت باشد زهرای من، بزرگ‌ترین اساتید جهان روزی از نقطه صفر شروع کرده‌اند. من به پتانسیل درخشان تو ایمان دارم.",
                    grammarTip = "شعار آکادمی زهرا: Start from zero, continue until absolute mastery!",
                    quickReplies = listOf("از صفر شروع کن، تا تسلط کامل ادامه بده! 🏆", "Let's study now! 📚")
                )
            }

            // Grammar classroom
            cleanInput.contains("grammar") || cleanInput.contains("گرامر") -> {
                TutorResponse(
                    english = "Welcome to the Grammar Mastery Class! English grammar has a logical and beautiful architecture. We begin with Present Simple: 'Subject + Verb'. Example: 'Zahra studies with dedication.' Notice how 'studies' has an 'es' because Zahra is third person singular.",
                    persian = "به کلاس جامع گرامر خوش آمدی! گرامر انگلیسی ساختاری بسیار منظم و زیبا دارد. ما با زمان حال ساده شروع می‌کنیم: «فاعل + فعل». مثال: 'Zahra studies with dedication' (زهرا با تعهد مطالعه می‌کند). دقت کن که برای سوم شخص به فعل پسوند اضافه می‌شود.",
                    grammarTip = "قاعده طلایی: برای He, She, It فعل پسوند s یا es می‌گیرد.",
                    quickReplies = listOf("یک مثال دیگر بزن 💡", "چطور منفی‌اش کنم؟ ❓", "یک کوئیز ازم بگیر 🎯")
                )
            }

            // Conversation & Speaking practice
            cleanInput.contains("conversation") || cleanInput.contains("speaking") || cleanInput.contains("مکالمه") || cleanInput.contains("صحبت") -> {
                TutorResponse(
                    english = "Step into the Royal Conversation Room, Zahra. Tell me: 'What is your greatest goal for this year?' Speak into the microphone and I will listen and evaluate your pronunciation with precision.",
                    persian = "وارد اتاق مکالمه سلطنتی شو، زهرای عزیز. به انگلیسی بهم بگو: 'بزرگ‌ترین هدف امسالت چیه؟' دکمه میکروفون را بزن و با صدای رسا بیان کن تا نمره و تحلیل تلفظت را بهت بگویم.",
                    grammarTip = "جمله‌سازی: My greatest goal is to become completely fluent in English.",
                    quickReplies = listOf("My greatest goal is mastery 👑", "I want to speak fluently 🌟", "Ready to practice mic 🎙️")
                )
            }

            // Default encouraging progression
            else -> {
                TutorResponse(
                    english = "Well spoken, Zahra! In our academy, every word you produce is a step towards absolute fluency. Would you like me to quiz you on this concept or should we dive into our next drill?",
                    persian = "بسیار شیوا و عالی گفتی زهرای عزیز! در آکادمی ما، هر کلمه‌ای که ادا می‌کنی یک گام محکم به سوی تسلط کامل است. مایل هستی یک کوئیز کوچک از این مبحث بگیریم یا تمرین بعدی را آغاز کنیم؟",
                    grammarTip = "توصیه معلم: کلمات جدید را در دفترچه یادداشت خودسازی‌ات بنویس تا ماندگار شوند.",
                    quickReplies = listOf("یک سوال امتحانی بپرس 🎯", "درس را با جزییات بیشتر باز کن 📖", "Let's review my streak 🔥")
                )
            }
        }

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromTutor = true,
            englishText = response.english,
            persianTranslation = response.persian,
            grammarTipFa = response.grammarTip,
            weaknessFlagged = response.weaknessNoted,
            quickReplies = response.quickReplies
        )
    }
}
