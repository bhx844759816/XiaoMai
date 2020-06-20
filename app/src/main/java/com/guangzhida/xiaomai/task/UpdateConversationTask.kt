package com.guangzhida.xiaomai.task

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils

/**
 * 更新用户的本地会话对象
 * 在ChatHelper中接收到消息得时候，开启后台任务将这个人的信息保存到本地,通过调用接口查询用户
 * 信息将信息保存到ConversationEntity表中
 *
 * 因为接收到的消息有普通用户发送来的消息,和客服消息,所以需要从不同的接口去查询
 *
 *
 */
class UpdateConversationTask(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val mWorkerParameters = workerParameters
    private val mChatRepository = InjectorUtil.getChatRepository()
    //好友
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }
    // 会话的实例
    private val mConversationDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).conversationDao()
    }

    override fun doWork(): Result {
        //需要插入的Conversation
        try {
            val userName = mWorkerParameters.inputData.getString("userName")
            LogUtils.i("start UpdateConversationTask userName=$userName")
            userName?.let {
                var conversation = mConversationDao?.queryConversationByUserName(it)
                if (conversation == null) {
                    //查询本地好友
                    val userEntity = mUserDao?.queryUserByUserName(it)
                    LogUtils.i("userEntity=$userEntity")
                    if (userEntity != null) {
                        //通过本地好友对象获取会话实例
                        conversation = getConversationEntity(userEntity)
                    } else {
                        //查询服务端好友列表获取用户实例
                        conversation = getConversationEntityByUserTable(it)
                        if (conversation == null) {
                            conversation = getConversationEntityByServiceTable(it)
                        }
                    }
                    if (conversation != null) {
                        mConversationDao?.insert(conversation)
                    }
                }
            }
        } catch (t: Throwable) {
            LogUtils.i("start UpdateConversationTask error=${t.localizedMessage}")
            t.printStackTrace()
            return Result.failure()
        }
        LogUtils.i("start UpdateConversationTask finish")
        return Result.success()
    }

    /**
     * 通过本地保存的用户对象UserEntity获取会话的Entity
     */
    private fun getConversationEntity(userEntity: UserEntity): ConversationEntity {
        return ConversationEntity(
            userName = userEntity.userName,
            avatarUrl = userEntity.avatarUrl,
            nickName = userEntity.nickName,
            remarkName = userEntity.remarkName,
            sex = userEntity.sex,
            age = userEntity.age,
            type = 0,
            parentUserName = BaseApplication.instance().mUserModel?.username ?: ""
        )
    }

    /**
     * 获取用户信息从用户表
     */
    private fun getConversationEntityByUserTable(userName: String): ConversationEntity? {
        val call = mChatRepository.getUserInfoSync(userName)
        val response = call.execute()
        if (response.isSuccessful && response.body() != null) {
            val result = response.body()!!
            if (result.isSuccess() && result.data.isNotEmpty()) {
                val conversationEntity = ConversationEntity()
                //获取的用户对象
                val userModel = result.data[0]
                conversationEntity.age = userModel.age.toString()
                conversationEntity.avatarUrl = userModel.headUrl ?: ""
                conversationEntity.nickName = userModel.nickName
                conversationEntity.userName = userModel.mobilePhone
                conversationEntity.sex = userModel.sex.toString()
                return conversationEntity
            }

        }
        return null
    }

    /**
     * 获取用户信息从客服表
     */
    private fun getConversationEntityByServiceTable(userName: String): ConversationEntity? {


        return null
    }


}