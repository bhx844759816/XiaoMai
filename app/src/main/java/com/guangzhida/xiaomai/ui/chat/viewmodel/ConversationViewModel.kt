package com.guangzhida.xiaomai.ui.chat.viewmodel

import android.util.Pair
import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ConversationViewModel : BaseViewModel() {
    val mConversationListLiveData = MutableLiveData<MutableList<EMConversation>>()
    /**
     * 加载会话列表
     */
    @Synchronized
    fun loadConversationList() {
        launchUI {
            try { //
                val list = withContext(Dispatchers.IO) {
                    val conversations =
                        EMClient.getInstance().chatManager().allConversations
                    val sortList: MutableList<Pair<Long, EMConversation>> =
                        ArrayList()
                    for (conversation in conversations.values) {
                        if (conversation.allMessages.size != 0) {
                            sortList.add(
                                Pair(
                                    conversation.lastMessage.msgTime,
                                    conversation
                                )
                            )
                        }
                    }
                    val listBySort = sortList.sortedBy {
                        it.first
                    }
                    val list: MutableList<EMConversation> =
                        ArrayList()
                    for (sortItem in listBySort) {
                        list.add(sortItem.second)
                    }
                    list
                }
                if (list.isNotEmpty()) {
                    mConversationListLiveData.postValue(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}