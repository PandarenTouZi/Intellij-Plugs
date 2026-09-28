package com.android.tool.entrance

import com.android.tool.dialog.BaseDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.JComponent

/**
 * 入口
 */
class Entrance : AnAction() {

    override fun actionPerformed(p0: AnActionEvent) {
        println("Entrance.actionPerformed  $p0")
    }

    inner class EntranceDialog : BaseDialog("") {

        override fun createCenterPanel(): JComponent? {
            return super.createCenterPanel()
        }
    }
}