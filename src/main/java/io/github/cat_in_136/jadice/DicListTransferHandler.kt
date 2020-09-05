package io.github.cat_in_136.jadice

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File
import java.io.IOException
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.TransferHandler

class DicListTransferHandler(var list: JList<String>) : TransferHandler() {
    val listItemFlavor = DataFlavor(list.javaClass, "item of list")
    private var srcIndex = -1
    private var dstIndex = -1

    var fileAddHandler: ((List<File>, JList.DropLocation) -> Boolean)? = null

    override fun canImport(support: TransferSupport): Boolean {
        return support.isDrop && (support.isDataFlavorSupported(listItemFlavor) ||
                (fileAddHandler != null && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)))
    }

    override fun createTransferable(c: JComponent): Transferable {
        srcIndex = list.selectedIndex
        val transferredObjects = list.selectedValue
        return object : Transferable {
            override fun getTransferDataFlavors(): Array<DataFlavor> {
                return arrayOf(listItemFlavor)
            }

            override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
                return listItemFlavor == flavor
            }

            @Throws(UnsupportedFlavorException::class, IOException::class)
            override fun getTransferData(flavor: DataFlavor): Any {
                return if (isDataFlavorSupported(flavor)) {
                    transferredObjects
                } else {
                    throw UnsupportedFlavorException(flavor)
                }
            }
        }
    }

    override fun getSourceActions(c: JComponent): Int {
        return MOVE
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!support.isDrop) {
            return false
        }

        val listModel = list.model as DefaultListModel<String>
        val dl = support.dropLocation as JList.DropLocation

        if (support.isDataFlavorSupported(listItemFlavor)) {
            val data = runCatching { support.transferable.getTransferData(listItemFlavor) as String }
                    .getOrElse { return false }
            dstIndex = dl.index
            listModel.add(dstIndex, data)
            return true
        } else if (fileAddHandler != null && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            @Suppress("UNCHECKED_CAST")
            val data = runCatching { support.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File> }
                    .getOrElse { return false }
            return fileAddHandler!!(data, dl)
        } else {
            return false
        }
    }

    override fun exportDone(c: JComponent, data: Transferable, action: Int) {
        cleanup(action == MOVE)
    }

    private fun cleanup(remove: Boolean) {
        if (remove && srcIndex != -1) {
            val model = list.model as DefaultListModel<String>
            if (dstIndex in 0..srcIndex) {
                srcIndex++
            }
            model.removeElementAt(srcIndex)
        }
        srcIndex = -1
        dstIndex = -1
    }
}