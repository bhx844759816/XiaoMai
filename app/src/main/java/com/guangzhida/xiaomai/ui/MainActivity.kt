package com.guangzhida.xiaomai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.receiver.WifiStateManager
import com.guangzhida.xiaomai.ui.home.HomeFragment
import com.guangzhida.xiaomai.ui.chat.fragment.ChatFragment
import com.guangzhida.xiaomai.ui.user.UserFragment
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 首页面
 */
class MainActivity : AppCompatActivity() {
    private val mFragments = listOf(HomeFragment(),
        ChatFragment(), UserFragment())
    private var mOldPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.id_fragment, mFragments[0])
            .commitNow()
        id_bottomNavigationBar.setTabSelectedListener(object :
            BottomNavigationBar.OnTabSelectedListener {
            override fun onTabReselected(position: Int) {
            }

            override fun onTabUnselected(position: Int) {
            }

            override fun onTabSelected(position: Int) {
                changeTab(position)
            }
        })
    }

    private fun changeTab(pos: Int) {
        val now = mFragments[pos]
        supportFragmentManager.beginTransaction().apply {
            if (!now.isAdded) {
                add(R.id.id_fragment, now)
            }
            show(now)
            hide(mFragments[mOldPos])
            commit()
            mOldPos = pos
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtils.i("注册网络监听")
        WifiStateManager.getInstance().registerNetReceiver(this)
    }

    override fun onPause() {
        super.onPause()
        LogUtils.i("取消注册网络监听")
        WifiStateManager.getInstance().unRegisterNetReceiver(this)
    }
}