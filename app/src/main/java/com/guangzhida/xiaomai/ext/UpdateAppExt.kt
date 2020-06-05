package com.guangzhida.xiaomai.ext

import com.vector.update_app.UpdateAppManager
import com.vector.update_app.service.DownloadService
import java.io.File

inline fun UpdateAppManager.myDownload(init: DownloadCallback.() -> Unit) {
    download(DownloadCallback().apply(init))
}

class DownloadCallback : DownloadService.DownloadCallback {


    private var _onStart: (() -> Unit)? = null
    private var _onFinish: ((file:File?) -> Boolean)? = null
    private var _onError: ((msg: String) -> Unit)? = null
    private var _setMax: ((totalSize: Long) -> Unit)? = null
    private var _onInstallAppAndAppOnForeground: ((file: File) -> Boolean)? = null
    private var _onProgress: ((progress: Float, totalSize: Long) -> Unit)? = null

    override fun onStart() {
        _onStart?.invoke()
    }

    override fun onProgress(progress: Float, totalSize: Long) {
        _onProgress?.invoke(progress, totalSize)
    }

    override fun setMax(totalSize: Long) {
        _setMax?.invoke(totalSize)
    }


    override fun onFinish(file: File?): Boolean {
        if (_onFinish != null) {
            return _onFinish!!.invoke(file)
        } else {
            return true
        }
    }

    override fun onError(msg: String) {
        _onError?.invoke(msg)
    }

    override fun onInstallAppAndAppOnForeground(file: File?): Boolean {

        return _onInstallAppAndAppOnForeground?.invoke(file!!)!!
    }

    fun onStart(listener: () -> Unit) {
        _onStart = listener
    }

    fun onFinish(listener: (file:File?) -> Boolean) {
        _onFinish = listener
    }

    fun onError(listener: (msg: String) -> Unit) {
        _onError = listener
    }

    fun setMax(listener: (totalSize: Long) -> Unit) {
        _setMax = listener
    }

    fun onProgress(listener: (progress: Float, totalSize: Long) -> Unit) {
        _onProgress = listener
    }

    fun onInstallAppAndAppOnForeground(listener: (file: File) -> Boolean) {
        _onInstallAppAndAppOnForeground = listener
    }
}