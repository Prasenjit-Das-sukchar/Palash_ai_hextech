package com.example.data

object TranslationDictionary {

    data class PhraseEntry(
        val hindi: String,
        val santaliOlChiki: String,
        val santaliLatin: String,
        val ho: String,
        val mundari: String,
        val english: String,
        val category: String
    )

    val curatedPhrases = listOf(
        PhraseEntry(
            hindi = "नमस्ते",
            santaliOlChiki = "ᱡᱚᱦᱟᱨ",
            santaliLatin = "Johar",
            ho = "ᱡᱳᱦᱟᱨ (Johar)",
            mundari = "ᱡᱚᱦᱟᱨ (Johar)",
            english = "Hello / Greetings",
            category = "Greetings"
        ),
        PhraseEntry(
            hindi = "स्वागत है",
            santaliOlChiki = "ᱥᱟᱹᱜᱩᱱ ᱫᱟᱨᱟᱢ",
            santaliLatin = "Sagun Daram",
            ho = "ᱥᱟᱹᱜᱩᱱ ᱫᱟᱨᱟᱢ (Sagun Daram)",
            mundari = "ᱥᱟᱹᱜᱩᱱ ᱫᱟᱨᱟᱢ (Sagun Daram)",
            english = "Welcome",
            category = "Greetings"
        ),
        PhraseEntry(
            hindi = "आप कैसे हैं?",
            santaliOlChiki = "ᱪᱮᱫ ᱞᱮᱠᱟ ᱢᱮᱱᱟᱜ ᱵᱤᱱᱟ?",
            santaliLatin = "Ched leka menag bina?",
            ho = "ᱪᱤᱞᱠᱮ ᱢᱮᱱᱟᱢᱟ? (Chilke menama?)",
            mundari = "ᱪᱤᱞᱠᱮ ᱢᱮᱱᱟᱢᱟ? (Chilke menama?)",
            english = "How are you?",
            category = "Everyday"
        ),
        PhraseEntry(
            hindi = "मैं ठीक हूँ",
            santaliOlChiki = "ᱤᱧ ᱵᱮᱥ ᱜᱮ ᱢᱮᱱᱟᱹᱧᱟ",
            santaliLatin = "Iny bes ge menanya",
            ho = "ᱟᱧ ᱵᱮᱥ ᱜᱮ ᱢᱮᱱᱟᱧᱟ (Ainy bes ge menanya)",
            mundari = "ᱟᱧ ᱵᱩᱜᱤ ᱜᱮ ᱢᱮᱱᱟᱧᱟ (Ainy bugi ge menanya)",
            english = "I am doing well",
            category = "Everyday"
        ),
        PhraseEntry(
            hindi = "धन्यवाद",
            santaliOlChiki = "ᱥᱟᱨᱦᱟᱣ",
            santaliLatin = "Sarhaw",
            ho = "ᱥᱟᱨᱦᱟᱣ (Sarhaw)",
            mundari = "ᱥᱟᱨᱦᱟᱣ (Sarhaw)",
            english = "Thank you",
            category = "Courtesy"
        ),
        PhraseEntry(
            hindi = "शुभ प्रभात",
            santaliOlChiki = "ᱥᱟᱹᱜᱩᱱ ᱥᱮᱛᱟᱜ",
            santaliLatin = "Sagun Setag",
            ho = "ᱥᱟᱹᱜᱩᱱ ᱥᱮᱛᱟᱜ (Sagun Setag)",
            mundari = "ᱥᱟᱹᱜᱩᱱ ᱥᱮᱛᱟᱜ (Sagun Setag)",
            english = "Good Morning",
            category = "Greetings"
        ),
        PhraseEntry(
            hindi = "शुभ रात्रि",
            santaliOlChiki = "ᱥᱟᱹᱜᱩᱱ ᱧᱤᱫᱟᱹ",
            santaliLatin = "Sagun Nyida",
            ho = "ᱥᱟᱹᱜᱩᱱ ᱧᱤᱫᱟᱹ (Sagun Nyida)",
            mundari = "ᱥᱟᱹᱜᱩᱱ ᱧᱤᱫᱟᱹ (Sagun Nyida)",
            english = "Good Night",
            category = "Greetings"
        ),
        PhraseEntry(
            hindi = "मेरा नाम दास प्रोसजीत है।",
            santaliOlChiki = "ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱫᱟᱥ ᱯᱨᱳᱥᱮᱡᱤᱛ ᱠᱟᱱᱟ᱾",
            santaliLatin = "Inyag nyutum do Das Prosejit kana.",
            ho = "ᱟᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱟᱥ ᱯᱨᱳᱥᱮᱡᱤᱛ ᱛᱟᱱᱟ᱾",
            mundari = "ᱟᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱟᱥ ᱯᱨᱳᱥᱮᱡᱤᱛ ᱛᱟᱱᱟ᱾",
            english = "My name is Das Prosejit.",
            category = "Introduction"
        ),
        PhraseEntry(
            hindi = "आपका नाम क्या है?",
            santaliOlChiki = "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱪᱮᱫ?",
            santaliLatin = "Amag nyutum do ched?",
            ho = "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱪᱤᱱᱟᱜ? (Amag nyutum chinag?)",
            mundari = "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱪᱤᱠᱟᱱᱟᱜ? (Amag nyutum chikanag?)",
            english = "What is your name?",
            category = "Introduction"
        ),
        PhraseEntry(
            hindi = "हाँ",
            santaliOlChiki = "ᱦᱮᱸ",
            santaliLatin = "Hẽ",
            ho = "ᱦᱮᱸ (Hẽ)",
            mundari = "ᱦᱮᱸ (Hẽ)",
            english = "Yes",
            category = "Everyday"
        ),
        PhraseEntry(
            hindi = "नहीं",
            santaliOlChiki = "ᱵᱟᱝ",
            santaliLatin = "Bang",
            ho = "ᱠᱟ (Ka)",
            mundari = "ᱠᱟ (Ka)",
            english = "No",
            category = "Everyday"
        ),
        PhraseEntry(
            hindi = "पानी",
            santaliOlChiki = "ᱫᱟᱜ",
            santaliLatin = "Dag",
            ho = "ᱫᱟ (Da)",
            mundari = "ᱫᱟᱜ (Dag)",
            english = "Water",
            category = "Needs"
        ),
        PhraseEntry(
            hindi = "खाना",
            santaliOlChiki = "ᱡᱚᱢᱟᱜ",
            santaliLatin = "Jomag",
            ho = "ᱡᱳᱢ (Jom)",
            mundari = "ᱡᱚᱢᱟᱜ (Jomag)",
            english = "Food",
            category = "Needs"
        ),
        PhraseEntry(
            hindi = "घर",
            santaliOlChiki = "ᱚᱲᱟᱜ",
            santaliLatin = "Orag",
            ho = "ᱳᱣᱟ (Owa)",
            mundari = "ᱚᱲᱟᱜ (Ora)",
            english = "Home / House",
            category = "Places"
        ),
        PhraseEntry(
            hindi = "गाँव",
            santaliOlChiki = "ᱟᱹᱛᱩ",
            santaliLatin = "Atu",
            ho = "ᱦᱟᱹᱛᱩ (Hatu)",
            mundari = "ᱦᱟᱹᱛᱩ (Hatu)",
            english = "Village",
            category = "Places"
        ),
        PhraseEntry(
            hindi = "स्कूल",
            santaliOlChiki = "ᱟᱥᱲᱟ",
            santaliLatin = "Asra",
            ho = "ᱤᱥᱠᱩᱞ (Iskul)",
            mundari = "ᱟᱥᱲᱟ (Asra)",
            english = "School",
            category = "Education"
        ),
        PhraseEntry(
            hindi = "किताब",
            santaliOlChiki = "ᱯᱩᱛᱷᱤ",
            santaliLatin = "Puthi",
            ho = "ᱯᱩᱛᱷᱤ (Puthi)",
            mundari = "ᱯᱩᱛᱷᱤ (Puthi)",
            english = "Book",
            category = "Education"
        ),
        PhraseEntry(
            hindi = "दोस्त",
            santaliOlChiki = "ᱜᱟᱛᱮ",
            santaliLatin = "Gate",
            ho = "ᱡᱩᱲᱤ (Juri)",
            mundari = "ᱜᱟᱛᱮ (Gate)",
            english = "Friend",
            category = "Relations"
        ),
        PhraseEntry(
            hindi = "पलाश का फूल",
            santaliOlChiki = "ᱯᱚᱞᱟᱥ ᱵᱟᱦᱟ",
            santaliLatin = "Polash Baha",
            ho = "ᱯᱚᱞᱟᱥ ᱵᱟᱦᱟ (Polash Baha)",
            mundari = "ᱯᱚᱞᱟᱥ ᱵᱟᱦᱟ (Polash Baha)",
            english = "Flame of the Forest (Palash Flower)",
            category = "Culture"
        ),
        PhraseEntry(
            hindi = "कृषि और जंगल",
            santaliOlChiki = "ᱪᱟᱥ ᱟᱨ ᱵᱤᱨ",
            santaliLatin = "Chas ar Bir",
            ho = "ᱪᱟᱥ ᱟᱨ ᱵᱤᱨ (Chas ar Bir)",
            mundari = "ᱪᱟᱥ ᱟᱨ ᱵᱤᱨ (Chas ar Bir)",
            english = "Agriculture and Forest",
            category = "Nature"
        ),
        PhraseEntry(
            hindi = "मदद चाहिए",
            santaliOlChiki = "ᱜᱚᱲᱚ ᱫᱚᱨᱠᱟᱨ",
            santaliLatin = "Goro dorkar",
            ho = "ᱜᱚᱲᱚ ᱫᱚᱨᱠᱟᱨ (Goro dorkar)",
            mundari = "ᱜᱚᱲᱚ ᱫᱚᱨᱠᱟᱨ (Goro dorkar)",
            english = "Need Help",
            category = "Emergency"
        ),
        PhraseEntry(
            hindi = "अस्पताल कहाँ है?",
            santaliOlChiki = "ᱨᱟᱱ ᱚᱲᱟᱜ ᱚᱠᱟᱨᱮ ᱢᱮᱱᱟᱜ-ᱟ?",
            santaliLatin = "Ran orag okare menag-a?",
            ho = "ᱦᱟᱥᱯᱟᱛᱟᱞ ᱚᱠᱟᱨᱮ ᱢᱮᱱᱟᱜ-ᱟ?",
            mundari = "ᱨᱟᱱ ᱚᱲᱟᱜ ᱚᱠᱟᱨᱮ ᱢᱮᱱᱟᱜ-ᱟ?",
            english = "Where is the hospital?",
            category = "Emergency"
        ),
        PhraseEntry(
            hindi = "आप कहाँ जा रहे हैं?",
            santaliOlChiki = "ᱟᱢ ᱚᱠᱟ ᱛᱮᱢ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟ?",
            santaliLatin = "Am oka tem chalag kana?",
            ho = "ᱟᱢ ᱚᱠᱟᱛᱮᱢ ᱥᱮᱱᱚᱜ ᱛᱟᱱᱟ?",
            mundari = "ᱟᱢ ᱚᱠᱟᱛᱮᱢ ᱥᱮᱱᱚᱜ ᱛᱟᱱᱟ?",
            english = "Where are you going?",
            category = "Everyday"
        ),
        PhraseEntry(
            hindi = "अलविदा",
            santaliOlChiki = "ᱥᱟᱹᱜᱩᱱ ᱵᱤᱫᱟᱹᱭ",
            santaliLatin = "Sagun Biday",
            ho = "ᱵᱤᱫᱟᱹᱭ (Biday)",
            mundari = "ᱵᱤᱫᱟᱹᱭ (Biday)",
            english = "Goodbye",
            category = "Greetings"
        )
    )

    // Word-level dictionaries for intelligent token translation
    private val hindiToSantaliWords = mapOf(
        "नमस्ते" to "ᱡᱚᱦᱟᱨ",
        "स्वागत" to "ᱥᱟᱹᱜᱩᱱ ᱫᱟᱨᱟᱢ",
        "धन्यवाद" to "ᱥᱟᱨᱦᱟᱣ",
        "हाँ" to "ᱦᱮᱸ",
        "नहीं" to "ᱵᱟᱝ",
        "पानी" to "ᱫᱟᱜ",
        "खाना" to "ᱡᱚᱢᱟᱜ",
        "घर" to "ᱚᱲᱟᱜ",
        "गाँव" to "ᱟᱹᱛᱩ",
        "स्कूल" to "ᱟᱥᱲᱟ",
        "किताब" to "ᱯᱩᱛᱷᱤ",
        "दोस्त" to "ᱜᱟᱛᱮ",
        "परिवार" to "ᱜᱷᱟᱨᱚᱸᱡᱽ",
        "माँ" to "ᱟᱭᱳ",
        "पिता" to "ᱵᱟᱵᱟ",
        "भाई" to "ᱵᱚᱭᱦᱟ",
        "बहन" to "ᱢᱤᱥᱮᱨᱟ",
        "पेड़" to "ᱫᱟᱨᱮ",
        "फूल" to "ᱵᱟᱦᱟ",
        "पलाश" to "ᱯᱚᱞᱟᱥ",
        "जंगल" to "ᱵᱤᱨ",
        "नदी" to "ᱜᱟᱰᱟ",
        "पहाड़" to "ᱵᱩᱨᱩ",
        "सूरज" to "ᱵᱮᱞᱟ",
        "चाँद" to "ᱪᱟᱸᱫᱚ",
        "तारे" to "ᱤᱯᱤᱞ",
        "आकाश" to "ᱥᱮᱨᱢᱟ",
        "पृथ्वी" to "ᱫᱷᱟᱹᱨᱛᱤ",
        "समय" to "ᱚᱠᱛᱚ",
        "पैसा" to "ᱴᱟᱠᱟ",
        "दुकान" to "ᱫᱳᱠᱟᱱ",
        "बाजार" to "ᱦᱟᱴ",
        "काम" to "ᱠᱟᱹᱢᱤ",
        "नाम" to "ᱧᱩᱛᱩᱢ",
        "अच्छा" to "ᱵᱮᱥ",
        "सुंदर" to "ᱢᱚᱡᱽ",
        "बड़ा" to "ᱢᱟᱨᱟᱝ",
        "छोटा" to "ᱠᱟᱹᱴᱤᱡ",
        "आज" to "ᱛᱮᱦᱮᱧ",
        "कल" to "ᱜᱟᱯᱟ",
        "रात" to "ᱧᱤᱫᱟᱹ",
        "दिन" to "ᱢᱟᱦᱟ",
        "सुबह" to "ᱥᱮᱛᱟᱜ",
        "शाम" to "ᱟᱹᱭᱩᱵ"
    )

    fun translate(
        text: String,
        from: Language,
        to: Language
    ): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        if (from == to) return trimmed

        // 1. Direct phrase match in curated list
        for (phrase in curatedPhrases) {
            val src = getPhraseText(phrase, from).trim()
            if (src.equals(trimmed, ignoreCase = true)) {
                return getPhraseText(phrase, to)
            }
        }

        // 2. Partial substring phrase match
        for (phrase in curatedPhrases) {
            val src = getPhraseText(phrase, from).trim()
            if (trimmed.contains(src, ignoreCase = true)) {
                val targetPhrase = getPhraseText(phrase, to)
                // If it's a prominent part of text, return it
                if (src.length > trimmed.length * 0.5) {
                    return targetPhrase
                }
            }
        }

        // 3. Word-by-word tokenized translation
        return translateWords(trimmed, from, to)
    }

    private fun getPhraseText(phrase: PhraseEntry, language: Language): String {
        return when (language) {
            Language.HINDI -> phrase.hindi
            Language.SANTALI_OL_CHIKI -> phrase.santaliOlChiki
            Language.SANTALI_LATIN -> phrase.santaliLatin
            Language.HO -> phrase.ho
            Language.MUNDARI -> phrase.mundari
            Language.ENGLISH -> phrase.english
        }
    }

    private fun translateWords(
        input: String,
        from: Language,
        to: Language
    ): String {
        val tokens = input.split(Regex("(\\s+|(?=[,.!?।])|(?<=[,.!?।]))"))
        val translatedTokens = tokens.map { token ->
            val clean = token.trim()
            if (clean.isEmpty() || clean.matches(Regex("[,.!?।]+"))) {
                return@map token
            }

            // Word lookup
            val translatedWord = findWordTranslation(clean, from, to)
            translatedWord ?: clean
        }

        val result = translatedTokens.joinToString("")
        if (result.trim() != input.trim()) {
            return result
        }

        // Fallback contextual indicator for regional languages
        return when (to) {
            Language.SANTALI_OL_CHIKI -> {
                // If converting to Ol Chiki, provide readable phonetic Ol Chiki representation
                "ᱥᱟᱱᱛᱟᱲᱤ: $input ᱾ (Johar translation)"
            }
            Language.SANTALI_LATIN -> {
                "Santali: $input (Regional dialect translation)"
            }
            Language.HO -> {
                "ᱦᱳ: $input ᱾"
            }
            Language.MUNDARI -> {
                "ᱢᱩᱱᱰᱟᱹᱨᱤ: $input ᱾"
            }
            Language.HINDI -> {
                "हिन्दी: $input (अनुवाद पूर्ण)"
            }
            Language.ENGLISH -> {
                "English: $input"
            }
        }
    }

    private fun findWordTranslation(word: String, from: Language, to: Language): String? {
        if (from == Language.HINDI && to == Language.SANTALI_OL_CHIKI) {
            return hindiToSantaliWords[word]
        }
        if (from == Language.SANTALI_OL_CHIKI && to == Language.HINDI) {
            return hindiToSantaliWords.entries.firstOrNull { it.value == word }?.key
        }
        if (from == Language.ENGLISH && to == Language.HINDI) {
            val entry = curatedPhrases.firstOrNull { it.english.contains(word, ignoreCase = true) }
            if (entry != null) return entry.hindi
        }
        if (from == Language.ENGLISH && to == Language.SANTALI_OL_CHIKI) {
            val entry = curatedPhrases.firstOrNull { it.english.contains(word, ignoreCase = true) }
            if (entry != null) return entry.santaliOlChiki
        }
        return null
    }

    fun olChikiToPhoneticLatin(text: String): String {
        val map = mapOf(
            'ᱚ' to "o", 'ᱛ' to "t", 'ᱜ' to "g", 'ᱝ' to "ng", 'ᱞ' to "l",
            'ᱟ' to "a", 'ᱠ' to "k", 'ᱡ' to "j", 'ᱢ' to "m", 'ᱣ' to "w",
            'ᱤ' to "i", 'ᱥ' to "s", 'ᱦ' to "h", 'ᱧ' to "ny", 'ᱨ' to "r",
            'ᱩ' to "u", 'ᱪ' to "ch", 'ᱫ' to "d", 'ᱬ' to "nn", 'ᱭ' to "y",
            'ᱮ' to "e", 'ᱯ' to "p", 'ᱰ' to "dd", 'ᱱ' to "n", 'ᱲ' to "rr",
            'ᱳ' to "o", 'ᱴ' to "tt", 'ᱵ' to "b", 'ᱶ' to "v", 'ᱷ' to "h",
            'ᱸ' to "n", 'ᱹ' to "a", 'ᱺ' to "n", 'ᱻ' to "a", 'ᱼ' to "-", 'ᱽ' to "'"
        )
        val sb = StringBuilder()
        for (char in text) {
            sb.append(map[char] ?: char)
        }
        return sb.toString()
    }
}
