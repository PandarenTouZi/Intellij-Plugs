package com.android.tool.action

import com.android.tool.Log
import com.android.tool.dialog.ADBCommandDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * adb相关命令
 */
class ADBDetailAction: AnAction() {

    override fun actionPerformed(p0: AnActionEvent) {
        Log.print("adb指令详情")
        ADBCommandDialog().showAndGet()
    }
}