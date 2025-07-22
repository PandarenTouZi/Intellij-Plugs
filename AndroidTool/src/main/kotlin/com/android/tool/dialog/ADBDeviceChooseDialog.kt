package com.android.tool.dialog

import com.android.tool.EntranceAction.ADBDeviceEntity
import com.intellij.openapi.ui.ComboBox
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * adb设备选择
 */
class ADBDeviceChooseDialog() : BaseDialog() {

    private var _comboBox: ComboBox<ADBDeviceEntity>? = null
    private var list: List<ADBDeviceEntity>? = null
    private var onSelectListener: OnSelectListener? = null

    constructor(list: List<ADBDeviceEntity>) : this() {
        title = "选择连接的设备"
        this.list = list
        init()
    }

    override fun createCenterPanel(): JComponent? {
        list?.also { li ->
            val panel = JPanel(BorderLayout())
            _comboBox = ComboBox(li.toTypedArray()).apply {
                this.addActionListener(object : ActionListener {
                    override fun actionPerformed(e: ActionEvent?) {
                        println("ADBDeviceChooseDialog.actionPerformed $e")
                        val item = _comboBox?.selectedItem
                        if (item is ADBDeviceEntity) {
                            onSelectListener?.onSelect(item)
                        }
                    }
                })
            }
            panel.add(_comboBox, BorderLayout.CENTER)
            return panel
        }
        return null
    }

    fun setOnSelectListener(onSelectListener: OnSelectListener) {
        this.onSelectListener = onSelectListener
    }

    interface OnSelectListener {
        fun onSelect(entity: ADBDeviceEntity)
    }
}