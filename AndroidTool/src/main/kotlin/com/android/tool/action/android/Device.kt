package com.android.tool.action.android

import com.android.tool.Log
import com.android.tool.ThreadUtils
import com.android.tool.action.CommandConfig
import com.android.tool.action.EntranceAction
import com.android.tool.action.EntranceAction.ADBDeviceEntity
import com.android.tool.dialog.ADBDeviceChooseDialog
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class Device : AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        EntranceAction.entity = null
        start(null)
    }

    companion object {
        fun start(completeBlock: (() -> Unit)?) {
            ThreadUtils.Companion.thread().launch {
                val adbDeviceList = readAdbDeviceList()
                Log.Companion.print("Device   adbDeviceList.size:${adbDeviceList?.size}  ${Thread.currentThread()}")
                if (!adbDeviceList.isNullOrEmpty()) {
                    if (adbDeviceList.size == 1) {
                        setDeviceChanged(adbDeviceList[0], completeBlock)
                    } else {
                        ThreadUtils.Companion.threadMain().launch {
                            //选择一个
                            createChooseDeviceDialog(adbDeviceList, completeBlock)
                        }
                    }
                } else {
                    BaseNotification.Companion.notifyE("连接的设备", "没有发现连接设备")
                }
            }
        }


        /**
         * 读取所有adb设备信息
         */
        private fun readAdbDeviceList(): List<ADBDeviceEntity>? {
            try {
                Runtime.getRuntime().exec(CommandConfig.DEVICES_LIST, null)?.also { process ->
                    if (process.isAlive) {
                        val stream = process.inputStream
                        val writer = BufferedReader(InputStreamReader(stream))
                        val devices = arrayListOf<ADBDeviceEntity>()
                        run readLoop@{
                            writer.readLines().forEach { line ->
                                if (line.isNotEmpty()) {
                                    if (!line.startsWith(CommandConfig.DEVICES_LIST_START, true)) {
                                        //eg:JTK5T19910001215       device product:TAS-AL00 model:TAS_AL00 device:HWTAS transport_id:1
                                        val split = line.split(" ")
                                        if (split.size > 5) {
                                            var hasDevice = false
                                            var serial = ""
                                            var product = ""
                                            var model = ""
                                            var device = ""
                                            var transportId = ""
                                            split.forEachIndexed { index, info ->
                                                if (index == 0) {
                                                    serial = info
                                                } else {
                                                    if (info.isNotEmpty()) {
                                                        //是否处于解析device字段阶段
                                                        if (!hasDevice && info == "device") {
                                                            hasDevice = true
                                                            return@forEachIndexed
                                                        }
                                                        if (hasDevice) {
                                                            val semicolonIndex = info.indexOf(":")
                                                            val type = info.substring(0, semicolonIndex)
                                                            val value = info.substring(semicolonIndex + 1, info.length)
                                                            when (type) {
                                                                "product" -> {
                                                                    product = value
                                                                }

                                                                "model" -> {
                                                                    model = value
                                                                }

                                                                "device" -> {
                                                                    device = value
                                                                }

                                                                "transport_id" -> {
                                                                    transportId = value
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            devices.add(ADBDeviceEntity(serial, product, model, device, transportId))
                                        }
                                    }
                                }
                            }
                        }
                        Log.Companion.print(
                            "Device.actionPerformed result ${
                                devices.toTypedArray().contentToString()
                            }  ${Thread.currentThread()}"
                        )
                        return devices
                    }
                }
            } catch (e: IOException) {
                Log.Companion.print("Device.actionPerformed  ${e.toString()}  ${Thread.currentThread()}")
            }
            return null
        }

        /**
         * 选择设备面板
         */
        private fun createChooseDeviceDialog(list: List<ADBDeviceEntity>, completeBlock: (() -> Unit)?) {
            Log.Companion.print("Device.createChooseDeviceDialog   ${list.size}")
            ADBDeviceChooseDialog(list).apply {
                this.setOnSelectListener(object : ADBDeviceChooseDialog.OnSelectListener {
                    override fun onSelect(entity: ADBDeviceEntity) {
                        setDeviceChanged(entity, completeBlock)
                    }
                })
            }.showAndGet()
        }

        /**
         * 选中设备变更
         */
        private fun setDeviceChanged(entity: ADBDeviceEntity, completeBlock: (() -> Unit)?) {
            EntranceAction.entity = entity
            BaseNotification.Companion.notifyE("连接的设备", "已选择新的设备：$entity")

            //默认选择当前前台进程
            Process.parseFrontProcess(entity, completeBlock)
        }
    }
}