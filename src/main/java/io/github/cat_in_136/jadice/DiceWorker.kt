package io.github.cat_in_136.jadice

import jp.sblo.pandora.dice.DiceFactory
import jp.sblo.pandora.dice.IIndexCacheFile
import jp.sblo.pandora.dice.IdicResult
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Supplier


class DiceWorker {
    private val dice = DiceFactory.getInstance()

    private var latestSubmittedFuture: CompletableFuture<List<DiceResultData>> = CompletableFuture.completedFuture(Collections.emptyList())
    private val pool = Executors.newSingleThreadExecutor()

    init {
        dice.open("PDWD1913U.dic")?.let { dicInfo ->
            val idxFile = File.createTempFile("JaDice", ".idx")
            idxFile.deleteOnExit()

            if (!dicInfo.readIndexBlock(object : IIndexCacheFile {
                        override fun getInput(): FileInputStream = FileInputStream(idxFile)
                        override fun getOutput(): FileOutputStream = FileOutputStream(idxFile)
                    })) {
                dice.close(dicInfo)
            } else {
                // TODO SettingsActivity.apllySettings(this, dicInfo)
            }
        }
    }

    private fun cancelSearchTasks() {
        synchronized(this) {
            if (!latestSubmittedFuture.isCancelled &&
                    !latestSubmittedFuture.isDone) {
                latestSubmittedFuture.cancel(true)
            }
        }
    }

    fun search(keyword: String): CompletableFuture<List<DiceResultData>> {
        synchronized(this) {
            cancelSearchTasks()

            latestSubmittedFuture = CompletableFuture.supplyAsync(Supplier {
                searchSync(keyword)
            }, pool)
            return latestSubmittedFuture
        }
    }

    private fun searchSync(keyword: String): List<DiceResultData> {
        val result = ArrayList<DiceResultData>()

        synchronized(dice) {
            for (i in 0 until dice.dicNum) {
                throwInterruptedExceptionIfInterrupted()
                if (!dice.isEnable(i)) {
                    continue
                }

                dice.search(i, keyword)
                val pr = dice.getResult(i)

                throwInterruptedExceptionIfInterrupted()
                if (pr.count > 0) {
                    generateResultDisp(i, pr, result)
                    generateFooterDisp(i, result)
                }

                throwInterruptedExceptionIfInterrupted()
            }
        }

        if (result.size == 0) {
            generateNoneDisp(result)
        }

        throwInterruptedExceptionIfInterrupted()
        return result
    }

    private fun generateResultDisp(dic: Int, pr: IdicResult, result: ArrayList<DiceResultData>, pos: Int = -1): Int {
        val info = dice.getDicInfo(dic)
        var posWork = pos

        for (i in 0 until pr.count) {
            val idx = pr.getDisp(i)
            val index = if (idx.isNullOrEmpty()) {
                pr.getIndex(i)
            } else {
                idx
            }
            val phone = pr.getPhone(i)
            val trans = pr.getTrans(i)
            val sample = pr.getSample(i)

            val indexSize = info.GetIndexSize()
            val phoneSize = info.GetPhoneticSize()
            val transSize = info.GetTransSize()
            val sampleSize = info.GetSampleSize()

            val data = DiceResultData(DiceResultData.DiceResultDataMode.WORD, dic, index, phone, trans, sample, indexSize, phoneSize, transSize, sampleSize)
            posWork = addDiceResultDataTo(posWork, result, data)
        }

        if (dice.hasMoreResult(dic)) {
            val index = "More" // TODO
            val data = DiceResultData(DiceResultData.DiceResultDataMode.MORE, dic, index)
            posWork = addDiceResultDataTo(posWork, result, data)
        }

        return posWork
    }

    private fun generateFooterDisp(dic: Int, result: ArrayList<DiceResultData>, pos: Int = -1): Int {
        val info = dice.getDicInfo(dic)

        val dicName = if (info.GetDicName().isNullOrEmpty()) {
            info.GetFilename()
        } else {
            info.GetDicName()
        }

        val index = dicName
        val data = DiceResultData(DiceResultData.DiceResultDataMode.FOOTER, dic, index)
        return addDiceResultDataTo(pos, result, data)
    }

    private fun generateNoneDisp(result: ArrayList<DiceResultData>, pos: Int = -1): Int {
        val data = DiceResultData(DiceResultData.DiceResultDataMode.NONE, 0)
        return addDiceResultDataTo(pos, result, data)
    }

    private fun addDiceResultDataTo(pos: Int, result: ArrayList<DiceResultData>, data: DiceResultData): Int =
            if (pos == -1) {
                result.add(data)
                pos
            } else {
                result.add(pos, data)
                pos + 1
            }

    @Throws(InterruptedException::class)
    private fun throwInterruptedExceptionIfInterrupted() {
        if (Thread.currentThread().isInterrupted) {
            throw InterruptedException()
        }
    }
}