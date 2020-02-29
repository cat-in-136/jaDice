package io.github.cat_in_136.jadice

import io.github.cat_in_136.misc.AWTEventQueueTaskExecutor
import io.github.cat_in_136.misc.SimpleAction
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
    private val resultView = JEditorPane()
    private val renderer = DiceResultHTMLRenderer(this::generateCommandLink)

    private val eventQueueExecutor = AWTEventQueueTaskExecutor()
    private var clipboardTimer: javax.swing.Timer? = null

    private val alwaysOnTopAction = SimpleAction(bundle.getString("menu.always_on_top"),
            null,
            { _, action ->
                this.isAlwaysOnTop = action.isSelected ?: false
            },
            bundle.getString("menu.always_on_top.mnemonic").first().toInt(),
            "control T",
            this.isAlwaysOnTopSupported,
            false)

    private val watchClipboardAction = SimpleAction(bundle.getString("menu.watch_clip_board"),
            null,
            { _, action ->
                DicePreference.prefWatchClipboard = action.isSelected ?: false
            },
            bundle.getString("menu.watch_clip_board.mnemonic").first().toInt(),
            "control W",
            true,
            DicePreference.prefWatchClipboard)

    private val preferenceAction = SimpleAction(bundle.getString("menu.preference"),
            null,
            { _, _ -> JaDicePreferencePane(diceWorker).showDialog(this) },
            bundle.getString("menu.preference.mnemonic").first().toInt(),
            "control S")

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = bundle.getString("jadice")

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
        val scrollPane1 = JScrollPane()
        rootPane.add(scrollPane1, BorderLayout.CENTER)
        resultView.contentType = "text/html"
        resultView.isEditable = false
        resultView.text = bundle.getString("result.welcome")
        scrollPane1.setViewportView(resultView)

        val menuBar = JMenuBar()
        this.jMenuBar = menuBar
        val viewMenu = JMenu()
        viewMenu.text = bundle.getString("menu.view")
        viewMenu.mnemonic = bundle.getString("menu.view.mnemonic").first().toInt()
        menuBar.add(viewMenu)
        val alwaysOnTopMenuItem = JCheckBoxMenuItem(alwaysOnTopAction)
        viewMenu.add(alwaysOnTopMenuItem)
        val optionMenu = JMenu()
        optionMenu.text = bundle.getString("menu.option")
        optionMenu.mnemonic = bundle.getString("menu.option.mnemonic").first().toInt()
        menuBar.add(optionMenu)
        val watchClipboardMenuItem = JCheckBoxMenuItem(watchClipboardAction)
        optionMenu.add(watchClipboardMenuItem)
        val preferenceMenuItem = JMenuItem(preferenceAction)
        optionMenu.add(preferenceMenuItem)

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
                    stopClipboardWatcher()
                    if (prefWatchClipboard) {
                        startClipboardWatcher()
                    }
                }
                DicePreference.PREF_INTERVAL_FOR_WATCHING_CLIPBOARD -> {
                    stopClipboardWatcher()
                    if (DicePreference.prefWatchClipboard) {
                        startClipboardWatcher()
                    }
                }
            }
        })
        if (DicePreference.prefWatchClipboard) {
            startClipboardWatcher()
        }

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
        val clipboardTimer = javax.swing.Timer(DicePreference.prefIntervalForWatchClipboard, null)
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