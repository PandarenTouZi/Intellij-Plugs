package com.android.tool.action.other

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory

class PowerShellType : PowerShellParent() {

    override fun actionPerformed(p0: AnActionEvent) {
        val fileChooser = FileChooserFactory.getInstance()
            .createFileChooser(FileChooserDescriptorFactory.singleFile(), p0.project, null)
        val choose = fileChooser.choose(p0.project)
        if (choose.isNotEmpty()) {
            choose[0]?.also {
                doActionImp("type ${it.canonicalPath} -Head 1")
//                doActionImp("(Get-Item ${it.canonicalPath}).Extension")
//                doActionImp("(Get-Content \"${it.canonicalPath}\" -Encoding Byte -TotalCount 24 | ForEach-Object { '{0:X2}' -f \$_ }) -join ''")
            }
        }
    }
}