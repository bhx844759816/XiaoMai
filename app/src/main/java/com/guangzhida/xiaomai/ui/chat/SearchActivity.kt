package com.guangzhida.xiaomai.ui.chat

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.addTextChangedListener
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.model.SearchMessageModel
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_search_layout.*
import kotlinx.android.synthetic.main.layout_search_chat_message_parent.*
import kotlinx.android.synthetic.main.layout_search_contactlist_parent.*

/**
 * 搜索界面
 */
class SearchActivity : BaseActivity<SearchViewModel>() {
    private var mKey: String = ""
    override fun layoutId(): Int = R.layout.activity_search_layout

    override fun initView(savedInstanceState: Bundle?) {
        showKeyboard(etInput)
        initObserver()
    }

    override fun initListener() {
        etInput.addTextChangedListener {
            afterTextChanged {
                mKey = it?.toString() ?: ""
                if (mKey.isNotEmpty()) {
                    mViewModel.doSearch(mKey)
                }
            }
        }
        tvCancel.clickN {
            hideKeyboard()
            finish()
        }
    }

    /**
     * 初始化观察者
     */
    private fun initObserver() {
        mViewModel.searchChatMessageMap.observe(this, Observer {
            if (it.isEmpty()) {
                llChatMessageParent.gone()
            } else {
                llChatMessageParent.visible()
                showChatMessageRecordView(it)
            }
        })
        //查询到联系人
        mViewModel.searchUserEntityList.observe(this, Observer {
            if (it.isEmpty()) {
                llContactListParent.gone()
            } else {
                llContactListParent.visible()
                showContactListView(it)
            }
        })
    }

    /**
     * 展示搜索到好友列表的View
     */
    private fun showContactListView(list: List<UserEntity>) {
        if (list.size > 3) {
            llContactListBottomControl.visible()
        } else {
            llContactListBottomControl.gone()
        }
        //更多联系人
        llContactListBottomControl.setOnClickListener {
            startKtxActivity<SearchContactListActivity>(
                value = Pair("SearchKey", mKey)
            )
        }
        //填充view
        llContactListContent.removeAllViews()
        list.forEach {
            val itemView =
                LayoutInflater.from(this).inflate(R.layout.layout_search_user_entity_item, null)
            val ivHeaderView = itemView.findViewById<ImageView>(R.id.ivHeaderView)
            val tvName = itemView.findViewById<TextView>(R.id.tvName)
            val start = it.nickName.indexOf(mKey)
            val end = start + mKey.length

            val spannableString = SpannableString(it.nickName)
            spannableString.setSpan(
                ForegroundColorSpan(Color.parseColor("#33CC99")),
                start,
                end,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            tvName.text = spannableString
            ivHeaderView.loadFilletRectangle(
                BASE_URL.substring(0, BASE_URL.length - 1) + it.avatarUrl
            )
            itemView.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(67))
            llContactListContent.addView(
                itemView
            )
            //点击跳转到联系人详情
            itemView.clickN {
                startKtxActivity<PersonInfoActivity>(value = Pair("userName", it.userName))
            }
        }

    }

    /**
     * 展示搜索到聊天记录的View
     */
    private fun showChatMessageRecordView(map: Map<String, SearchMessageModel>) {
        if (map.size > 3) {
            llChatMessageBottomControl.visible()
        } else {
            llChatMessageBottomControl.gone()
        }
        llChatMessageBottomControl.setOnClickListener {
            startKtxActivity<SearchMessageListActivity>(
                value = Pair("SearchKey", mKey)
            )
        }
        llChatMessageContent.removeAllViews()
        map.forEach {
            val userEntity = it.value.userEntity
            val itemView =
                LayoutInflater.from(this).inflate(R.layout.layout_search_chat_message_item, null)
            val ivHeaderView = itemView.findViewById<ImageView>(R.id.ivHeaderView)
            val tvName = itemView.findViewById<TextView>(R.id.tvName)
            val tvSubTitle = itemView.findViewById<TextView>(R.id.tvSubTitle)
            ivHeaderView.loadFilletRectangle(
                BASE_URL.substring(0, BASE_URL.length - 1) + userEntity.avatarUrl
            )
            tvName.text = userEntity.nickName
            itemView.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp2px(67))
            tvSubTitle.text = buildString {
                append(it.value.messageCount)
                append("条相关的聊天记录")
            }
            //点击跳转到聊天记录里面去
            itemView.clickN {
                val params = listOf(
                    Pair("SearchKey", mKey),
                    Pair("UserName", userEntity.userName),
                    Pair("NickName", userEntity.nickName)
                )
                startKtxActivity<ChatMessageRecordListActivity>(values = params)
            }
            llChatMessageContent.addView(itemView)
        }

    }
}