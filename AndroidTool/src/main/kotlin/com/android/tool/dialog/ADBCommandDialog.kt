package com.android.tool.dialog

import com.android.tool.ThreadUtils
import com.android.tool.action.EntranceAction
import com.android.tool.effect.click
import com.android.tool.effect.select
import com.android.tool.notify.BaseNotification
import com.intellij.ui.JBColor
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * adb指令详情
 */
class ADBCommandDialog : BaseDialog {

    private var _textField: JTextField? = null
    private val onClickListener = object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            val source = e?.source
            println("ADBCommandDialog.mouseClicked $source")
            if (source is JComponent) {
                source.select(true)
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
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        var index = 0
        panel.add(JLabel("uninstall", SwingConstants.LEFT).apply {
            this.addMouseListener(onClickListener)
            this.name = "uninstall"
            this.isOpaque = true
            this.select(false)
            this.background = JBColor.BLUE
            this.maximumSize = Dimension(Int.MAX_VALUE, this.preferredSize.height)
        }, index++)

        _textField = JTextField().apply {
            this.isEnabled = false
            this.toolTipText = "输入框未启用"
            panel.add(this, BorderLayout.CENTER, index++)
        }
        return panel
    }

    override fun doOKAction() {
        EntranceAction.entity?.also { entity ->
            val text = _textField?.text
            EntranceAction.packageName = text
            if (text.isNullOrEmpty()) {
                BaseNotification.notifyE("操作失败", "包名为空")
            } else {
                ThreadUtils.threadMain().launch {
                    Runtime.getRuntime()
                        .exec(arrayOf("adb", "-s", entity.serial, "uninstall", text))
                }
                super.doOKAction()
            }
        }
    }

    override fun doCancelAction() {
        EntranceAction.packageName = _textField?.text
        super.doCancelAction()
    }
}