package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.PlaceholderLayerUI
import io.github.cat_in_136.misc.TimedTextChangeAdapter
import io.github.cat_in_136.misc.escapeHtml
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.event.ChangeListener

class JaDiceWindow : JFrame() {
    private val worker = DiceWorker()

    private val searchTextBox = JTextField()
    private val resultView = JEditorPane().apply {
        this.contentType = "text/html"
        this.isEditable = false
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        val rootPane = JPanel(BorderLayout())

        val toolBar = JToolBar()
        toolBar.isFloatable = false
        rootPane.add(toolBar, BorderLayout.NORTH)

        val popupMenu = JPopupMenu()
        popupMenu.add("Setting")

        searchTextBox.document.addDocumentListener(TimedTextChangeAdapter(100, ChangeListener {
            val value = searchTextBox.text
            val result = worker.search(value).get()

            setTextToResultView(result)
        }))
        toolBar.add(JLayer(searchTextBox, PlaceholderLayerUI("Search")))
        toolBar.addSeparator()
        val menuButton = JButton("\u22EE")
        menuButton.addActionListener {
            popupMenu.show(menuButton, 0, menuButton.height)
        }
        toolBar.add(menuButton)

        resultView.text = """
            <h1>jaDice</h1>
            <p>jaDice is the viewer for dictionaries of PDIC format.</p>
        """.trimIndent()
        val resultViewScrollPane = JScrollPane(resultView)
        rootPane.add(resultViewScrollPane, BorderLayout.CENTER)

        contentPane = rootPane
        pack()
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