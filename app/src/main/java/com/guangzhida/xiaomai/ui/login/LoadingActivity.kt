package com.guangzhida.xiaomai.ui.login

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guangzhida.xiaomai.APP_UPDATE_URL
import com.guangzhida.xiaomai.IS_SHOW_PROTOCOL_KEY
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.dialog.AppUpdateDialog
import com.guangzhida.xiaomai.dialog.DownlandProgressDialog
import com.guangzhida.xiaomai.dialog.ProtocolDialog
import com.guangzhida.xiaomai.ext.myDownload
import com.guangzhida.xiaomai.http.AppUpdateManager
import com.guangzhida.xiaomai.ktxlibrary.ext.getFormatFileSize
import com.guangzhida.xiaomai.ktxlibrary.ext.sharedpreference.getSpValue
import com.guangzhida.xiaomai.ktxlibrary.ext.sharedpreference.putSpValue
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.versionCode
import com.guangzhida.xiaomai.model.AppUpdateModel
import com.guangzhida.xiaomai.ui.MainActivity
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.login.viewmodel.LoadingViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.vector.update_app.UpdateAppBean
import com.vector.update_app.UpdateAppManager
import com.vector.update_app.utils.AppUpdateUtils
import com.vector.update_app_kotlin.check
import com.vector.update_app_kotlin.updateApp
import permissions.dispatcher.ktx.withPermissionsCheck
import kotlin.collections.HashMap


/**
 * loading页面
 */
class LoadingActivity : BaseActivity<LoadingViewModel>() {

    override fun layoutId(): Int = R.layout.activity_loading

    override fun initView(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        val isShowProtocolDialog = getSpValue(IS_SHOW_PROTOCOL_KEY, true)
        if (isShowProtocolDialog) {
            ProtocolDialog.showDialog(this, this, {
                startKtxActivity<WebActivity>(
                    values = listOf(
                        Pair("url", "file:///android_asset/ServiceAgreement.html"),
                        Pair("type", "protocol")
                    )
                )
            }, {
                startKtxActivity<WebActivity>(
                    values = listOf(
                        Pair("url", "file:///android_asset/PrivacyProtocol.html"),
                        Pair("type", "protocol")
                    )
                )
            }, {
                putSpValue(IS_SHOW_PROTOCOL_KEY, false)
                checkAppUpdatePermission()
            })
        } else {
            checkAppUpdatePermission()
        }
        mViewModel.loadingFinish.observe(this, Observer {
            startKtxActivity<MainActivity>()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
            );
            this.finish()
        })
    }

    private fun checkAppUpdate() {
        val map = HashMap<String, String>()
        map["type"] = "2" //标识是Android还是IOS
        map["version"] = versionCode.toString() //标识当前版本
        updateApp(APP_UPDATE_URL, AppUpdateManager())
        {
            isPost = true
            params = map
            themeColor = 0xff82A2FE.toInt()
            isIgnoreDefParams = true
            targetPath = ""
        }.check {
            parseJson {
                val response = it
                val appUpdateModel = Gson().fromJson<BaseResult<AppUpdateModel>>(
                    response!!, object : TypeToken<BaseResult<AppUpdateModel>>() {}.type
                )
                LogUtils.i("appUpdateModel=$appUpdateModel")
                if (appUpdateModel.status == 200) {
                    if (appUpdateModel.data.version > versionCode && appUpdateModel.data.url.isNotEmpty()) {
                        val isNeedUpdate = "Yes"
                        UpdateAppBean()
                            .setUpdate(isNeedUpdate)
                            .setNewVersion(appUpdateModel.data.version.toString())
                            .setApkFileUrl(appUpdateModel.data.url)
                            .setUpdateLog(appUpdateModel.data.message)
                            .setTargetSize(getFormatFileSize(appUpdateModel.data.appSize))
                            .setConstraint(appUpdateModel.data.isForcibly)
                    } else {
                        UpdateAppBean()
                            .setUpdate("no")
                    }
                } else {
                    UpdateAppBean()
                        .setUpdate("no")
                }
            }
            hasNewApp { updateApp, updateAppManager ->
                showUpdateDialog(updateApp, updateAppManager)
            }
            noNewApp {
                mViewModel.verifyToken()
            }
        }
    }

    /**
     * 检测更新
     */
    private fun checkAppUpdatePermission() =
        withPermissionsCheck(Manifest.permission.WRITE_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        })
        {
            checkAppUpdate()
        }


    /**
     * 展示更新提示的Dialog
     */
    private fun showUpdateDialog(updateApp: UpdateAppBean, appManager: UpdateAppManager) {
        AppUpdateDialog.showDialog(this, updateApp.updateLog) {
            appManager.myDownload {
                onStart {
                    DownlandProgressDialog.showDialog(this@LoadingActivity)
                }
                onProgress { progress, _ ->
                    runOnUiThread {
                        DownlandProgressDialog.changeProgress((progress * 100).toInt() + 1)
                    }
                }
                onFinish { file ->
                    if (file != null && file.exists()) {
                        AppUpdateUtils.installApp(this@LoadingActivity, file)
                    }
                    true
                }
            }
        }
    }
}