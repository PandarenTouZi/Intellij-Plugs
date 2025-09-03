package com.android.tool

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class Log {
    companion object {
        fun print(string: String) {
            println("${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}  $string")
        }

        fun print(tag: String, string: String) {
            println("${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}     $tag    $string")
        }

        fun printErrorStream(process: Process) {
            process.errorReader()?.also {
                var line = it.readLine()
                while (line != null) {
                    print("-------$line")
                    line = it.readLine()
                }
            }
        }
    }
}