package io.github.cat_in_136.misc

fun escapeHtml(text: CharSequence, breakLine: Boolean = false): String = text.replace(Regex("[&\"<>\n]")) {
    return@replace when (it.value) {
        "&" -> "&amp;"
        "\"" -> "&quot;"
        "<" -> "&lt;"
        ">" -> "&gt;"
        "\n" -> if (breakLine) {
            "<br>\n"
        } else {
            "\n"
        }
        else -> it.value
    }
}