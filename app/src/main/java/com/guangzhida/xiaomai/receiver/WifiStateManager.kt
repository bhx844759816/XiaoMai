package com.guangzhida.xiaomai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import com.guangzhida.xiaomai.event.netChangeLiveData
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.LogUtils.TAG
import com.guangzhida.xiaomai.utils.NetworkUtils


/**
 * Wifi状态的监听
 */
class WifiStateManager {
    private var mReceiver: NetWorkChangeReceiver? = null
    private var mConnectResult = false
    private var mIsBondService = false;

    companion object {
        fun getInstance() = SingletonHolder.INSTANCE
    }

    private object SingletonHolder {
        val INSTANCE = WifiStateManager()
    }


    fun registerNetReceiver(context: Context) {
        if (mReceiver == null) {
            mReceiver = NetWorkChangeReceiver()
            val filter = IntentFilter().apply {
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            }
            context.registerReceiver(mReceiver, filter);
        }
    }

    fun unRegisterNetReceiver(context: Context) {
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }

    /**
     * 网络状态改变的广播接收者
     */
    inner class NetWorkChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent != null) {
                val result = NetworkUtils.isWifiConnected(context)
                if (result != mConnectResult) {
                    mConnectResult = result
                    netChangeLiveData.postValue(mConnectResult)
                }
                LogUtils.i("当前wifi连接结果=$result")
//                //获得ConnectivityManager对象
//                val connMgr =
//                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;
//                val activeNetwork = connMgr.activeNetworkInfo;
//                if (activeNetwork != null) { // connected to the internet
//                    if (activeNetwork.isConnected) {
//                        if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
//                            Log.e(TAG, "当前WiFi连接可用 ");
//                        } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
//                            Log.e(TAG, "当前移动网络连接可用 ");
//                        }
//                    } else {
//                    }
//                } else {   // not connected to the internet
//                    Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
//
//                }
            }

        }
    }


    enum class WifiState {
        WIFI,//wifi
        GPRS,//移动网络
        NONE//没有网络
    }

}