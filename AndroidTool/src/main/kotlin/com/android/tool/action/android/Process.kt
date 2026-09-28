package com.android.tool.action.android

import com.android.tool.RuntimeExecutor
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class Process : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        EntranceAction.entity?.also {
            parseFrontProcess(it, null)
            return
        }

        BaseNotification.notifyE("获取失败", "获取当前运行app进程失败：设备为空")
    }

    companion object {
        /**
         * 解析指定设备当前前台线程
         */
        fun parseFrontProcess(entity: EntranceAction.ADBDeviceEntity, completeBlock: (() -> Unit)?) {
            val command = CommandConfig.currentRunningTask(entity.serial)
            RuntimeExecutor.execute(command, object : RuntimeExecutor.IExecuteCallback {
                override fun onExecuteSuccess(result: String, isLastLine: Boolean): Boolean? {
                    var parsedProcessName: String? = null
                    val startIndex = result.indexOf("{")
                    if (startIndex >= 0) {
                        val splits = result.substring(startIndex, result.length - 1).split(" ")
                        splits.forEach {
                            if (it.contains("/")) {
                                parsedProcessName = it.substring(0, it.indexOf("/"))
                                return@forEach
                            }
                        }
                    }
                    if (parsedProcessName.isNullOrEmpty()) {
                        BaseNotification.notifyE("获取失败", "获取当前运行app进程失败")
                    } else {
                        EntranceAction.packageName = parsedProcessName
                        BaseNotification.notifyE("获取成功", "获取当前运行app进程：$parsedProcessName")
                    }
                    completeBlock?.invoke()
                    return true
                }

                override fun onExecuteFail(result: String) {
                    BaseNotification.notifyE("获取失败", "操作失败. $result")
                    completeBlock?.invoke()
                }
            })
        }
    }
}