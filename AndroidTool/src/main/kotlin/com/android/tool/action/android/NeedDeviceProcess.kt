package com.android.tool.action.android

import com.android.tool.action.EntranceAction
import com.android.tool.action.EntranceAction.ADBDeviceEntity
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

abstract class NeedDeviceProcess : AnAction() {

    private var retryCount = 0

    override fun actionPerformed(p0: AnActionEvent) {
        val entity = EntranceAction.entity
        if (entity == null) {
            if (retryCount > 1) {
                BaseNotification.notifyE(provideCommand(), "操作失败: retryCount：$retryCount")
                return
            }
            Device.start {
                retryCount++
                actionPerformed(p0)
            }
            return
        }
        val packageName = EntranceAction.packageName
        if (packageName.isNullOrEmpty()) {
            if (retryCount > 1) {
                BaseNotification.notifyE(provideCommand(), "操作失败: retryCount：$retryCount")
                return
            }
            Process.parseFrontProcess(entity) {
                retryCount++
                actionPerformed(p0)
            }
            return
        }
        retryCount = 0
        doActionImp(entity, packageName, null)
    }

    abstract fun provideCommand(): String

    open fun doActionImp(entity: ADBDeviceEntity, packageName: String, completeBlock: ((Any?) -> Unit)?) = Unit
}