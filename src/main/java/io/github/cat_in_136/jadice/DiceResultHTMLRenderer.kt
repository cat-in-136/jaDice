package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.SimpleHTMLStreamWriter


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

    fun renderDiceResultData(result: List<DiceResultData>, writer: SimpleHTMLStreamWriter) {
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
                        writer.characters(data.trans, true)
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
                    writer.characters("More...")
                    writer.endElement()
                    writer.endElement()
                }
                DiceResultData.DiceResultDataMode.FOOTER -> {
                    writer.startElement("div")
                    writer.characters("from ${data.index.toString()}")
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
}