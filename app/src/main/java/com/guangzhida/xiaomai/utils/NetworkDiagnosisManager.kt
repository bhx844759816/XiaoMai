package com.guangzhida.xiaomai.utils

import com.guangzhida.xiaomai.model.PingResultModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.DecimalFormat

/**
 * 网络诊断的辅助工具类
 */
object NetworkDiagnosisManager {
    var process: Process? = null
    /**
     * 执行ping的命令
     * @param ip ping的地址
     * @count count ping的次数
     * @count timeout ping的超时时间 ms单位
     */
    fun executeCmd(ip: String, count: Int, timeout: Int): Pair<Boolean, String>? {
        val cmd = "ping -c $count -w $timeout $ip"
        var reader: BufferedReader? = null
        val stringBuilder = StringBuilder()
        val timeList = mutableListOf<String>()
        try {
            process = Runtime.getRuntime().exec(cmd)// ping网址3次
            reader = BufferedReader(InputStreamReader(process!!.inputStream))
            BufferedReader(InputStreamReader(process!!.inputStream)).use {
                var line: String
                while (true) {
                    line = it.readLine() ?: break //当有内容时读取一行数据，否则退出循环
                    operateLine(ip, line, stringBuilder, timeList)
                }
            }
            //计算发包丢失包和平均时间
            if (timeList.isNotEmpty()) {
                var fastTime = timeList[0].toDouble();
                var longTime = timeList[0].toDouble();
                var totalTime = 0.0;
                timeList.forEach {
                    totalTime += it.toDouble()
                    if (it.toDouble() < fastTime) {
                        fastTime = it.toDouble()
                    }
                    if (it.toDouble() > longTime) {
                        longTime = it.toDouble()
                    }
                }
                stringBuilder.append(",最短=")
                    .append(fastTime)
                    .append("ms")
                    .append(",最长=")
                    .append(longTime)
                    .append("ms")
                    .append(",平均=")
                    .append(String.format("%.2f", totalTime / timeList.size))
                    .append("ms")
            }
            val status = process!!.waitFor()
            return Pair(status == 0, stringBuilder.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            CloseUtils.closeIO(reader)
            process?.destroy()
        }
        return null
    }

    /**
     * 操作解析的行数据
     */
    private fun operateLine(
        ip: String,
        line: String,
        stringBuilder: StringBuilder,
        list: MutableList<String>
    ) {
        if (line.contains("bytes from") && line.contains("ttl=") && line.contains("time=")) {
            val time = line.split("time=")[1].split(" ms")[0].trim();
            val icmp_seq = line.split("icmp_seq=")[1].split(" ttl")[0].trim();
            val ttl = line.split("ttl=")[1].split(" time")[0].trim();
            val content = buildString {
                append("来自 ")
                append(ip)
                append(" 的回复 ")
                append(" icmp_seq=")
                append(icmp_seq)
                append(" ttl=")
                append(ttl)
                append(" 时间=")
                append(time)
                append("ms")
            }
            stringBuilder.append(content).append("\n")
            list.add(time)
        } else if (line.contains("packets transmitted") && line.contains("received")) {
            val temp = line.split(":")
            val res = temp[temp.size - 1] //分开时间与返回的结果
            val resArray = res.split(",") //分开每个结果
            var time = "";
            var send = "";
            var receive = "";
            var lost = "";
            for (s in resArray) {
                when {
                    s.contains("packets transmitted") -> {
                        send = s.split("packets transmitted")[0].trim();
                    }
                    s.contains("received") -> {
                        receive = s.split("received")[0].trim();
                    }
                    s.contains("packet loss") -> {
                        lost = s.split("packet loss")[0].trim();
                    }
                    s.contains("time") -> {
                        time = s.split("time")[1].trim();
                    }
                }
            }
            stringBuilder.append(ip).append("的ping统计信息").append("\n")
            val content = buildString {
                append("数据包:已发送=")
                append(send)
                append(", 已接收=")
                append(receive)
                append(",\n")
                append("平均丢失=")
                append(lost)
            }
            stringBuilder.append(content)
        }
    }

    /**
     *
     * 执行cmd命令 获取执行结果
     * status=0 成功 其他表示失败
     * averageDelay 平均延迟
     * lossPackageRate 丢包率
     */
    fun executeCmd2(ip: String, count: Int, timeout: Int): PingResultModel {
        val model = PingResultModel()
        var lossPackageRate = ""
        var averageDelay = ""
        try {
            val cmd = "ping -c $count -w $timeout $ip"
            process = Runtime.getRuntime().exec(cmd)// ping网址3次
            BufferedReader(InputStreamReader(process!!.inputStream)).use {
                var line: String
                while (true) {
                    line = it.readLine() ?: break //当有内容时读取一行数据，否则退出循环
                    if (averageDelay.isEmpty()) {
                        averageDelay = getPingDelayList(line)
                    }
                    if (lossPackageRate.isEmpty()) {
                        lossPackageRate = getLossPackageRate(line)
                    }
                }
            }
            val status = process?.waitFor()
            model.success = status == 0
            LogUtils.i("lossPackageRate=$lossPackageRate")
            if (lossPackageRate.isNotEmpty()) {
                model.lossPackageRate = lossPackageRate
            }
            if (averageDelay.isNotEmpty()) {
                model.averageDelay = averageDelay
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
        return model
    }

    /**
     * 取消命令执行
     */
    fun cancelExecute() {
        process?.destroy()
    }

    /**
     * 获取ping延迟的list
     */
    private fun getPingDelayList(line: String, list: MutableList<String>) {
        if (line.contains("bytes from") && line.contains("ttl=") && line.contains("time=")) {
            val time = line.split("time=")[1].split(" ms")[0].trim();
            list.add(time)
        }
    }

    private fun getPingDelayList(line: String): String {
        if (line.contains("rtt") && line.contains("min/avg/max")) {
            return line.split("=")[1].trim().split("/")[1].plus("ms")
        }
        return ""
    }

    /**
     * 获取丢包率
     */
    private fun getLossPackageRate(line: String): String {
        if (line.contains("packets transmitted") && line.contains("received")) {
            val temp = line.split(":")
            val res = temp[temp.size - 1] //分开时间与返回的结果
            val resArray = res.split(",") //分开每个结果
            for (s in resArray) {
                when {
                    s.contains("packet loss") -> {
                        return s.split("packet loss")[0];
                    }
                }
            }
        }
        return ""
    }
}