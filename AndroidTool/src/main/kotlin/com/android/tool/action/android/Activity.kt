package com.android.tool.action.android

import com.android.tool.RuntimeExecutor
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction.ADBDeviceEntity
import com.android.tool.notify.BaseNotification

class Activity : NeedDeviceProcess() {

    override fun provideCommand(): String {
        return CommandConfig.COMMAND_TYPE_ACTIVITY
    }

    override fun doActionImp(entity: ADBDeviceEntity, packageName: String, completeBlock: ((Any?) -> Unit)?) {
        val command = CommandConfig.locateCurrentActivity(entity.serial)
        RuntimeExecutor.execute(command, object : RuntimeExecutor.IExecuteCallback {
            override fun onExecuteSuccess(result: String, isLastLine: Boolean): Boolean? {
                var indexOfActivityStart = result.indexOf("/.")
                if (indexOfActivityStart < 0) {
                    indexOfActivityStart = result.indexOf("/")
                }
                if (indexOfActivityStart >= 0) {
                    val indexOfActivityEnd = result.indexOf(" ", indexOfActivityStart)
                    if (indexOfActivityEnd > indexOfActivityStart) {
                        val activityName =
                            result.substring(indexOfActivityStart + 2, indexOfActivityEnd)
                        if (activityName.isNotEmpty()) {
                            BaseNotification.notifyI(provideCommand(), "操作成功: $activityName")
                            completeBlock?.invoke(activityName)
                            return false
                        }
                    }
                }
                return true
            }

            override fun onExecuteFail(result: String) {
                BaseNotification.notifyE(provideCommand(), "操作失败. $packageName")
            }
        })
    }
}