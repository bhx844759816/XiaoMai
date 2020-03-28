package com.guangzhida.xiaomai.ui

import android.os.Bundle
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.LogUtils
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebChromeClient
import kotlinx.android.synthetic.main.activity_web.*

/**
 * Web界面显示
 */
class WebActivity : AppCompatActivity() {
    private var mAgentWeb: AgentWeb? = null
    private var mTitleWebClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            tvWebTitle.text = title ?: ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val url = intent.getStringExtra("url")
        val type = intent.getStringExtra("type")
        val params = intent.getStringExtra("params")
        LogUtils.i("web url=$url")
        initListener()
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(llWebParent, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebChromeClient(mTitleWebClient)
            .createAgentWeb()
            .ready()
            .go("")
        if (type == "AccountRegister") {
            //用户注册
            mAgentWeb?.urlLoader?.loadUrl(url)
        } else if (type == "AccountRecharge") {
            mAgentWeb?.urlLoader?.postUrl(
                url, params?.toByteArray()
            );
        } else if (type == "ForgetPassword") {
            mAgentWeb?.urlLoader?.loadUrl(url)
        }
    }

    private fun initListener() {
        ivCancel.setOnClickListener {
            finish()
        }
        toolBar.setNavigationOnClickListener {
            if (mAgentWeb?.back() != true) {
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