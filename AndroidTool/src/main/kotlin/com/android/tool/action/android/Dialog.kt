package com.android.tool.action.android

import com.android.tool.RuntimeExecutor
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction.ADBDeviceEntity
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class Dialog : NeedDeviceProcess() {

    override fun provideCommand(): String {
        return CommandConfig.COMMAND_TYPE_DIALOG
    }

    override fun doActionImp(entity: ADBDeviceEntity, packageName: String, completeBlock: ((Any?) -> Unit)?) {
        val command = CommandConfig.locateCurrentDialog(entity.serial)
        RuntimeExecutor.execute(command, object : RuntimeExecutor.IExecuteCallback {
            override fun onExecuteSuccess(result: String, isLastLine: Boolean): Boolean? {
                BaseNotification.notifyI(provideCommand(), "操作成功: $packageName")
                return true
            }

            override fun onExecuteFail(result: String) {
                BaseNotification.notifyE(provideCommand(), "操作失败. $packageName")
            }
        })
    }
}