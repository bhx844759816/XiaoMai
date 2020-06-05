package com.guangzhida.xiaomai.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.LogUtils
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*

/**
 * Web界面显示
 */
class WebActivity : AppCompatActivity() {
    private var mType: String? = null
    private var mAgentWeb: AgentWeb? = null
    private var mTitleWebClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            tvWebTitle.text = title ?: ""
        }
    }
    private var mWebChromeClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            LogUtils.i("onPageFinished url=$url")
            if (mType == "AccountRecharge" && url == "http://yonghu.guangzhida.cn/lfradius/home.php/login/showright") {
                mAgentWeb?.urlLoader?.loadUrl("http://yonghu.guangzhida.cn/lfradius/home.php/user/server")
            }
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            LogUtils.i("shouldOverrideUrlLoading=$url")
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            LogUtils.i(url)
            return super.shouldInterceptRequest(view, url)
        }
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              LogUtils.i(  request?.url?.path)
            }
            return super.shouldInterceptRequest(view, request)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val url = intent.getStringExtra("url")
        mType = intent.getStringExtra("type")
        val params = intent.getStringExtra("params")
        LogUtils.i("web url=$url")
        initListener()
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(llWebParent, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebChromeClient(mTitleWebClient)
            .setWebViewClient(mWebChromeClient)
            .createAgentWeb()
            .ready()
            .get()
        when (mType) {
            "AccountRegister" -> { //用户注册
                mAgentWeb?.urlLoader?.loadUrl(url)
            }
            "AccountRecharge" -> {//修改套餐
                mAgentWeb?.urlLoader?.postUrl(
                    url, params?.toByteArray()
                );
            }
            "ForgetPassword" -> {//校园网忘记密码
                mAgentWeb?.urlLoader?.loadUrl(url)
            }
            "protocol" -> { //用户服务协议隐私政策
                mAgentWeb?.urlLoader?.loadUrl(url)
            }
            "activity"->{//活动页面
                mAgentWeb?.urlLoader?.loadUrl(url)
            }
            "test"->{
                mAgentWeb?.urlLoader?.loadUrl(url)
            }
            "ad"->{
                mAgentWeb?.urlLoader?.loadUrl(url)
            }
        }
        val androidInterface = AndroidInterface(this)
        mAgentWeb?.jsInterfaceHolder?.addJavaObject("android",androidInterface)

    }

    private fun initListener() {
        ivCancel.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
        toolBar.setNavigationOnClickListener {
            if (mAgentWeb?.back() != true) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mAgentWeb?.webLifeCycle?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mAgentWeb?.webLifeCycle?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAgentWeb?.webLifeCycle?.onDestroy()
    }
}