package io.github.cat_in_136.jadice

import java.awt.BorderLayout
import java.awt.Component
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
        private var delayForSearchTextField = JFormattedTextField()
        private var searchDelayCheckBox = JCheckBox()
        private val rootPane = JPanel()

        init {
            createUIComponents()
        }

        private fun createUIComponents() {
            val delayForSearchLabel = JLabel()
            delayForSearchLabel.text = "Delay time for incremental search"
            delayForSearchTextField.value = DicePreferenceService.prefSearchForDelay
            delayForSearchTextField.horizontalAlignment = JTextField.TRAILING
            delayForSearchTextField.columns = 4
            delayForSearchLabel.labelFor = delayForSearchTextField

            searchDelayCheckBox.text = "Normalize search word"
            searchDelayCheckBox.isEnabled = false

            val layout = GroupLayout(rootPane)
            layout.autoCreateGaps = true
            layout.autoCreateContainerGaps = true
            rootPane.layout = layout

            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(delayForSearchLabel))
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(delayForSearchTextField)))
                            .addComponent(searchDelayCheckBox)
                    )
            )

            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(delayForSearchLabel)
                                            .addComponent(delayForSearchTextField))
                            )
                    )
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(searchDelayCheckBox))
            )
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
            val panel1 = JPanel()
            panel1.layout = BoxLayout(panel1, BoxLayout.PAGE_AXIS)
            rootPane.add(panel1, BorderLayout.EAST)
            addButton.text = "Add"
            addButton.isEnabled = false
            panel1.add(addButton)
            delButton.text = "Delete"
            delButton.isEnabled = false
            panel1.add(delButton)
            panel1.add(Box.createVerticalGlue())
        }

        fun getRootComponent(): JComponent = rootPane
    }
}