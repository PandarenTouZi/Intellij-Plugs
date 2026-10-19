package com.android.tool.action.other

import com.android.tool.ThreadUtils
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.actionSystem.AnAction
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

abstract class PowerShellParent : AnAction() {

    protected fun doActionImp(command: String) {
        ThreadUtils.threadIO().launch {
            val processBuilder = ProcessBuilder(
                arrayListOf(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-EncodedCommand",
                    Base64.getEncoder().encodeToString(command.toByteArray(StandardCharsets.UTF_16LE))
                )
            )
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            var bufferedReader: BufferedReader? = null
            try {
                bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String? = null
                line = bufferedReader.readLine()
                var result: String? = null
                while (line != null) {
                    //过滤
                    if (line != "#< CLIXML" && !line.startsWith("<Objs Version=")) {
                        println("PowerShellParent.actionPerformed line:$line")
                        result = line
                        break
                    }
                    line = bufferedReader.readLine()
                }

                if (result.isNullOrEmpty()) {
                    BaseNotification.notifyE("执行失败", "操作失败: ${this@PowerShellParent}")
                } else {
                    BaseNotification.notifyI("执行成功", "结果：$result")
                }
            } catch (e: Exception) {
                BaseNotification.notifyE("执行失败", "操作失败: $e")
            } finally {
                bufferedReader?.close()
            }
        }
    }
}