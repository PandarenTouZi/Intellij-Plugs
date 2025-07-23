package com.android.tool.action

import com.android.tool.Log
import com.android.tool.ThreadUtils
import com.android.tool.dialog.ADBDeviceChooseDialog
import com.android.tool.notify.BaseNotification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class EntranceAction : AnAction() {

    companion object {
        var entity: ADBDeviceEntity? = null
        var packageName: String? = null
    }

    override fun actionPerformed(p0: AnActionEvent) {
        Log.Companion.print("EntranceAction.actionPerformed ----打开工具主window  ${Thread.currentThread()}")

        val job = ThreadUtils.Companion.thread().launch {
            val adbDeviceList = readAdbDeviceList()
            Log.Companion.print("EntranceAction.actionPerformed   adbDeviceList.size:${adbDeviceList?.size}  ${Thread.currentThread()}")
            if (!adbDeviceList.isNullOrEmpty()) {
                if (adbDeviceList.size == 1) {
                    setDeviceChanged(adbDeviceList[0])
                } else {
                    ThreadUtils.Companion.threadMain().launch {
                        //选择一个
                        createChooseDeviceDialog(adbDeviceList)
                    }
                }
            } else {
                BaseNotification.Companion.notifyE("连接的设备", "没有发现连接设备")
            }
        }
        job.invokeOnCompletion {
            println("EntranceAction.actionPerformed ------------ 执行完成。。。。。。。。。。。。")
        }
        Log.Companion.print("EntranceAction.actionPerformed  command complete")
    }

    /**
     * 读取所有adb设备信息
     */
    private fun readAdbDeviceList(): List<ADBDeviceEntity>? {
        try {
            Runtime.getRuntime().exec(arrayOf("adb", "devices", "-l"), null)?.also { process ->
                if (process.isAlive) {
                    val stream = process.inputStream
                    val writer = BufferedReader(InputStreamReader(stream))
                    val devices = arrayListOf<ADBDeviceEntity>()
                    run readLoop@{
                        writer.readLines().forEach { line ->
                            if (line.isNotEmpty()) {
                                if (!line.startsWith("line List of devices attached", true)) {
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
                        "EntranceAction.actionPerformed result ${
                            devices.toTypedArray().contentToString()
                        }  ${Thread.currentThread()}"
                    )
                    return devices
                }
            }
        } catch (e: IOException) {
            Log.Companion.print("EntranceAction.actionPerformed  ${e.toString()}  ${Thread.currentThread()}")
        }
        return null
    }

    private fun createChooseDeviceDialog(list: List<ADBDeviceEntity>) {
        Log.Companion.print("EntranceAction.createChooseDeviceDialog   ${list.size}")
        ADBDeviceChooseDialog(list).apply {
            this.setOnSelectListener(object : ADBDeviceChooseDialog.OnSelectListener {
                override fun onSelect(entity: ADBDeviceEntity) {
                    setDeviceChanged(entity)
                }
            })
        }.showAndGet()
    }

    private fun setDeviceChanged(entity: ADBDeviceEntity) {
        if (Companion.entity != entity) {
            Companion.entity = entity
            BaseNotification.Companion.notifyE("连接的设备", "已选择新的设备：$entity")
        }
    }

    /**
     * adb设备信息
     */
    data class ADBDeviceEntity(
        /**
         * 序列号
         */
        val serial: String,
        /**
         *设备型号代码
         */
        val product: String,
        /**
         * 设备型号的内部标识符
         */
        val model: String,
        /**
         * 设备硬件平台的缩写(厂商+设备硬件代号)
         */
        val device: String,
        /**
         * 设备的通信通道ID
         */
        val transportId: String
    ) {
        override fun toString(): String {
//            return "[${super.toString()}], serial:$serial, product:$product, model:$model, device:$device, transportId:$transportId"
            return "$model ($serial)"
        }
    }
}