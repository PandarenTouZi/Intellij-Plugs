package com.android.tool.action

interface CommandConfig {
    companion object {

        const val COMMAND_TYPE_UNINSTALL = "uninstall"
        const val COMMAND_TYPE_CLEAN = "clean"
        const val COMMAND_TYPE_ACTIVITY = "activity"
        const val COMMAND_TYPE_DIALOG = "dialog"
        const val COMMAND_TYPE_FRAGMENT = "fragment"
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

        /**
         * 当前显示的Activity
         */
        fun locateCurrentActivity(devices: String) = arrayOf("adb", "-s", devices, "shell", "dumpsys", "activity", "activities", "|", "grep", "ResumedActivity")

        /**
         * 当前显示的Dialog
         */
        fun locateCurrentDialog(devices: String) = arrayOf("adb", "-s", devices, "shell", "dumpsys", "window", "windows", "| grep -E mCurrentFocus")

        /**
         * 当前显示的Fragment
         */
        fun locateCurrentFragment(devices: String, activityName: String) = arrayOf("adb", "-s", devices, "shell", "dumpsys", "activity", activityName)
    }
}