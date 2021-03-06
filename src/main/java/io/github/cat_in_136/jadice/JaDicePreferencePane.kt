package io.github.cat_in_136.jadice

import jp.sblo.pandora.dice.IdicInfo
import java.awt.*
import java.io.File
import java.util.*
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
                bundle.getString("toolbar.preference"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE)
        if (ret == JOptionPane.OK_OPTION) {
            applyToPreference()
        }
    }

    private fun createUIComponents() {
        this.add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab(bundle.getString("preference.search"), searchPref.getRootComponent())
        tabbedPane.setMnemonicAt(0, bundle.getString("preference.search.mnemonic").first().toInt())
        tabbedPane.addTab(bundle.getString("preference.dictionaries"), dictionaryPref.getRootComponent())
        tabbedPane.setMnemonicAt(1, bundle.getString("preference.dictionaries.mnemonic").first().toInt())
    }

    private fun applyToPreference() {
        searchPref.applyToPreference()
        dictionaryPref.applyToPreference()

        DicePreference.flush()
    }

    private class SearchPref {
        private val delayForSearchTextField = JFormattedTextField()
        private val intervalForWatchClipboardTextField = JFormattedTextField()
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
            delayForSearchLabel.text = bundle.getString("preference.search.delayForTime")
            delayForSearchLabel.displayedMnemonic = bundle.getString("preference.search.delayForTime.mnemonic").first().toInt()
            delayForSearchPanel.add(delayForSearchLabel)
            delayForSearchTextField.value = DicePreference.prefSearchForDelay
            delayForSearchTextField.horizontalAlignment = JTextField.TRAILING
            delayForSearchTextField.columns = 4
            delayForSearchLabel.labelFor = delayForSearchTextField
            delayForSearchPanel.add(delayForSearchTextField)
            delayForSearchPanel.add(JLabel(bundle.getString("preference.search.timerMilliSec")))
            val intervalForWatchClipboardPanel = JPanel()
            intervalForWatchClipboardPanel.layout = FlowLayout(FlowLayout.CENTER, 5, 5)
            rootPane.add(intervalForWatchClipboardPanel, gbc)
            val intervalForWatchClipboardLabel = JLabel()
            intervalForWatchClipboardLabel.text = bundle.getString("preference.search.intervalForWatchClipboard")
            intervalForWatchClipboardLabel.displayedMnemonic = bundle.getString("preference.search.intervalForWatchClipboard.mnemonic").first().toInt()
            intervalForWatchClipboardPanel.add(intervalForWatchClipboardLabel)
            intervalForWatchClipboardTextField.value = DicePreference.prefIntervalForWatchClipboard
            intervalForWatchClipboardTextField.horizontalAlignment = JTextField.TRAILING
            intervalForWatchClipboardTextField.columns = 4
            intervalForWatchClipboardLabel.labelFor = intervalForWatchClipboardTextField
            intervalForWatchClipboardPanel.add(intervalForWatchClipboardTextField)
            intervalForWatchClipboardPanel.add(JLabel(bundle.getString("preference.search.timerMilliSec")))
            normalizeSearchCheckBox.text = bundle.getString("preference.search.normalizeSearch")
            normalizeSearchCheckBox.mnemonic = bundle.getString("preference.search.normalizeSearch.mnemonic").first().toInt()
            normalizeSearchCheckBox.isSelected = DicePreference.prefNormalizeSearch
            rootPane.add(normalizeSearchCheckBox, gbc)
        }

        fun getRootComponent(): JComponent = rootPane

        fun applyToPreference() {
            DicePreference.prefSearchForDelay = delayForSearchTextField.value as Int
            DicePreference.prefIntervalForWatchClipboard = intervalForWatchClipboardTextField.value as Int
            DicePreference.prefNormalizeSearch = normalizeSearchCheckBox.isSelected
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
            dicListView.selectionMode = ListSelectionModel.SINGLE_SELECTION
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
            dicListView.dragEnabled = true
            dicListView.dropMode=DropMode.INSERT
            val dicListTransHandler = DicListTransferHandler(dicListView)
            dicListTransHandler.fileAddHandler = { list, dl ->
                addPDICFiles(list, dl)
                true
            }
            dicListView.transferHandler = dicListTransHandler
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
            addButton.text = bundle.getString("preference.dictionaries.add")
            addButton.mnemonic = bundle.getString("preference.dictionaries.add.mnemonic").first().toInt()
            btnPanel.add(addButton, gbc)
            delButton.text = bundle.getString("preference.dictionaries.delete")
            delButton.mnemonic = bundle.getString("preference.dictionaries.delete.mnemonic").first().toInt()
            delButton.isEnabled = false
            btnPanel.add(delButton, gbc)
            upButton.text = bundle.getString("preference.dictionaries.up")
            upButton.mnemonic = bundle.getString("preference.dictionaries.up.mnemonic").first().toInt()
            upButton.isEnabled = false
            btnPanel.add(upButton, gbc)
            downButton.text = bundle.getString("preference.dictionaries.down")
            downButton.mnemonic = bundle.getString("preference.dictionaries.down.mnemonic").first().toInt()
            downButton.isEnabled = false
            btnPanel.add(downButton, gbc)

            for (dic in DicePreference.prefDics) {
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
                fileChooser.fileFilter = FileNameExtensionFilter(bundle.getString("preference.file_chooser.filter.pdic"), "dic")
                fileChooser.isAcceptAllFileFilterUsed = true
                fileChooser.dialogTitle = addButton.text

                val selected = fileChooser.showOpenDialog(rootPane)
                if (selected == JFileChooser.APPROVE_OPTION) {
                    addPDICFiles(fileChooser.selectedFiles.asList(), null)
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

        fun addPDICFiles(files: List<File>, dl: JList.DropLocation?) {
            if (files.isEmpty()) {
                return  // do nothing if empty
            }

            val filenames = files.map { it.absolutePath }
            val alreadyAddedFiles = filenames.filter(dicListModel::contains)
            if (alreadyAddedFiles.isNotEmpty()) {
                JOptionPane.showMessageDialog(rootPane,
                        String.format(bundle.getString("preference.dictionaries.error.already"),
                                alreadyAddedFiles.joinToString(",")))
                return
            }

            val tryOpenFutures = filenames.map {
                this.tryToOpenPDICFile(it)
            }.toTypedArray()
            CompletableFuture.allOf(*tryOpenFutures).whenComplete { _, _ ->
                val failedFiles = tryOpenFutures.mapIndexed { i, future ->
                    Pair(filenames[i], future.isCompletedExceptionally)
                }.filter { it.second }.map { it.first }

                if (failedFiles.isEmpty()) {
                    val index = dl?.index ?: dicListModel.size()
                    SwingUtilities.invokeAndWait {
                        for ((i, filename) in filenames.withIndex()) {
                            dicListModel.insertElementAt(filename, index + i)
                        }
                    }
                } else {
                    SwingUtilities.invokeAndWait {
                        JOptionPane.showMessageDialog(rootPane,
                                String.format(bundle.getString("preference.dictionaries.error.io"),
                                        failedFiles.joinToString(",")))
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
            DicePreference.prefDics = prefDics

            diceWorker.removeAllDictionaries().whenCompleteAsync { _, e ->
                e?.run { throw e }
                for (dic in prefDics) {
                    diceWorker.addDictionary(dic).join()
                }
            }
        }
    }

    companion object {
        private val bundle = ResourceBundle.getBundle("jadice")
    }
}
