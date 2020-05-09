package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.AWTEventQueueTaskExecutor
import io.github.cat_in_136.misc.ClipboardWatcher
import io.github.cat_in_136.misc.SimpleAction
import io.github.cat_in_136.misc.TimedTextChangeAdapter
import jp.sblo.pandora.dice.DiceFactory
import java.awt.*
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import java.util.function.Consumer
import java.util.prefs.PreferenceChangeListener
import javax.swing.*
import javax.swing.event.ChangeListener
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLDocument


class JaDiceWindow(private val diceWorker: DiceWorker) : JFrame() {

    private val toolBar = JToolBar()
    private val searchTextBox = JTextField()
    private val resultView = JEditorPane()
    private val renderer = DiceResultHTMLRenderer(this::generateCommandLink)

    private val eventQueueExecutor = AWTEventQueueTaskExecutor()
    private val clipboardWatcher = ClipboardWatcher(DicePreference.prefIntervalForWatchClipboard) { current, _ ->
        if (!isFocused) {
            searchTextBox.text = current
        }
    }

    private val alwaysOnTopAction = SimpleAction(bundle.getString("toolbar.always_on_top"),
            null,
            { _, action ->
                this.isAlwaysOnTop = action.isSelected ?: false
            },
            bundle.getString("toolbar.always_on_top.mnemonic").first().toInt(),
            "control T",
            this.isAlwaysOnTopSupported,
            false)

    private val watchClipboardAction = SimpleAction(bundle.getString("toolbar.watch_clip_board"),
            null,
            { _, action ->
                DicePreference.prefWatchClipboard = action.isSelected ?: false
            },
            bundle.getString("toolbar.watch_clip_board.mnemonic").first().toInt(),
            "control W",
            true,
            DicePreference.prefWatchClipboard)

    private val preferenceAction = SimpleAction(bundle.getString("toolbar.preference"),
            null,
            { _, _ -> JaDicePreferencePane(diceWorker).showDialog(this) },
            bundle.getString("toolbar.preference.mnemonic").first().toInt(),
            "control S")

    private val resultViewCopyAction = SimpleAction(bundle.getString("result.popup.copy"),
            null,
            { _, _ -> resultView.copy() },
            bundle.getString("result.popup.copy.mnemonic").first().toInt(),
            "control C")

    private val resultViewSearchAction = SimpleAction(bundle.getString("result.popup.search"),
            null,
            { _, _ -> searchTextBox.text = resultView.selectedText.trim() },
            bundle.getString("result.popup.search.mnemonic").first().toInt())


    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = bundle.getString("jadice")
        iconImage = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("jadice.png"))

        createUIComponents()
        pack()
        searchTextBox.requestFocusInWindow()
    }

    private fun createUIComponents() {
        val rootPane = JPanel(BorderLayout())

        rootPane.layout = BorderLayout(0, 0)
        val topBar = JPanel()
        topBar.layout = GridBagLayout()
        rootPane.add(topBar, BorderLayout.NORTH)
        val gbc = GridBagConstraints()
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.gridwidth = GridBagConstraints.REMAINDER
        topBar.add(toolBar, gbc)
        topBar.add(searchTextBox, gbc)
        val scrollPane1 = JScrollPane()
        rootPane.add(scrollPane1, BorderLayout.CENTER)
        resultView.contentType = "text/html"
        resultView.isEditable = false
        resultView.text = bundle.getString("result.welcome")
        resultView.addCaretListener {
            val isSelected = resultView.selectedText?.isNotBlank() ?: false
            resultViewCopyAction.isEnabled = isSelected
            resultViewSearchAction.isEnabled = isSelected
        }
        scrollPane1.setViewportView(resultView)

        val resultViewPopupMenu = JPopupMenu()
        resultView.componentPopupMenu = resultViewPopupMenu
        val resultViewCopyMenuItem = JMenuItem(resultViewCopyAction)
        resultViewPopupMenu.add(resultViewCopyMenuItem)
        val resultViewSearchMenuItem = JMenuItem(resultViewSearchAction)
        resultViewPopupMenu.add(resultViewSearchMenuItem)

        toolBar.isFloatable = false
        toolBar.isRollover = true
        val alwaysOnTopMenuItem = JToggleButton(alwaysOnTopAction)
        toolBar.add(alwaysOnTopMenuItem)
        val watchClipboardMenuItem = JToggleButton(watchClipboardAction)
        toolBar.add(watchClipboardMenuItem)
        val preferenceMenuItem = JButton(preferenceAction)
        toolBar.add(preferenceMenuItem)

        val timedTextChangeAdapter = TimedTextChangeAdapter(
                DicePreference.prefSearchForDelay,
                ChangeListener {
                    val text = if (DicePreference.prefNormalizeSearch) {
                        DiceFactory.convert(searchTextBox.text)
                    } else {
                        searchTextBox.text
                    }

                    diceWorker.search(text).thenApply {
                        renderer.convertDiceResultDataToHtml(it)
                    }.thenAcceptAsync(Consumer {
                        resultView.text = it
                        resultView.select(0, 0)
                    }, eventQueueExecutor)
                })
        searchTextBox.document.addDocumentListener(timedTextChangeAdapter)
        resultView.addHyperlinkListener { event ->
            if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                val sourceElement = event.sourceElement.parentElement
                val (command, argument) = parseCommandLink(event.url)

                when (command) {
                    "more" -> argument?.toIntOrNull(10)?.also { dic ->
                        diceWorker.moreResults(dic).thenApply {
                            renderer.convertDiceResultDataToPartialHtml(it)
                        }.thenAcceptAsync(Consumer {
                            val offset = sourceElement.startOffset
                            (sourceElement.document as HTMLDocument).setOuterHTML(sourceElement, it)
                            resultView.select(offset, offset)
                        }, eventQueueExecutor)
                    }
                    "search" -> argument?.also { keyword ->
                        searchTextBox.text = keyword
                    }
                    null -> if (argument == null) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(event.url.toURI())
                        }
                    }
                }
            }
        }

        DicePreference.addPreferenceChangeListener(PreferenceChangeListener {
            when (it.key) {
                DicePreference.PREF_DELAY_FOR_SEARCH -> {
                    timedTextChangeAdapter.delay = DicePreference.prefSearchForDelay
                }
                DicePreference.PREF_WATCH_CLIPBOARD -> {
                    val prefWatchClipboard = DicePreference.prefWatchClipboard
                    watchClipboardAction.isSelected = prefWatchClipboard
                    if (prefWatchClipboard) {
                        clipboardWatcher.start()
                    } else {
                        clipboardWatcher.stop()
                    }
                }
                DicePreference.PREF_INTERVAL_FOR_WATCHING_CLIPBOARD -> {
                    clipboardWatcher.interval = DicePreference.prefIntervalForWatchClipboard
                }
            }
        })
        if (DicePreference.prefWatchClipboard) {
            clipboardWatcher.start()
        }

        contentPane = rootPane
    }

    private fun generateCommandLink(command: String, arguments: String): String {
        return "https://0.0.0.0/${URLEncoder.encode(command, "UTF-8")}/${URLEncoder.encode(arguments, "UTF-8")}"
    }

    private fun parseCommandLink(url: URL): Pair<String?, String?> {
        return if ((url.protocol == "https") && (url.host == "0.0.0.0") && (url.path.startsWith("/"))) {
            val array = url.file.substring(1).split("/")
            Pair(URLDecoder.decode(array.getOrNull(0), "UTF-8"),
                    URLDecoder.decode(array.getOrNull(1), "UTF-8"))
        } else {
            Pair(null, null)
        }
    }

    companion object {
        private val bundle = ResourceBundle.getBundle("jadice")
    }
}