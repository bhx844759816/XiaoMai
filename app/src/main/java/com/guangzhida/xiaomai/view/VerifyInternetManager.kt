package com.guangzhida.xiaomai.view

import android.app.Activity
import android.net.http.SslError
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.widget.LinearLayout
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebViewClient
import org.jsoup.Jsoup


/**
 * 认证网络管理类
 */
object VerifyInternetManager {
    private var mAgentWeb: AgentWeb? = null
    private var mAccount = "12121" //账号
    private var mPassword = "11114222"//密码
    var mVerifyCallBack: ((Boolean) -> Unit)? = null
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
                    view.loadUrl(insertJavaScript)
                }, 200)
            } else if (url != null && url.contains("http://10.8.8.10/lfradius/portal/sxz/success.html")) {
                //Success
                mVerifyCallBack?.invoke(true)

            } else if (url != null && url.contains("http://10.8.8.10/lfradius/portal/sxz/success.html")) {
                view?.loadUrl(
                    "javascript:window.local_obj.showVerifyErrorHtml('<head>'+"
                            + "document.getElementsByTagName('html')[0].innerHTML+'</head>');"
                );
            } else if (url != null && url.startsWith("https://m.baidu.com")) {
                view?.loadUrl(
                    "javascript:window.local_obj.showVerifyBaiduHtml('<head>'+"
                            + "document.getElementsByTagName('html')[0].innerHTML+'</head>');"
                );
            }
            super.onPageFinished(view, url)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.proceed();
        }
    }


    /**
     * 调用一键认证
     */
    fun doVerify(
        activity: Activity,
        attachView: LinearLayout,
        account: String = "",
        passWord: String = "",
        callBack: ((Boolean) -> Unit)?
    ) {
        if (account.isEmpty() || passWord.isEmpty()) {
            ToastUtils.toastShort("账号密码为空认证失败")
            return
        }
        if (mAgentWeb == null) {
            mAgentWeb = AgentWeb.with(activity)
                .setAgentWebParent(attachView, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebViewClient(mWebChromeClient)
                .createAgentWeb()
                .ready()
                .get()
            mAgentWeb?.agentWebSettings?.webSettings?.domStorageEnabled = true
            mAgentWeb?.agentWebSettings?.webSettings?.javaScriptEnabled = true
            mAgentWeb?.jsInterfaceHolder?.addJavaObject("local_obj", InJavaScriptLocalObj())
            this.mVerifyCallBack = callBack
        }
        mAgentWeb?.urlLoader?.loadUrl("http://www.baidu.com")
    }

    /**
     * 下线
     */
    fun  quitVerify(){

    }

    class InJavaScriptLocalObj {
        @JavascriptInterface
        fun showVerifyErrorHtml(html: String?) {
            LogUtils.i("html=$html")
            verifyErrorHtml(html)
        }

        @JavascriptInterface
        fun showVerifyBaiduHtml(html: String?) {
            verifyBaiduHtml(html)
        }

    }

    fun verifyErrorHtml(str: String?) {
        //error zhuru
        mVerifyCallBack?.invoke(false)
        LogUtils.i("html str=$str")
    }

    fun verifyBaiduHtml(html: String?) {
        if (html != null && html.isNotEmpty()) {
            val document= Jsoup.parse(html)
            mVerifyCallBack?.invoke(document.body().childrenSize()>0)
        }
    }
}