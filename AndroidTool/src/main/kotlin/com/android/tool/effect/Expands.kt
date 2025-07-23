package com.android.tool.effect

import com.intellij.ui.JBColor

/**
 * 点击效果
 */
fun javax.swing.JComponent.click() {
    val component = this
    setBackground(JBColor.GREEN)
    javax.swing.Timer(300) { e ->
        component.setBackground(null)
        (e?.source as javax.swing.Timer).stop()
    }.start()
}

/**
 *
 */
fun javax.swing.JComponent.select(isSelect: Boolean) {
    setBackground(
        if (isSelect) {
            JBColor.GREEN
        } else {
            null
        }
    )
}