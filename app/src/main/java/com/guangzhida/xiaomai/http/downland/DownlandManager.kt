package com.guangzhida.xiaomai.http.downland

import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.utils.LogUtils
import com.vector.update_app.HttpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.*
import java.io.File
import java.io.IOException

class DownlandManager(
    fileInfo: FileInfo,
    callback: HttpManager.FileCallback
) {
    private val mCallBack = callback
    private val mFileInfo = fileInfo
    private val mSink: BufferedSink
    private val repository = InjectorUtil.getUpdateRepository()
    private var mContentSize: Long = 0L
    private var mDownLandSize: Long = 0L
    private var mProgressSource: Source? = null
    private var mFileNameTmp: String? = null

    init {
        mFileNameTmp = fileInfo.fileName + ".tmp"
        val dir = File(fileInfo.filePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(fileInfo.filePath, mFileNameTmp)
        if (file.exists()) {
            file.delete()
        }
        mSink = Okio.buffer(Okio.sink(file))
    }

    /**
     * 开始下载
     */
    fun downland() {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                mCallBack.onBefore()
                val result = withContext(Dispatchers.IO) {
                    val responseBody = repository.download(mFileInfo.url)
                    mContentSize = responseBody.contentLength()
                    mProgressSource = getProgressSource(responseBody.source())
                    mProgressSource?.let {
                        mSink.writeAll(Okio.buffer(it))
                        mSink.close()
                    }
                    val file =
                        File(mFileInfo.filePath + File.separator + mFileNameTmp)
                    val destFile =
                        File(mFileInfo.filePath + File.separator + mFileInfo.fileName)
                    if (destFile.exists()) {
                        destFile.delete()
                    }
                    val result =file.renameTo(File(mFileInfo.filePath + File.separator + mFileInfo.fileName))
                    if(result){
                        destFile
                    }else{
                        null
                    }
                 }
                if(result != null){
                    LogUtils.i("下载完成=${result.absolutePath}")
                    mCallBack.onResponse(result)
                }else{
                    mCallBack.onError("downland error")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                mCallBack.onError("downland error")
            } finally {

            }
        }
    }

    private fun onRead(read: Long) {
        mDownLandSize += if (read == -1L) 0 else read
        if (mDownLandSize > mContentSize) {
            mDownLandSize = mContentSize
        }
        val progress = (mDownLandSize * 1.0f) / mContentSize
        mCallBack.onProgress(progress, mContentSize)
    }

    private fun getProgressSource(source: Source): ForwardingSource? {
        return object : ForwardingSource(source) {
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val read = super.read(sink, byteCount)
                onRead(read)
                return read
            }
        }
    }

}