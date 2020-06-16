package com.guangzhida.xiaomai.view

import android.app.Activity
import android.net.http.SslError
import android.webkit.*
import android.widget.LinearLayout
import com.google.gson.Gson
import com.guangzhida.xiaomai.utils.HttpUtils
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.just.agentweb.AgentWeb
import com.just.agentweb.WebViewClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.Jsoup


/**
 * 认证网络管理类
 */
object VerifyInternetManager {
    //    private val BASE_URL = "http://yonghu.guangzhida.cn"
    private val HOST = "10.8.8.10"
    private val BASE_URL = "http://$HOST"
    private var mAgentWeb: AgentWeb? = null
    // private var mAccount = "12121" //账号
//    private var mPassword = "11114222"//密码
    var mVerifyCallBack: ((Boolean, String) -> Unit)? = null
    private var mInsertJavaScrpit = ""
    private var mWebChromeClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            LogUtils.i("onPageFinished url=$url")
            if (url != null && url.contains("$BASE_URL/lfradius/portal/sxz/weblogin.html")) {
                view?.postDelayed({
                    view.loadUrl(mInsertJavaScrpit)
                }, 200)
            } else if (url != null && url.contains("$BASE_URL/lfradius/portal/sxz/success.html")) {
                mVerifyCallBack?.invoke(true, "认证成功")
            } else if (url != null && url.contains("$BASE_URL/lfradius/portal/sxz/fail.html")) {
                view?.loadUrl(
                    "javascript:window.local_obj.showVerifyErrorHtml('<head>'+"
                            + "document.getElementsByTagName('html')[0].innerHTML+'</head>');"
                );
                mAgentWeb?.destroy()
                mAgentWeb = null
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
     *
     * 调用一键认证
     */
    fun doVerify(
        activity: Activity,
        attachView: LinearLayout,
        account: String = "",
        passWord: String = "",
        callBack: ((Boolean, String) -> Unit)?
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
            CookieSyncManager.createInstance(activity)
            CookieManager.getInstance().removeAllCookie()
            mAgentWeb?.webCreator?.webView?.clearHistory()
            mAgentWeb?.webCreator?.webView?.clearFormData()
            mAgentWeb?.agentWebSettings?.webSettings?.domStorageEnabled = true
            mAgentWeb?.agentWebSettings?.webSettings?.javaScriptEnabled = true
            mAgentWeb?.agentWebSettings?.webSettings?.loadWithOverviewMode = true
            mAgentWeb?.agentWebSettings?.webSettings?.savePassword = false
            mAgentWeb?.agentWebSettings?.webSettings?.saveFormData = false
            mAgentWeb?.jsInterfaceHolder?.addJavaObject("local_obj", InJavaScriptLocalObj())
            this.mVerifyCallBack = callBack
        }
        mInsertJavaScrpit = buildString {
            append(
                """
            javascript:(function() {
             document.getElementById("user").value= '$account';
             document.getElementById("pass").value= '$passWord';
             var object = document.forms['_userregistr'];
             object.usrname.value = object.user.value;
             object.passwd.value = object.password1.value;
             object.target ="_top";
             object.action = document.location.protocol+'//$HOST/lfradius/libs/portal/20191107/portalweb.php?router=huawei&run=login';
             object.submit();
             })(); """
            )
        }
        LogUtils.i(mInsertJavaScrpit)
        mAgentWeb?.urlLoader?.loadUrl("http://www.baidu.com")
    }

    /**
     * 下线
     */
    fun quitVerify() {

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
        str?.let {
            val error = it.substring(
                it.indexOf("</script>失败原因：") + "</script>失败原因：".length,
                it.indexOf("<br>")
            )

            mVerifyCallBack?.invoke(false, error.trim())
        }
    }

    fun verifyBaiduHtml(html: String?) {
        if (html != null && html.isNotEmpty()) {
            val document = Jsoup.parse(html)
            mVerifyCallBack?.invoke(document.body().childrenSize() > 0, "认证成功")
        }
    }


    //
//    fun doVerifyNetWork(
//        activity: Activity,
//        account: String = "",
//        passWord: String = "",
//        attachView: LinearLayout
//        ) {
//        if (mAgentWeb == null) {
//            mAgentWeb = AgentWeb.with(activity)
//                .setAgentWebParent(attachView, LinearLayout.LayoutParams(-1, -1))
//                .useDefaultIndicator()
//                .setWebViewClient(mWebChromeClient)
//                .createAgentWeb()
//                .ready()
//                .get()
//            CookieSyncManager.createInstance(activity)
//            CookieManager.getInstance().removeAllCookie()
//            mAgentWeb?.webCreator?.webView?.clearHistory()
//            mAgentWeb?.webCreator?.webView?.clearFormData()
//            mAgentWeb?.agentWebSettings?.webSettings?.domStorageEnabled = true
//            mAgentWeb?.agentWebSettings?.webSettings?.javaScriptEnabled = true
//            mAgentWeb?.agentWebSettings?.webSettings?.loadWithOverviewMode = true
//            mAgentWeb?.agentWebSettings?.webSettings?.savePassword = false
//            mAgentWeb?.agentWebSettings?.webSettings?.saveFormData = false
//            mAgentWeb?.jsInterfaceHolder?.addJavaObject("local_obj", InJavaScriptLocalObj())
//        }
//
//        LogUtils.i(mInsertJavaScrpit)
//        mAgentWeb?.urlLoader?.loadUrl("http://www.baidu.com")
//    }

//    /**
//     * 提交参数
//     */
//    private fun postParams() {
//        Thread {
//            try {
//                val params = mapOf(
//                    "usrip" to "10.15.10.37",
//                    "usrmac" to "04:b1:67:67:4c:18",
//                    "usrname" to "12121",
//                    "passwd" to "11114222",
//                    "treaty" to "on",
//                    "nasid" to "1"
//                    )
//                LogUtils.i("post参数=$params")
//               val response =  HttpUtils.post(
//                    "http://10.8.8.10/lfradius/libs/portal/unify/portal.php/login/huawei_login",
//                   params
//                )
//                if(response != null){
//                   val str =  response.body()?.string()
//                    LogUtils.i("str=$str")
//                }
//            } catch (e: Throwable) {
//                e.printStackTrace()
//            }
//        }.start()
//    }

}