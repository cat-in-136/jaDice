package io.github.cat_in_136.jadice

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.TransferHandler

class ListReorderTransferHandler(var list: JList<String>) : TransferHandler() {
    val listItemFlavor = DataFlavor(list.javaClass, "item of list")
    private var srcIndex = -1
    private var dstIndex = -1

    override fun canImport(support: TransferSupport): Boolean {
        return support.isDrop && support.isDataFlavorSupported(listItemFlavor)
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
        dstIndex = dl.index

        val data = runCatching { support.transferable.getTransferData(listItemFlavor) as String }
                .getOrElse { return false }
        listModel.add(dstIndex, data)
        return true
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