package io.github.cat_in_136.jadice

data class DiceResultData(val mode: DiceResultDataMode,
                          val dic: Int,
                          val index: CharSequence? = null,
                          val phone: CharSequence? = null,
                          val trans: CharSequence? = null,
                          val sample: CharSequence? = null,
                          val indexSize: Int = 0,
                          val phoneSize: Int = 0,
                          val transSize: Int = 0,
                          val sampleSize: Int = 0) {

    enum class DiceResultDataMode {
        WORD, MORE, NONE, NORESULT, FOOTER
    }
}