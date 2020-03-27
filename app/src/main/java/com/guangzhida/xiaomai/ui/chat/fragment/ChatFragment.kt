package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.model.TabEntity
import com.guangzhida.xiaomai.ui.chat.AddFriendsActivity
import com.guangzhida.xiaomai.ui.chat.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.fragment_message.*
import java.util.ArrayList

/**
 * 互动的fragment
 */
class ChatFragment : BaseFragment<MessageViewModel>() {
    private val mTitles = arrayOf("消息", "联系人")
    private val mFragments = listOf(
        ConversationFragment(),
        ContactListFragment()
    )
    private var mOldPos = 0
    private val mTabEntities = ArrayList<CustomTabEntity>()
    override fun layoutId(): Int = R.layout.fragment_message

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.flFragment, mFragments[0])
            .commitNow()
        mTabEntities.add(TabEntity(mTitles[0]))
        mTabEntities.add(TabEntity(mTitles[1]))
        tabLayout.setTabData(mTabEntities)
        tabLayout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                changeTab(position)
            }

            override fun onTabReselect(position: Int) {
            }

        })
    }

    override fun initListener() {
        ivAddFriend.setOnClickListener {
            startActivity(Intent(context, AddFriendsActivity::class.java))
        }
    }

    private fun changeTab(pos: Int) {
        val now = mFragments[pos]
        childFragmentManager.beginTransaction().apply {
            if (!now.isAdded) {
                add(R.id.flFragment, now)
            }
            show(now)
            hide(mFragments[mOldPos])
            commit()
            mOldPos = pos
        }
    }


}