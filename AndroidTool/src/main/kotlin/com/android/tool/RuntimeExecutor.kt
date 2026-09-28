package com.android.tool

import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class RuntimeExecutor {
    companion object {
        fun execute(cmdArray: Array<String>, callback: IExecuteCallback?) {
            ThreadUtils.threadIO().launch {
                try {
                    println("RuntimeExecutor.execute  ${cmdArray.contentToString()}")
                    Runtime.getRuntime().exec(cmdArray, null)?.also { process ->
                        if (process.isAlive) {
                            val stream = process.inputStream
                            val writer = BufferedReader(InputStreamReader(stream))
                            var hasResult = false
                            val readLines = writer.readLines()
                            val size = readLines.size
                            run loop@{
                                readLines.forEachIndexed { index, line ->
                                    hasResult = true
                                    println("inputStream  line:$line")
                                    if (callback?.onExecuteSuccess(line, size == index + 1) != true) {
                                        return@loop
                                    }
                                }
                            }
                            if (!hasResult) {
                                var hasErrorResult = false
                                process.errorReader()?.also {
                                    var line = it.readLine()
                                    while (line != null) {
                                        hasErrorResult = true
                                        callback?.onExecuteFail(line)
                                        line = it.readLine()
                                    }
                                }
                                if (!hasErrorResult) {
                                    callback?.onExecuteFail("")
                                }
                            }
                        } else {
                            callback?.onExecuteFail("Exception: process had been terminated.")
                        }
                    }
                } catch (ignore: Exception) {
                    callback?.onExecuteFail("Exception: $ignore")
                }
            }
        }
    }

    /**
     * 线程执行结果
     */
    interface IExecuteCallback {
        /**
         * @WorkThread
         */
        fun onExecuteSuccess(result: String, isLastLine: Boolean): Boolean?

        /**
         * @WorkThread
         */
        fun onExecuteFail(result: String)
    }
}