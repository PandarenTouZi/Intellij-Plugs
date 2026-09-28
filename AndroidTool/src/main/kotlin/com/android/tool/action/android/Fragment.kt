package com.android.tool.action.android

import com.android.tool.RuntimeExecutor
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction.ADBDeviceEntity
import com.android.tool.notify.BaseNotification

class Fragment : NeedDeviceProcess() {

    override fun provideCommand(): String {
        return CommandConfig.COMMAND_TYPE_FRAGMENT
    }

    override fun doActionImp(entity: ADBDeviceEntity, packageName: String, completeBlock: ((Any?) -> Unit)?) {
        Activity().doActionImp(entity, packageName) { activityName ->
            if (activityName is String) {
                val command = CommandConfig.locateCurrentFragment(entity.serial, activityName)
                RuntimeExecutor.execute(command, object : RuntimeExecutor.IExecuteCallback {
                    private var isResultValid = false
                    private var appendMsg: String = ""
                    override fun onExecuteSuccess(result: String, isLastLine: Boolean): Boolean? {
                        val lastResultValid = isResultValid
                        //解析Fragment
                        if (result.endsWith("Added Fragments:")) {
                            isResultValid = true
                        } else if (isResultValid && result.contains(Regex("#[0-9]+:*"))) {
                            val indexOfFragmentStart = result.indexOf(":")
                            val indexOfFragmentEnd = result.indexOf("{")
                            if (indexOfFragmentStart >= 0 && indexOfFragmentEnd > indexOfFragmentStart) {
                                appendMsg += result.substring(indexOfFragmentStart + 1, indexOfFragmentEnd)
                            }
                        } else {
                            isResultValid = false
                        }
                        if (isLastLine || lastResultValid && !isResultValid) {
                            BaseNotification.notifyI(provideCommand(), "操作成功: $appendMsg")
                            return false
                        } else {
                            null
                        }
                        return true
                    }

                    override fun onExecuteFail(result: String) {
                        BaseNotification.notifyE(provideCommand(), "操作失败. $packageName")
                    }
                })
            }
        }
    }
}