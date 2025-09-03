package com.android.tool.action

interface CommandConfig {
    companion object {
        /**
         * 设备列表
         */
        val DEVICES_LIST = arrayOf("adb", "devices", "-l")

        /**
         * 设备列表-内容起始行
         */
        const val DEVICES_LIST_START = "line List of devices attached"

        /**
         * 停止adb服务
         */
        val KILL_ADB = arrayOf("adb", "kill-server")

        /**
         * 启动adb服务
         */
        val START_ADB = arrayOf("adb", "start-server")

        /**
         * 当前前台的进程
         * Linux and macOS:grep
         * Windows:findstr
         */
        fun currentRunningTask(devices: String) = arrayOf("adb", "-s", devices, "shell", "dumpsys", "window", "|", "grep", "mCurrentFocus")

        /**
         * 错误日志
         */
        val ANR_LOG = arrayOf("adb", "bugreport", "./bugreport.zip")

        /**
         * 日志
         */
        val LOG = arrayOf("adb", "logcat", "-v", "time", ">", "log.txt")

        /**
         * 清除缓存
         */
        fun clean(devices: String, process: String) = arrayOf("adb", "-s", devices, "shell", "pm", "clear", process)

        /**
         * 卸载
         */
        fun uninstall(devices: String, process: String) = arrayOf("adb", "-s", devices, "uninstall", process)
    }
}