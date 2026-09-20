package com.example.data

enum class Language(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val scriptName: String,
    val flagOrIcon: String
) {
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिन्दी",
        scriptName = "Devanagari",
        flagOrIcon = "🇮🇳"
    ),
    SANTALI_OL_CHIKI(
        code = "sat",
        displayName = "Santali (Ol Chiki)",
        nativeName = "ᱥᱟᱱᱛᱟᱲᱤ",
        scriptName = "ᱚᱞ ᱪᱤᱠᱤ",
        flagOrIcon = "🌸"
    ),
    SANTALI_LATIN(
        code = "sat-lat",
        displayName = "Santali (Latin)",
        nativeName = "Santali",
        scriptName = "Roman",
        flagOrIcon = "🔤"
    ),
    HO(
        code = "hoc",
        displayName = "Ho",
        nativeName = "ᱦᱳ / हो",
        scriptName = "Varang Kshiti / Devanagari",
        flagOrIcon = "🌿"
    ),
    MUNDARI(
        code = "unr",
        displayName = "Mundari",
        nativeName = "ᱢᱩᱱᱰᱟᱹᱨᱤ / मुंडारी",
        scriptName = "Mundari Bani / Devanagari",
        flagOrIcon = "🍃"
    ),
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        scriptName = "Latin",
        flagOrIcon = "🌐"
    );

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: HINDI
        }
    }
}
