package com.android.tool.dialog

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

open class BaseDialog : DialogWrapper {
    constructor() : super(true)
    constructor(title: String? = null) : this() {
        this.title = title
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel().apply {
            this.layout = BorderLayout()
//            val text = JLabel("选择")
//            this.add(text, BorderLayout.CENTER)
        }
        return panel
    }
}