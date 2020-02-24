package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.SimpleHTMLStreamWriter
import java.io.File
import java.util.*
import java.util.regex.Pattern


class DiceResultHTMLRenderer(private val generateCommandLinkFunc: (String, String) -> String) {

    fun convertDiceResultDataToHtml(result: List<DiceResultData>): String {
        val strOut = StringBuilder()
        val writer = SimpleHTMLStreamWriter(strOut, true)

        writer.startElement("html")
        writer.startElement("head")
        writer.endElement()
        writer.startElement("body")

        renderDiceResultData(result, writer)

        writer.endElement()
        writer.endElement()

        return strOut.toString()
    }

    fun convertDiceResultDataToPartialHtml(result: List<DiceResultData>): String {
        val strOut = StringBuilder()
        val writer = SimpleHTMLStreamWriter(strOut, true)

        renderDiceResultData(result, writer)

        return strOut.toString()
    }

    private fun renderDiceResultData(result: List<DiceResultData>, writer: SimpleHTMLStreamWriter) {
        for (data in result) {
            when (data.mode) {
                DiceResultData.DiceResultDataMode.WORD -> {
                    writer.startElement("div")
                    writer.startElement("h3")
                    writer.characters(data.index ?: "")
                    writer.endElement()
                    if (data.phone != null) {
                        writer.startElement("div", mapOf("style" to "margin-bottom: 3ex"))
                        writer.characters(data.phone)
                        writer.endElement()
                    }
                    if (data.trans != null) {
                        writer.startElement("div")
                        renderTransTest(data.trans, writer)
//                        writer.characters(data.trans, true)
                        writer.endElement()
                    }
                    if (data.sample != null) {
                        writer.startElement("div")
                        writer.characters(data.sample, true)
                        writer.endElement()
                    }
                    writer.endElement()
                }
                DiceResultData.DiceResultDataMode.MORE -> {
                    writer.startElement("div")
                    writer.startElement("a", mapOf(
                            "href" to generateCommandLinkFunc("more", data.dic.toString())
                    ))
                    writer.characters(bundle.getString("result.more"))
                    writer.endElement()
                    writer.endElement()
                }
                DiceResultData.DiceResultDataMode.FOOTER -> {
                    val dicPath = data.index.toString()
                    val dicName = dicPath.split(File.separatorChar).last()
                    writer.startElement("div")
                    writer.characters(String.format(bundle.getString("result.reference"), dicName))
                    writer.startElement("small")
                    writer.characters("($dicPath)")
                    writer.endElement()
                    writer.endElement()
                    writer.emptyElement("hr")
                }
                else -> {
                    writer.startElement("div")
                    writer.characters(data.index ?: "")
                    writer.endElement()
                }
            }
        }
    }

    private fun renderTransTest(text: CharSequence, writer: SimpleHTMLStreamWriter) {
        val pattern = Pattern.compile(
                arrayOf(
                        "<(→(?<eijiro>.+?))>", // EIJORO-style "<→word>"
                        "(→　(?<waeijiro>.+))", // WAEIJIRO-style "→　word"
                        "(＝(?<ryakujiro>.+))●" // RYAKUJIRO-style "＝word●"
                ).joinToString("|"))
        val matcher = pattern.matcher(text)

        var pos = 0
        while (matcher.find()) {
            val groupName = arrayOf("eijiro", "waeijiro", "ryakujiro").find {
                try {
                    matcher.group(it)
                    true
                } catch (e: IllegalArgumentException) {
                    false
                }
            }!!
            val keyword = matcher.group(groupName)
            val start = matcher.start()
            val groupStart = matcher.start(groupName)
            val groupEnd = matcher.end(groupName)
            val end = matcher.end()

            writer.characters(text.subSequence(pos, start), true) // $`
            writer.characters(text.subSequence(start, groupStart), false)
            writer.startElement("a", mapOf("href" to generateCommandLinkFunc("preference.search", keyword)))
            writer.characters(keyword, false)
            writer.endElement()
            writer.characters(text.subSequence(groupEnd, end), false)

            pos = end
        }
        writer.characters(text.subSequence(pos, text.length), true)
    }

    companion object {
        private val bundle = ResourceBundle.getBundle("jadice")
    }
}