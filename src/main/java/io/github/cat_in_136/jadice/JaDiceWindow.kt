package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.TimedTextChangeAdapter
import io.github.cat_in_136.misc.escapeHtml
import java.awt.BorderLayout
import java.util.prefs.PreferenceChangeListener
import javax.swing.*
import javax.swing.event.ChangeListener


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
                    diceWorker.search(searchTextBox.text).whenComplete { result, throwable ->
                        if (throwable != null) {
                            throw throwable
                        }
                        if (result != null) {
                            setTextToResultView(result)
                        }
                    }
                })
        searchTextBox.document.addDocumentListener(timedTextChangeAdapter)
        hamburgerButton.addActionListener {
            popupMenu.show(hamburgerButton, 0, hamburgerButton.height)
        }

        DicePreferenceService.addPreferenceChangeListener(PreferenceChangeListener {
            if (it.key == DicePreferenceService.PREF_DELAY_FOR_SEARCH) {
                timedTextChangeAdapter.delay = DicePreferenceService.prefSearchForDelay
            }
        })

        contentPane = rootPane
    }

    private fun setTextToResultView(result: List<DiceResultData>) {
        val items = result.map {
            when (it.mode) {
                DiceResultData.DiceResultDataMode.WORD -> """
                    <div>
                        <h3>${escapeHtml(it.index ?: "")}</h3>
                        <div>
                            ${escapeHtml(it.trans ?: "", true)}
                        </div>
                    </div>
                """.trimIndent()
                DiceResultData.DiceResultDataMode.MORE -> """
                    <div>
                        <a href="about:blank">More...</a><!-- TODO -->
                    </div>
                """.trimIndent()
                DiceResultData.DiceResultDataMode.FOOTER -> """
                    <div>from ${escapeHtml(it.index ?: "")}</div>
                    <hr>
                """.trimIndent()
                else -> """
                    <div>${escapeHtml(it.index ?: "")}</div>
                """.trimIndent()
            }
        }.joinToString("")

        SwingUtilities.invokeLater {
            resultView.text = """
                <html>
                    <head></head>
                    <body>
                        ${items}
                    </body>
                </html>
            """.trimIndent()
            resultView.select(0, 0)
        }
    }
}