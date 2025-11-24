package com.android.tool.dialog

import com.android.tool.RuntimeExecutor
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction
import com.android.tool.action.EntranceAction.ADBDeviceEntity
import com.android.tool.effect.select
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.ui.ComboBox
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

    private val mCommandTypeUninstall = "uninstall"
    private val mCommandTypeClean = "clean"
    private val mCommandTypeActivity = "activity"
    private val mCommandTypeDialog = "dialog"
    private val mCommandTypeFragment = "fragment"

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
                Command(mCommandTypeUninstall, CommandConfig.uninstall(deviceEntity.serial, process)),
                Command(mCommandTypeClean, CommandConfig.clean(deviceEntity.serial, process)),
                Command(mCommandTypeActivity, buildGetCurrentResumedActivityCommand(deviceEntity.serial)),
                Command(mCommandTypeDialog, CommandConfig.locateCurrentDialog(deviceEntity.serial)),
                Command(
                    mCommandTypeFragment,
                    CommandConfig.locateCurrentFragment(deviceEntity.serial, ""),
                    "getDependActivity"
                ),
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

    private fun buildGetCurrentResumedActivityCommand(serial: String) = CommandConfig.locateCurrentActivity(serial)
    private fun buildGetCurrentResumedFragmentCommand(serial: String, activity: String) =
        CommandConfig.locateCurrentFragment(serial, activity)

    override fun doOKAction() {
        EntranceAction.entity?.also { entity ->
            val text = EntranceAction.packageName ?: _textField?.text
            if (text.isNullOrEmpty()) {
                BaseNotification.notifyE("操作失败", "包名为空")
            } else {
                val selectedItem = comboBox?.selectedItem
                doCommandClick(selectedItem, entity)
            }
        }
    }

    private fun doCommandClick(selectedItem: Any?, entity: ADBDeviceEntity) {
        if (selectedItem is Command) {
            val command = if (selectedItem.name == mCommandTypeFragment) {
                if (selectedItem.status == "getDependActivity") {
                    buildGetCurrentResumedActivityCommand(entity.serial)
                } else {
                    buildGetCurrentResumedFragmentCommand(entity.serial, selectedItem.status ?: return)
                }
            } else {
                selectedItem.command
            }
            RuntimeExecutor.execute(command, object : RuntimeExecutor.IExecuteCallback {
                private var isResultValid = false
                private var appendMsg: String = ""
                override fun onExecuteSuccess(result: String, isLastLine: Boolean) {
                    val isProcessed: Boolean? = when (selectedItem.name) {
                        mCommandTypeUninstall -> {
                            true
                        }

                        mCommandTypeClean -> {
                            true
                        }

                        mCommandTypeActivity -> {
                            var getActivity = false
                            val indexOfActivityStart = result.indexOf("/.")
                            if (indexOfActivityStart >= 0) {
                                val indexOfActivityEnd = result.indexOf(" ", indexOfActivityStart)
                                if (indexOfActivityEnd > indexOfActivityStart) {
                                    val activityName =
                                        result.substring(indexOfActivityStart + 2, indexOfActivityEnd)
                                    if (activityName.isNotEmpty()) {
                                        getActivity = true
                                        appendMsg = activityName
                                    }
                                }
                            }
                            getActivity
                        }

                        mCommandTypeFragment -> {
                            if (selectedItem.status == "getDependActivity") {
                                var getActivity = false
                                val indexOfActivityStart = result.indexOf("/.")
                                if (indexOfActivityStart >= 0) {
                                    val indexOfActivityEnd = result.indexOf(" ", indexOfActivityStart)
                                    if (indexOfActivityEnd > indexOfActivityStart) {
                                        val activityName =
                                            result.substring(indexOfActivityStart + 2, indexOfActivityEnd)
                                        if (activityName.isNotEmpty()) {
                                            getActivity = true
                                            appendMsg = activityName
                                        }
                                    }
                                }
                                if (getActivity) {
                                    val copy = selectedItem.copy()
                                    copy.status = appendMsg
                                    appendMsg = ""
                                    doCommandClick(copy, entity)
                                    null
                                } else {
                                    false
                                }
                            } else {
                                //解析Fragment
                                if (result.endsWith("Added Fragments:")) {
                                    isResultValid = true
                                } else if (result.contains(Regex("#[0-9]+:*"))) {
                                    if (isResultValid) {
                                        val indexOfFragmentStart = result.indexOf(":")
                                        val indexOfFragmentEnd = result.indexOf("{")
                                        if (indexOfFragmentStart >= 0 && indexOfFragmentEnd > indexOfFragmentStart) {
                                            appendMsg += result.substring(indexOfFragmentStart + 1, indexOfFragmentEnd)
                                        }
                                    }
                                } else {
                                    isResultValid = false
                                }
                                if (isLastLine) {
                                    BaseNotification.notifyI(selectedItem.name, "操作成功: $appendMsg")
                                    true
                                } else {
                                    null
                                }
                            }
                        }

                        mCommandTypeDialog -> {
                            true
                        }

                        else -> {
                            false
                        }
                    }
                    if (isProcessed == true) {
                        BaseNotification.notifyI(selectedItem.name, "操作成功: $appendMsg")
                        appendMsg = ""
                    } else if (isProcessed == false) {
                        BaseNotification.notifyE(selectedItem.name, "操作失败. $result . $appendMsg")
                        appendMsg = ""
                    }
                }

                override fun onExecuteFail(result: String) {
                    BaseNotification.notifyE(selectedItem.name, "操作失败. $result")
                }
            })
            /*ThreadUtils.threadMain().launch {
                Runtime.getRuntime()
                    .exec(selectedItem.command).also {process ->
                        if (process.isAlive) {
                            process.
                        }
                    }
            }*/
//                    BaseNotification.notifyI(selectedItem.name, "操作成功")
        }
    }

    /**
     * 命令
     */
    inner class Command(val name: String, val command: Array<String>, var status: String? = null) {

        fun reset() {
            if (name == mCommandTypeFragment) {
                status = "getDependActivity"
            }
        }

        fun copy(): Command {
            return Command(name, command, status)
        }

        override fun toString(): String {
            return name
        }
    }
}