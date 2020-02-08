package io.github.cat_in_136.jadice

import java.awt.Dimension
import javax.swing.SwingUtilities
import kotlin.math.max

object JaDice {
    @JvmStatic
    fun main(args: Array<String>) {
        val dics = DicePreferenceService.prefDics
        val worker = DiceWorker(dics)

        SwingUtilities.invokeLater {
            val window = JaDiceWindow(worker)
            window.size = Dimension(max(window.size.width, 256), max(window.size.height, 256))
            window.isVisible = true
        }
    }
}