package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.AWTEventQueueTaskExecutor
import io.github.cat_in_136.misc.TimedTextChangeAdapter
import jp.sblo.pandora.dice.DiceFactory
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
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

    private val searchTextBox = JTextField()
    private val hamburgerButton = JButton()
    private val resultView = JEditorPane()
    private val renderer = DiceResultHTMLRenderer(this::generateCommandLink)

    private val eventQueueExecutor = AWTEventQueueTaskExecutor()
    private var clipboardTimer: javax.swing.Timer? = null

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
        resultView.text = bundle.getString("result.welcome")
        scrollPane1.setViewportView(resultView)

        val popupMenu = JPopupMenu()
        val preferenceMenuItem = JMenuItem()
        preferenceMenuItem.text = bundle.getString("preference")
        preferenceMenuItem.addActionListener {
            JaDicePreferencePane(diceWorker).showDialog(this)
        }
        popupMenu.add(preferenceMenuItem)
        val alwaysOnTopMenuItem = JCheckBoxMenuItem()
        alwaysOnTopMenuItem.text = bundle.getString("always_on_top")
        alwaysOnTopMenuItem.isEnabled = this.isAlwaysOnTopSupported
        alwaysOnTopMenuItem.isSelected = this.isAlwaysOnTop
        alwaysOnTopMenuItem.addActionListener {
            this.isAlwaysOnTop = alwaysOnTopMenuItem.isSelected
        }
        popupMenu.add(alwaysOnTopMenuItem)
        val watchClipboardMenuItem = JCheckBoxMenuItem()
        watchClipboardMenuItem.text = bundle.getString("watch_clip_board")
        watchClipboardMenuItem.addActionListener {
            stopClipboardWatcher()
            if (watchClipboardMenuItem.isSelected) {
                startClipboardWatcher()
            }
        }
        popupMenu.add(watchClipboardMenuItem)

        val timedTextChangeAdapter = TimedTextChangeAdapter(
                DicePreferenceService.prefSearchForDelay,
                ChangeListener {
                    val text = if (DicePreferenceService.prefNormalizeSearch) {
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

    private fun generateCommandLink(command: String, arguments: String): String {
        return "https://0.0.0.0/" +
                URLEncoder.encode(command, "UTF-8") +
                "/" +
                URLEncoder.encode(arguments, "UTF-8")
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

    private fun startClipboardWatcher() {
        val clipboardTimer = javax.swing.Timer(1000, null) // TODO let clipboard interval configurable
        clipboardTimer.addActionListener {
            if (!this.isFocused) {
                try {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    searchTextBox.text = clipboard.getData(DataFlavor.stringFlavor) as String
                } catch (e: Exception) {
                    // ignore all exception
                }
            }
        }
        clipboardTimer.start()
        this.clipboardTimer = clipboardTimer
    }

    private fun stopClipboardWatcher() {
        if (this.clipboardTimer != null) {
            this.clipboardTimer?.stop()
            this.clipboardTimer = null
        }
    }

    companion object {
        private val bundle = ResourceBundle.getBundle("jadice")
    }
}