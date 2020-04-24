package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.model.TabEntity
import com.guangzhida.xiaomai.ui.chat.AddFriendsActivity
import com.guangzhida.xiaomai.ui.chat.viewmodel.ChatViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.android.synthetic.main.fragment_message.*
import java.util.ArrayList

/**
 * 互动的fragment
 */
class ChatFragment : BaseFragment<ChatViewModel>() {
    private val mTitles = arrayOf("消息", "联系人")
    private val mFragments = listOf(
        ConversationFragment(),
        ContactListFragment2()
    )
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private var mOld = 0
    override fun layoutId(): Int = R.layout.fragment_message

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mTabEntities.add(TabEntity(mTitles[0]))
        mTabEntities.add(TabEntity(mTitles[1]))
        tabLayout.setTabData(mTabEntities)
        tabLayout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                switchPage(position)
            }

            override fun onTabReselect(position: Int) {
            }

        })
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, mFragments[0])
            .commitNow()
    }

    override fun initListener() {
        ivAddFriend.setOnClickListener {
            startActivity(Intent(context, AddFriendsActivity::class.java))
        }
    }

    inner class MyFragmentPageAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = mFragments.size

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }


    private fun switchPage(index: Int) {
        val now = mFragments[index]
        childFragmentManager.beginTransaction().apply {
            if (!now.isAdded) {
                add(R.id.fragment, now)
            }
            show(now)
            hide(mFragments[mOld])
            commit()
            mOld = index
        }
    }

    fun selectDefault() {
        LogUtils.i("lifecycle.currentState =${lifecycle.currentState }")
        if (lifecycle.currentState == Lifecycle.State.STARTED) {
            switchPage(0)
            tabLayout.currentTab = 0
        }
//        if (viewPager != null)
//            viewPager.currentItem = 0
    }

}