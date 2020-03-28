package io.github.cat_in_136.misc

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import javax.swing.Timer

class ClipboardWatcher(interval: Int, private val onChange: (String, String?) -> Unit) {
    private var text: String? = null

    private val timer = Timer(interval) {
        onTimerExpired()
    }

    var interval
        get() = timer.delay
        set(value) {
            val wasRunning = timer.isRunning
            timer.stop()
            timer.initialDelay = value
            timer.delay = value
            if (wasRunning) {
                timer.start()
            }
        }

    fun start() {
        timer.stop()
        timer.start()
    }

    fun stop() {
        timer.stop()
    }

    private fun onTimerExpired() {
        val oldText = this.text

        val text = kotlin.runCatching {
            Toolkit.getDefaultToolkit().systemClipboard
        }.mapCatching { clipboard ->
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                clipboard.getData(DataFlavor.stringFlavor) as String
            } else {
                return
            }
        }.getOrElse {
            //it.printStackTrace()
            return
        }

        this.text = text
        if (text != oldText) {
            onChange(text, oldText)
        }
    }
}