package io.github.cat_in_136.misc

import java.awt.EventQueue
import java.util.concurrent.Executor

class AWTEventQueueTaskExecutor : Executor {
    override fun execute(r: Runnable) {
        EventQueue.invokeLater(r)
    }
}