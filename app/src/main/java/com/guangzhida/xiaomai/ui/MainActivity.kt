package com.guangzhida.xiaomai.ui

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.dialog.SchoolPhoneAccountLoginDialog
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.jumpLoginByState
import com.guangzhida.xiaomai.receiver.WifiStateManager
import com.guangzhida.xiaomai.ui.chat.fragment.InteractionFragment
import com.guangzhida.xiaomai.ui.home.HomeFragment
import com.guangzhida.xiaomai.ui.user.UserFragment
import com.guangzhida.xiaomai.utils.StatusBarTextUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.jaeger.library.StatusBarUtil
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 首页面
 */
class MainActivity : BaseActivity<MainViewModel>() {
    private val mFragments = listOf(
        HomeFragment(),
        InteractionFragment(),
       UserFragment()
    )
    private var mOldPos = 0
    private var exitTime: Long = 0

    override fun initView(savedInstanceState: Bundle?) {
        viewPager.adapter = MyFragmentPageAdapter2()
        viewPager.offscreenPageLimit = 3
        viewPager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                id_bottomNavigationBar.selectTab(position)
            }

        })
    }

    override fun initListener() {
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
        messageCountChangeLiveData.observe(this, Observer {
            if (BaseApplication.instance().mUserModel != null) {
                mViewModel.getFriendInvite()
            }
        })
        //有好友请求过来
        mViewModel.mFriendInviteCount.observe(this, Observer {
            if (it > 0) {
                (mFragments[1] as InteractionFragment).showBadgeView()
            } else {
                (mFragments[1] as InteractionFragment).hideBadgeView()
            }
            id_bottomNavigationBar.setUnReadMessageCount(it + ChatHelper.getUnReadMessageCount())
        })


    }

    /**
     *
     * 检索是否有好友请求，有的话设置小红点 小红点数加一
     *
     */

    override fun onResume() {
        super.onResume()
        if (BaseApplication.instance().mUserModel != null) {
            mViewModel.getFriendInvite()
        }
    }

    private fun changeTab(pos: Int) {
        viewPager.currentItem = pos
        if(pos == 0 || pos == 1){
            StatusBarUtil.setColorNoTranslucent(this, resources.getColor(R.color.white))
            StatusBarTextUtils.setLightStatusBar(this, true)
        }else{
            StatusBarUtil.setTranslucentForImageViewInFragment(this,0,null)
            StatusBarTextUtils.setLightStatusBar(this, false)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        WifiStateManager.getInstance().registerNetReceiver(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        WifiStateManager.getInstance().unRegisterNetReceiver(this)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtils.toastShort("再按一次退出程序")
            exitTime = System.currentTimeMillis()
        } else {
            moveTaskToBack(false)
        }
    }

    override fun layoutId(): Int = R.layout.activity_main

    inner class MyFragmentPageAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = mFragments.size

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }


    inner class MyFragmentPageAdapter2 :
        FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size


    }

}