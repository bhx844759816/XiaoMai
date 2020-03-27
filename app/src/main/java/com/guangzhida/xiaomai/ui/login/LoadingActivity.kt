package com.guangzhida.xiaomai.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.USER_ACCOUNT_KEY
import com.guangzhida.xiaomai.USER_PASSWORD_KEY
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.rsADecode
import com.guangzhida.xiaomai.ui.MainActivity
import com.guangzhida.xiaomai.ui.login.viewmodel.LoadingViewModel
import com.guangzhida.xiaomai.utils.Base64Util
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.SPUtils

/**
 * loading页面
 */
class LoadingActivity : BaseActivity<LoadingViewModel>() {
    private var mCountDownTimer: MyCountDownTimer? = null

    override fun layoutId(): Int = R.layout.activity_loading

    override fun initView(savedInstanceState: Bundle?) {
        val phone = SPUtils.get(this, USER_ACCOUNT_KEY, "") as String
        val password = SPUtils.get(this, USER_PASSWORD_KEY, "") as String
        LogUtils.i("password:$password")
        //如果用户名和密码都不为空 调用登录接口(环信登录接口)
        if (phone.isNotEmpty() && password.isNotEmpty()) {
            mViewModel.doLogin(phone,  Base64Util.decodeWord(password))
        } else {
            mCountDownTimer = MyCountDownTimer(3 * 1000, 1000)
            mCountDownTimer?.start()
        }

        //登录完成
        mViewModel.loginFinish.observe(this, Observer {
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        })


//        EMClient.getInstance().login("bhx", "123", object : EMCallBack {
//            override fun onSuccess() {
//                runOnUiThread {
//                    ToastUtils.toastShort("登录成功")
//                    // 加载所有会话到内存
//                    EMClient.getInstance().chatManager().loadAllConversations()
//                    // 加载所有群组到内存
//                    EMClient.getInstance().groupManager().loadAllGroups();
//                    //跳转到主界面
//                    startActivity(Intent(this@LoadingActivity, MainActivity::class.java))
//                }
//            }
//
//            override fun onProgress(progress: Int, status: String?) {
//            }
//
//            override fun onError(code: Int, error: String?) {
//                //登录失败
//                ToastUtils.toastShort("登录失败")
//            }
//
//        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer?.cancel()
    }

    inner class MyCountDownTimer(
        private val millisInFuture: Long,
        private val countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            startActivity(Intent(this@LoadingActivity, MainActivity::class.java))
            this@LoadingActivity.finish()
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }
}