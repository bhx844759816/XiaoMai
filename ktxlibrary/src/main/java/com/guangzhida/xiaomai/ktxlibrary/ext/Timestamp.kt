package com.guangzhida.xiaomai.ktxlibrary.ext

import java.text.SimpleDateFormat

fun Long.formatDateTime(format:String = "yyyy/MM/dd HH:mm"):String{
    return SimpleDateFormat(format).format(this)
}