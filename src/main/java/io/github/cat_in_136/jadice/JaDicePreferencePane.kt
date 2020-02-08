package io.github.cat_in_136.jadice

import jp.sblo.pandora.dice.IdicInfo
import java.awt.*
import java.util.concurrent.CompletableFuture
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter


class JaDicePreferencePane(diceWorker: DiceWorker) : JPanel(BorderLayout()) {
    private val tabbedPane = JTabbedPane()

    private val searchPref = SearchPref()
    private val dictionaryPref = DicPref(diceWorker)

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
        dictionaryPref.applyToPreference()

        DicePreferenceService.flush()
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

    private class DicPref(val diceWorker: DiceWorker) {
        private val rootPane = JPanel()

        private val dicListModel = DefaultListModel<String>()
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
            dicListView.model = dicListModel
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
            btnPanel.add(addButton, gbc)
            delButton.text = "Delete"
            delButton.isEnabled = false
            btnPanel.add(delButton, gbc)

            for (dic in DicePreferenceService.prefDics) {
                dicListModel.addElement(dic)
            }
            dicListView.addListSelectionListener {
                delButton.isEnabled = !dicListView.isSelectionEmpty
            }
            addButton.addActionListener {
                val fileChooser = JFileChooser()
                fileChooser.fileFilter = FileNameExtensionFilter("PDIC File (*.dic)", "dic")
                fileChooser.isAcceptAllFileFilterUsed = true

                val selected = fileChooser.showOpenDialog(rootPane)
                if (selected == JFileChooser.APPROVE_OPTION) {
                    val path = fileChooser.selectedFile.absolutePath
                    if (dicListModel.contains(path)) {
                        JOptionPane.showMessageDialog(rootPane, "$path is already added")
                    } else {
                        tryToOpenPDICFile(path).whenCompleteAsync { _, e ->
                            SwingUtilities.invokeAndWait {
                                if (e != null) {
                                    JOptionPane.showMessageDialog(rootPane, "failed to open $path")
                                } else {
                                    dicListModel.addElement(path)
                                }
                            }
                        }
                    }
                }
            }
            delButton.addActionListener {
                val selectedIndex = dicListView.selectedIndex
                if (selectedIndex >= 0) {
                    dicListModel.removeElementAt(selectedIndex)
                }
            }
        }

        fun getRootComponent(): JComponent = rootPane

        fun tryToOpenPDICFile(path: String): CompletableFuture<IdicInfo> {
            return diceWorker.getDictionaries().thenApplyAsync { dics ->
                dics.find { it.GetFilename() == path } ?: diceWorker.addDictionary(path).thenApply { dicInfo ->
                    diceWorker.removeDictionary(path)
                    dicInfo
                }.get()
            }
        }

        fun applyToPreference() {
            val prefDics = dicListModel.elements().toList()
            DicePreferenceService.prefDics = prefDics

            diceWorker.removeAllDictionaries().whenCompleteAsync { _, e ->
                e?.run { throw e }
                for (dic in prefDics) {
                    diceWorker.addDictionary(dic).join()
                }
            }
        }
    }
}