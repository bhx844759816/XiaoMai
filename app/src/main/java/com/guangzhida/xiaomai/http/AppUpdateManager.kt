package com.guangzhida.xiaomai.http

import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.http.downland.DownlandManager
import com.guangzhida.xiaomai.http.downland.FileInfo
import com.guangzhida.xiaomai.utils.LogUtils
import com.vector.update_app.HttpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okio.Buffer
import okio.ForwardingSource
import okio.Okio
import okio.Source
import java.io.File
import java.io.IOException

class AppUpdateManager : HttpManager {
    private val repository = InjectorUtil.getUpdateRepository()

    override fun download(
        url: String,
        path: String,
        fileName: String,
        callback: HttpManager.FileCallback
    ) {
        LogUtils.i("path:$path")
        LogUtils.i("fileName:$fileName")
        val fileInfo = FileInfo(
            url = url,
            filePath = path,
            fileName = fileName
        )
        DownlandManager(fileInfo, callback).downland()
    }

    override fun asyncGet(
        url: String,
        params: MutableMap<String, String>,
        callBack: HttpManager.Callback
    ) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val message = withContext(Dispatchers.IO) {
                    val response = repository.asyncGet(url, params)
                    response.string()
                }
                callBack.onResponse(message)
            } catch (e: Throwable) {
                e.printStackTrace()
                callBack.onError(e.localizedMessage)
            }
        }

    }


    override fun asyncPost(
        url: String,
        params: MutableMap<String, String>,
        callBack: HttpManager.Callback
    ) {

        val requestBodyMaps = mutableMapOf<String, RequestBody>()
        params.forEach {
            val key = it.key
            val requestBody =
                RequestBody.create(okhttp3.MediaType.parse("multipart/form-data"), it.value)
            requestBodyMaps[key] = requestBody
        }
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val message = withContext(Dispatchers.IO) {
                    val response = repository.asyncPost(url, requestBodyMaps)
                    response.string()
                }
                callBack.onResponse(message)
            } catch (e: Throwable) {
                e.printStackTrace()
                callBack.onError(e.localizedMessage)
            }
        }

    }

}