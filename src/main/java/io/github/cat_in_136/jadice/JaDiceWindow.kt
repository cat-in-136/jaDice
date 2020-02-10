package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.SimpleHTMLStreamWriter
import io.github.cat_in_136.misc.TimedTextChangeAdapter
import java.awt.BorderLayout
import java.net.URL
import java.net.URLEncoder
import java.util.prefs.PreferenceChangeListener
import javax.swing.*
import javax.swing.event.ChangeListener
import javax.swing.event.HyperlinkEvent
import javax.swing.text.Element
import javax.swing.text.html.HTMLDocument


class JaDiceWindow(private val diceWorker: DiceWorker) : JFrame() {

    private val searchTextBox = JTextField()
    private val hamburgerButton = JButton()
    private val resultView = JEditorPane()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        createUIComponents()
        pack()
    }

    private fun createUIComponents() {
        val rootPane = JPanel(BorderLayout())

        rootPane.layout = BorderLayout(0, 0)
        val topBar = JPanel()
        topBar.layout = BorderLayout(0, 0)
        rootPane.add(topBar, BorderLayout.NORTH)
        topBar.add(searchTextBox, BorderLayout.CENTER)
        hamburgerButton.text = "≡"
        topBar.add(hamburgerButton, BorderLayout.EAST)
        val scrollPane1 = JScrollPane()
        rootPane.add(scrollPane1, BorderLayout.CENTER)
        resultView.contentType = "text/html"
        resultView.isEditable = false
        resultView.text = """
                <h1>jaDice</h1>
                <p>jaDice is the viewer for dictionaries of PDIC format.</p>
            """.trimIndent()
        scrollPane1.setViewportView(resultView)

        val popupMenu = JPopupMenu()
        popupMenu.add("Setting").addActionListener {
            JaDicePreferencePane(diceWorker).showDialog(this)
        }

        val timedTextChangeAdapter = TimedTextChangeAdapter(
                DicePreferenceService.prefSearchForDelay,
                ChangeListener {
                    diceWorker.search(searchTextBox.text).thenApply {
                        setTextToResultView(it)
                    }
                })
        searchTextBox.document.addDocumentListener(timedTextChangeAdapter)
        hamburgerButton.addActionListener {
            popupMenu.show(hamburgerButton, 0, hamburgerButton.height)
        }
        resultView.addHyperlinkListener { event ->
            if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                val sourceElement = event.sourceElement.parentElement
                val (command, argument) = parseCommandLink(event.url)

                when (command) {
                    "more" -> argument?.toIntOrNull(10)?.also { dic ->
                        diceWorker.moreResults(dic).thenApply {
                            replaceElementOnResultView(it, sourceElement)
                        }
                    }
                }
            }
        }

        DicePreferenceService.addPreferenceChangeListener(PreferenceChangeListener {
            if (it.key == DicePreferenceService.PREF_DELAY_FOR_SEARCH) {
                timedTextChangeAdapter.delay = DicePreferenceService.prefSearchForDelay
            }
        })

        contentPane = rootPane
    }

    private fun setTextToResultView(result: List<DiceResultData>) {
        val strOut = StringBuilder()
        val writer = SimpleHTMLStreamWriter(strOut, true)

        writer.startElement("html")
        writer.startElement("head")
        writer.endElement()
        writer.startElement("body")

        convertDiceResultDataToHtmlString(result, writer)

        writer.endElement()
        writer.endElement()

        val html = strOut.toString()
        SwingUtilities.invokeLater {
            resultView.text = html
            resultView.select(0, 0)
        }
    }

    private fun replaceElementOnResultView(result: List<DiceResultData>, element: Element) {
        val strOut = StringBuilder()
        val writer = SimpleHTMLStreamWriter(strOut, true)

        convertDiceResultDataToHtmlString(result, writer)

        val html = strOut.toString()
        SwingUtilities.invokeLater {
            val document = element.document as HTMLDocument
            val offset = element.startOffset
            document.setOuterHTML(element, html)
            resultView.select(offset, offset)
        }
    }

    private fun convertDiceResultDataToHtmlString(result: List<DiceResultData>, writer: SimpleHTMLStreamWriter) {
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
                    writer.startElement("a", mapOf("href" to generateCommandLink("more", data.dic.toString())))
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

    private fun generateCommandLink(command: String, arguments: String): String {
        return "https://0.0.0.0/" +
                URLEncoder.encode(command, "UTF-8") +
                "/" +
                URLEncoder.encode(arguments, "UTF-8")
    }

    private fun parseCommandLink(url: URL): Pair<String?, String?> {
        return if ((url.protocol == "https") && (url.host == "0.0.0.0") && (url.path.startsWith("/"))) {
            val array = url.file.substring(1).split("/")
            Pair(array.getOrNull(0), array.getOrNull(1))
        } else {
            Pair(null, null)
        }
    }
}