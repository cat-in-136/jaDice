package io.github.cat_in_136.misc

import java.util.*

class SimpleHTMLStreamWriter(private val writer: Appendable, private val xmlStyle: Boolean = false) {
    private val tagStack = LinkedList<CharSequence>()

    fun writeStartElement(name: CharSequence, attrs: Map<String, String> = mapOf()) {
        writer.append('<')
        writer.append(name)
        for ((attr, value) in attrs) {
            writer.append(' ')
            writer.append(attr)
            writer.append("=\"")
            writer.append(escapeHtml(value))
            writer.append("\"")
        }
        writer.append('>')

        tagStack.push(name)
    }

    fun writeEndElement() {
        val name = tagStack.pop()

        writer.append("</")
        writer.append(name)
        writer.append('>')
    }

    fun writeEmptyElement(name: CharSequence, attrs: Map<String, String> = mapOf()) {
        writer.append('<')
        writer.append(name)
        for ((attr, value) in attrs) {
            writer.append(' ')
            writer.append(attr)
            writer.append("=\"")
            writer.append(escapeHtml(value))
            writer.append("\"")
        }
        writer.append(if (xmlStyle) {
            " />"
        } else {
            ">"
        })
    }

    fun writeCharacters(text: CharSequence, breakLine: Boolean = false) {
        writer.append(escapeHtml(text, breakLine))
    }

//    fun writeComment(comment: CharSequence) {
//        writer.append("<!-- ")
//        writer.append(comment)
//        writer.append(" -->")
//    }

    private fun escapeHtml(text: CharSequence, breakLine: Boolean = false): String = text.replace(Regex("[&\"<>\n]")) {
        return@replace when (it.value) {
            "&" -> "&amp;"
            "\"" -> "&quot;"
            "<" -> "&lt;"
            ">" -> "&gt;"
            "\n" -> if (breakLine) {
                if (xmlStyle) {
                    "<br />"
                } else {
                    "<br>"
                }
            } else {
                "\n"
            }
            else -> it.value
        }
    }
}