package io.github.cat_in_136.jadice

import jp.sblo.pandora.dice.IdicInfo
import java.awt.*
import java.io.File
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
        private val normalizeSearchCheckBox = JCheckBox()
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
            normalizeSearchCheckBox.text = "Normalize search word"
            normalizeSearchCheckBox.isSelected = DicePreferenceService.prefNormalizeSearch
            rootPane.add(normalizeSearchCheckBox, gbc)
        }

        fun getRootComponent(): JComponent = rootPane

        fun applyToPreference() {
            DicePreferenceService.prefSearchForDelay = delayForSearchTextField.value as Int
            DicePreferenceService.prefNormalizeSearch = normalizeSearchCheckBox.isSelected
        }
    }

    private class DicPref(val diceWorker: DiceWorker) {
        private val rootPane = JPanel()

        private val dicListModel = DefaultListModel<String>()
        private val dicListView = JList<String>()
        private val addButton = JButton()
        private val delButton = JButton()
        private val upButton = JButton()
        private val downButton = JButton()

        init {
            createUIComponents()
        }

        private fun createUIComponents() {
            rootPane.layout = BorderLayout()

            val scrollPane1 = JScrollPane()
            rootPane.add(scrollPane1, BorderLayout.CENTER)
            dicListView.selectionMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION
            dicListView.model = dicListModel
            dicListView.cellRenderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(list: JList<*>,
                                                          value: Any,
                                                          index: Int,
                                                          isSelected: Boolean,
                                                          cellHasFocus: Boolean): Component {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    this.text = value.toString().split(File.separatorChar).last()
                    this.toolTipText = value.toString()
                    return this
                }
            }
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
            upButton.text = "Up"
            upButton.isEnabled = false
            btnPanel.add(upButton, gbc)
            downButton.text = "Down"
            downButton.isEnabled = false
            btnPanel.add(downButton, gbc)

            for (dic in DicePreferenceService.prefDics) {
                dicListModel.addElement(dic)
            }
            dicListView.addListSelectionListener {
                val selectedIndex = dicListView.selectedIndex
                delButton.isEnabled = selectedIndex >= 0
                upButton.isEnabled = selectedIndex >= 1
                downButton.isEnabled = (selectedIndex >= 0) && (selectedIndex < dicListView.model.size - 1)
            }
            addButton.addActionListener {
                val fileChooser = JFileChooser()
                fileChooser.isMultiSelectionEnabled = true
                fileChooser.fileFilter = FileNameExtensionFilter("PDIC File (*.dic)", "dic")
                fileChooser.isAcceptAllFileFilterUsed = true

                val selected = fileChooser.showOpenDialog(rootPane)
                if (selected == JFileChooser.APPROVE_OPTION) {
                    val files = fileChooser.selectedFiles.map { it.absolutePath }
                    addPDICFiles(files)
                }
            }
            delButton.addActionListener {
                val selectedIndex = dicListView.selectedIndex
                if (selectedIndex >= 0) {
                    dicListModel.removeElementAt(selectedIndex)
                }
            }
            upButton.addActionListener {
                val selectedIndex = dicListView.selectedIndex
                if (selectedIndex >= 1) {
                    val value = dicListModel.remove(selectedIndex)
                    dicListModel.add(selectedIndex - 1, value)
                    dicListView.selectedIndex = selectedIndex - 1
                }
            }
            downButton.addActionListener {
                val selectedIndex = dicListView.selectedIndex
                if ((selectedIndex >= 0) && (selectedIndex < dicListView.model.size - 1)) {
                    val value = dicListModel.remove(selectedIndex)
                    dicListModel.add(selectedIndex + 1, value)
                    dicListView.selectedIndex = selectedIndex + 1
                }
            }
        }

        fun getRootComponent(): JComponent = rootPane

        fun addPDICFiles(files: List<String>) {
            if (files.isEmpty()) {
                return  // do nothing if empty
            }

            val alreadyAddedFiles = files.filter(dicListModel::contains)
            if (alreadyAddedFiles.isNotEmpty()) {
                JOptionPane.showMessageDialog(rootPane,
                        "${alreadyAddedFiles.joinToString(",")} is already added")
                return
            }

            val tryOpenFutures = files.map {
                this.tryToOpenPDICFile(it)
            }.toTypedArray()
            CompletableFuture.allOf(*tryOpenFutures).whenComplete { _, e ->
                val failedFiles = tryOpenFutures.mapIndexed { i, future ->
                    Pair(files[i], future.isCompletedExceptionally)
                }.filter { it.second }.map { it.first }

                SwingUtilities.invokeAndWait {
                    if (failedFiles.isEmpty()) {
                        files.forEach(dicListModel::addElement)
                    } else {
                        JOptionPane.showMessageDialog(rootPane,
                                "Failed to open ${failedFiles.joinToString(",")}")
                    }
                }
            }
        }

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