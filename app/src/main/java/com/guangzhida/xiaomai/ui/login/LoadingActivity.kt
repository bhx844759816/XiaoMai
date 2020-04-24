package com.guangzhida.xiaomai.ui.login

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guangzhida.xiaomai.APP_UPDATE_URL
import com.guangzhida.xiaomai.IS_SHOW_PROTOCOL_KEY
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.base.BaseResult
import com.guangzhida.xiaomai.dialog.ProtocolDialog
import com.guangzhida.xiaomai.http.AppUpdateManager
import com.guangzhida.xiaomai.ktxlibrary.ext.sharedpreference.getSpValue
import com.guangzhida.xiaomai.ktxlibrary.ext.sharedpreference.putSpValue
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.versionCode
import com.guangzhida.xiaomai.model.AppUpdateModel
import com.guangzhida.xiaomai.ui.MainActivity
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.login.viewmodel.LoadingViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.SPUtils
import com.orhanobut.logger.Logger.json
import com.vector.update_app.UpdateAppBean
import com.vector.update_app.UpdateAppManager
import com.vector.update_app.utils.AppUpdateUtils
import com.vector.update_app_kotlin.check
import com.vector.update_app_kotlin.download
import com.vector.update_app_kotlin.updateApp
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions


/**
 * loading页面
 */
@RuntimePermissions
class LoadingActivity : BaseActivity<LoadingViewModel>() {

    override fun layoutId(): Int = R.layout.activity_loading

    override fun initView(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        val isShowProtocolDialog = getSpValue(IS_SHOW_PROTOCOL_KEY,true)
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
                //checkAppUpdateWithPermissionCheck()
                mViewModel.verifyToken()
            })
        } else {
            //checkAppUpdateWithPermissionCheck()
            mViewModel.verifyToken()
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

    @NeedsPermission(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun checkAppUpdate() {
        val _params = HashMap<String, String>()
        _params["type"] = "2" //标识是Android还是IOS
        _params["version"] = versionCode.toString() //标识当前版本
        updateApp(APP_UPDATE_URL, AppUpdateManager())
        {
            isPost = true
            params = _params
            themeColor = 0xff82A2FE.toInt()
            isIgnoreDefParams = true
        }.check {
            parseJson {
                val response = it
                val appUpdateModel = Gson().fromJson<BaseResult<AppUpdateModel>>(
                    response!!, object : TypeToken<BaseResult<AppUpdateModel>>() {}.type
                )
                LogUtils.i("appUpdateModel=$appUpdateModel")
                if (appUpdateModel.status == 200) {
                    val isNeedUpdate = "Yes"
                    UpdateAppBean()
                        .setUpdate(isNeedUpdate)
                        .setNewVersion(appUpdateModel.result.version ?: "")
                        .setApkFileUrl("http://rel.huya.com/apk/live.apk")
                        .setUpdateLog(appUpdateModel.result.message ?: "")
                        .setTargetSize(appUpdateModel.result.appSize ?: "")
                        .setConstraint(appUpdateModel.result.isForcibly ?: true)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    /**
     * 展示更新提示的Dialog
     */
    private fun showUpdateDialog(updateApp: UpdateAppBean, appManager: UpdateAppManager) {
        val content = buildString {
            //            append("是否更新到新版本${updateApp.newVersion}\n\n")
            append(updateApp.updateLog)
        }
        MaterialDialog(this)
            .cancelable(false)
            .cornerRadius(8f)
            .title(text = "检测更新")
            .message(text = content)
            .positiveButton(text = "升级", click = {
                appManager.download {
                    this.onFinish {
                        true
                    }
                    this.onInstallAppAndAppOnForeground { file ->
                        AppUpdateUtils.installApp(this@LoadingActivity, file)
                    }
                }
                it.dismiss()
                this@LoadingActivity.finish()
            })
            .lifecycleOwner(this)
            .show()
    }


}