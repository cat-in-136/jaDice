package io.github.cat_in_136.misc

import java.awt.event.ActionListener
import javax.swing.Timer
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document


class TimedTextChangeAdapter(delay: Int, listener: ChangeListener) : DocumentListener {

    private lateinit var source: Document

    private val timer = Timer(delay, ActionListener {
        listener.stateChanged(ChangeEvent(source))
    }).apply {
        isRepeats = false
    }

    override fun changedUpdate(e: DocumentEvent) {
        timer.stop()
        source = e.document
        timer.start()
    }

    override fun insertUpdate(e: DocumentEvent) {
        changedUpdate(e)
    }

    override fun removeUpdate(e: DocumentEvent) {
        changedUpdate(e)
    }
}