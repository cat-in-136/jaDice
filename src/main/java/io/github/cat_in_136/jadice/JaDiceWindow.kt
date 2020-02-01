package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.PlaceholderLayerUI
import io.github.cat_in_136.misc.TimedTextChangeAdapter
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.event.ChangeListener

class JaDiceWindow : JFrame() {

    val worker = DiceWorker()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        val rootPane = JPanel(BorderLayout())

        val toolBar = JToolBar()
        toolBar.isFloatable = false
        rootPane.add(toolBar, BorderLayout.NORTH)

        val popupMenu = JPopupMenu()
        popupMenu.add("Setting")

        val searchTextBox = JTextField()
        searchTextBox.document.addDocumentListener(TimedTextChangeAdapter(100, ChangeListener {
            val value = searchTextBox.text
            val result = worker.search(value).get()
            println(result)
        }))
        toolBar.add(JLayer(searchTextBox, PlaceholderLayerUI("Search")))
        toolBar.addSeparator()
        val menuButton = JButton("\u22EE")
        menuButton.addActionListener {
            popupMenu.show(menuButton, 0, menuButton.height)
        }
        toolBar.add(menuButton)

        val resultView = JTextPane()
        resultView.contentType = "text/html"
        resultView.text = """
            <h1>jaDice</h1>
            <p>jaDice is the viewer for dictionaries of PDIC format.</p>
        """.trimIndent()
        resultView.isEditable = false
        val resultViewScrollPane = JScrollPane(resultView)
        rootPane.add(resultViewScrollPane, BorderLayout.CENTER)

        contentPane = rootPane
        pack()
    }
}