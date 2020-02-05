package io.github.cat_in_136.jadice

import java.awt.*
import javax.swing.*


class JaDicePreferencePane : JPanel(BorderLayout()) {
    private val tabbedPane = JTabbedPane()

    private val searchPref = SearchPref()
    private val dictionaryPref = DicPref()

    init {
        createUIComponents()
    }

    fun showDialog(parent: Component) {
        val ret = JOptionPane.showConfirmDialog(parent,
                this,
                null,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE)
        if (ret == JOptionPane.OK_OPTION) {
            applyToPreference()
        }
    }

    private fun createUIComponents() {
        this.add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab("Search", searchPref.getRootComponent())
        tabbedPane.addTab("Dictionaries", dictionaryPref.getRootComponent())
    }

    private fun applyToPreference() {
        searchPref.applyToPreference()
    }

    private class SearchPref {
        private val delayForSearchTextField = JFormattedTextField()
        private val searchDelayCheckBox = JCheckBox()
        private val rootPane = JPanel()

        init {
            createUIComponents()
        }

        private fun createUIComponents() {
            rootPane.layout = GridBagLayout()
            val delayForSearchPanel = JPanel()
            delayForSearchPanel.layout = FlowLayout(FlowLayout.CENTER, 5, 5)
            val gbc = GridBagConstraints()
            gbc.anchor = GridBagConstraints.LINE_START
            gbc.gridwidth = GridBagConstraints.REMAINDER
            rootPane.add(delayForSearchPanel, gbc)
            val delayForSearchLabel = JLabel()
            delayForSearchLabel.text = "Delay time for incremental search"
            delayForSearchPanel.add(delayForSearchLabel)
            delayForSearchTextField.value = DicePreferenceService.prefSearchForDelay
            delayForSearchTextField.horizontalAlignment = JTextField.TRAILING
            delayForSearchTextField.columns = 4
            delayForSearchLabel.labelFor = delayForSearchTextField
            delayForSearchPanel.add(delayForSearchTextField)
            searchDelayCheckBox.text = "Normalize search word"
            searchDelayCheckBox.isEnabled = false
            rootPane.add(searchDelayCheckBox, gbc)
        }

        fun getRootComponent(): JComponent = rootPane

        fun applyToPreference() {
            DicePreferenceService.prefSearchForDelay = delayForSearchTextField.value as Int
        }
    }

    private class DicPref {
        private val rootPane = JPanel()

        private val dicListView = JList<String>()
        private val addButton = JButton()
        private val delButton = JButton()

        init {
            createUIComponents()
        }

        private fun createUIComponents() {
            rootPane.layout = BorderLayout()

            val scrollPane1 = JScrollPane()
            rootPane.add(scrollPane1, BorderLayout.CENTER)
            dicListView.setListData(arrayOf("None"))
            dicListView.isEnabled = false
            scrollPane1.setViewportView(dicListView)
            rootPane.add(scrollPane1, BorderLayout.CENTER)
            val btnPanel = JPanel()
            btnPanel.layout = GridBagLayout()
            rootPane.add(btnPanel, BorderLayout.EAST)
            val gbc = GridBagConstraints()
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.gridwidth = GridBagConstraints.REMAINDER
            gbc.insets = Insets(2, 0, 2, 0)
            addButton.text = "Add"
            addButton.isEnabled = false
            btnPanel.add(addButton, gbc)
            delButton.text = "Delete"
            delButton.isEnabled = false
            btnPanel.add(delButton, gbc)
        }

        fun getRootComponent(): JComponent = rootPane
    }
}