package com.android.tool.dialog

import com.android.tool.ThreadUtils
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction
import com.android.tool.effect.select
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.ui.ComboBox
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * adb指令详情
 */
class ADBCommandDialog : BaseDialog {

    private var _textField: JTextField? = null
    private var comboBox: ComboBox<Command>? = null
    private val onClickListener = object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            val source = e?.source
            println("ADBCommandDialog.mouseClicked $source")
            if (source is JComponent) {
                source.select(true)
                source.actionMap.allKeys().forEach {
                    println("ADBCommandDialog.mouseClicked key:$it, value:${source.actionMap.get(it)}")
                }
                when (source.name) {
                    "uninstall" -> {
                        _textField?.also { field ->
                            field.isEnabled = true
                            field.toolTipText = "输入包名"
                            if (EntranceAction.packageName.isNullOrEmpty()) {
                                field.putClientProperty("JTextField.placeholderText", "输入包名")
                            } else {
                                field.text = EntranceAction.packageName
                            }
                        }
                    }
                }
            }
        }
    }

    constructor() : super() {
        title = "指令详情"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val deviceEntity = EntranceAction.entity
        val process = EntranceAction.packageName ?: ""
        if (deviceEntity != null) {
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            val commands = arrayOf(
                Command("uninstall", CommandConfig.uninstall(deviceEntity.serial, process)),
                Command("clean", CommandConfig.clean(deviceEntity.serial, process))
            )
            comboBox = ComboBox(commands)
            panel.add(comboBox)
            _textField = JTextField().apply {
                this.isEnabled = false
                this.toolTipText = "输入框未启用"
                panel.add(this, BorderLayout.CENTER, 1)
            }
            return panel
        }
        return null
    }

    override fun doOKAction() {
        EntranceAction.entity?.also { entity ->
            val text = EntranceAction.packageName ?: _textField?.text
            if (text.isNullOrEmpty()) {
                BaseNotification.notifyE("操作失败", "包名为空")
            } else {
                val selectedItem = comboBox?.selectedItem
                if (selectedItem is Command) {
                    ThreadUtils.threadMain().launch {
                        Runtime.getRuntime()
                            .exec(selectedItem.command).also {process ->
                                if (process.isAlive) {
                                    BaseNotification.notifyI(selectedItem.name, "操作成功")
                                }
                            }
                    }
                }
            }
        }
    }

    /**
     * 命令
     */
    inner class Command(val name: String, val command: Array<String>) {
        override fun toString(): String {
            return name
        }
    }
}