package io.github.cat_in_136.misc

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class TransferableObject<T>(private val transferableObject: T, private val transferDataFlavor: DataFlavor) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(transferDataFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        return transferDataFlavor == flavor
    }

    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        return if (isDataFlavorSupported(flavor)) {
            transferableObject as Any
        } else {
            throw UnsupportedFlavorException(flavor)
        }
    }
}