package com.android.tool.notify

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit

/**
 * 通知信息
 */
class BaseNotification {
    companion object {
        @Volatile
        private var _notification: Notification? = null
        private fun closeLastNotification() {
            println("BaseNotification.closeLastNotification  last $_notification")
            _notification?.also { notification ->
                AppExecutorUtil.getAppScheduledExecutorService().schedule({
                    notification.expire()
                }, 200, TimeUnit.MILLISECONDS)
            }
            _notification = null
        }

        /**
         * 提示信息
         */
        fun notifyI(title: String, msg: String) {
            //吐司提示
            Notifications.Bus.notify(Notification(buildString {
                append("Normal")
            }, buildString {
                append(title)
            }, buildString {
                append(msg)
            }, NotificationType.INFORMATION).apply {
                closeLastNotification()
                _notification = this
            })
        }

        /**
         * 错误信息
         */
        fun notifyE(title: String, msg: String) {
            //吐司提示
            Notifications.Bus.notify(Notification(buildString {
                append("Normal")
            }, buildString {
                append(title)
            }, buildString {
                append(msg)
            }, NotificationType.ERROR).apply {
                closeLastNotification()
                _notification = this
            })
        }

        /**
         * 警告信息
         */
        fun notifyW(title: String, msg: String) {
            //吐司提示
            Notifications.Bus.notify(Notification(buildString {
                append("Normal")
            }, buildString {
                append(title)
            }, buildString {
                append(msg)
            }, NotificationType.WARNING).apply {
                closeLastNotification()
                _notification = this
            })
        }
    }
}