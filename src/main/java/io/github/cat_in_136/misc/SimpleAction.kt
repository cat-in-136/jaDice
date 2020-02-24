package io.github.cat_in_136.misc

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon
import javax.swing.KeyStroke

class SimpleAction : AbstractAction {
    private var actionHandler: (ActionEvent, SimpleAction) -> Unit

    var name: String
        get() = getValue(Action.NAME) as String
        set(value) = putValue(Action.NAME, value)

    var mnemonicKey: Int?
        get() = getValue(Action.MNEMONIC_KEY) as Int?
        set(value) = putValue(Action.MNEMONIC_KEY, value)

    var acceleratorKey: KeyStroke?
        get() = getValue(Action.ACCELERATOR_KEY) as KeyStroke?
        set(value) = putValue(Action.ACCELERATOR_KEY, value)

    var isSelected: Boolean?
        get() = getValue(Action.SELECTED_KEY) as Boolean?
        set(value) = putValue(Action.SELECTED_KEY, value)

    constructor(name: String?, icon: Icon?,
                onAction: (ActionEvent, SimpleAction) -> Unit,
                mnemonicKey: Int? = null,
                acceleratorKey: String? = null,
                isEnabled: Boolean = true,
                isSelected: Boolean? = null) : super(name, icon) {
        this.actionHandler = onAction
        this.mnemonicKey = mnemonicKey
        if (!acceleratorKey.isNullOrEmpty()) {
            this.acceleratorKey = KeyStroke.getKeyStroke(acceleratorKey)
        }
        this.isEnabled = isEnabled
        this.isSelected = isSelected
    }

    override fun actionPerformed(event: ActionEvent) {
        actionHandler(event, this)
    }
}