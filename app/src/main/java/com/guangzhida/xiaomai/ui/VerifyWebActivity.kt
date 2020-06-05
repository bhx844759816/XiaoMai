package com.guangzhida.xiaomai.ui

import android.os.Bundle
import android.webkit.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.LogUtils
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*

class VerifyWebActivity : AppCompatActivity() {
    private var mAgentWeb: AgentWeb? = null
    private var mAccount = 12121 //账号
    private var mPassword = 11114222//密码
    //    val insertJavaScript = """
//            javascript:(function() {
//            document.getElementById("user").value= '$mAccount';
//            document.getElementById("pass").value= '$mPassword';
//            document.forms['_'].submit();
//            })();"""

    val insertJavaScript = """
            javascript:(function() {
             document.getElementById("user").value= '$mAccount';
             document.getElementById("pass").value= '$mPassword';
             var object = document.forms['_userregistr'];
             object.usrname.value = object.user.value;
             object.passwd.value = object.password1.value;
             object.target ="_top";
             object.action = document.location.protocol+'//10.8.8.10/lfradius/libs/portal/20191107/portalweb.php?router=huawei&run=login';
             object.submit();
             })(); """
    private var mWebChromeClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            LogUtils.i("onPageFinished url=$url")
            if (url != null && url.contains("http://10.8.8.10/lfradius/portal/sxz/weblogin.html")) {
                view?.postDelayed({
                    LogUtils.i("onPageFinished loadUrl=$insertJavaScript")
                    view.loadUrl(insertJavaScript)
                }, 200)
            } else if (url != null && url.contains("http://10.8.8.10/lfradius/portal/sxz/success.html")) {
                //Success


            } else if (url != null && url.contains("http://10.8.8.10/lfradius/portal/sxz/success.html")) {
               //error zhuru

            }
            super.onPageFinished(view, url)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            LogUtils.i("onReceivedError error=${error}")
            super.onReceivedError(view, request, error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_web_layout)
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(llWebParent, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .setWebViewClient(mWebChromeClient)
            .createAgentWeb()
            .ready()
            .get()
        CookieSyncManager.createInstance(this)
        CookieManager.getInstance().removeAllCookie()
        mAgentWeb?.webCreator?.webView?.clearHistory()
        mAgentWeb?.webCreator?.webView?.clearFormData()
        mAgentWeb?.agentWebSettings?.webSettings?.domStorageEnabled = true
        mAgentWeb?.agentWebSettings?.webSettings?.javaScriptEnabled = true
        mAgentWeb?.agentWebSettings?.webSettings?.loadWithOverviewMode = true
        mAgentWeb?.agentWebSettings?.webSettings?.savePassword = false
        mAgentWeb?.agentWebSettings?.webSettings?.saveFormData = false
        //http://10.8.8.10/lfradius/libs/portal/unify/portal.php/login/main/nasid/1/?wlanacname=route1&wlanacip=10.15.10.1&wlanuserip=10.15.10.37&mac=60:ee:5c:23:38:20&url=http%3A%2F%2F
        mAgentWeb?.urlLoader?.loadUrl("http://www.baidu.com")
    }
}