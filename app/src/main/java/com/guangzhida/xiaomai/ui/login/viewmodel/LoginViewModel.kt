package com.guangzhida.xiaomai.ui.login.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.SERVICE_USERNAME
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.Preference
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class LoginViewModel : BaseViewModel() {
    private var mUserGson by Preference(Preference.USER_GSON, "") //用户对象
    private var mServiceGson by Preference(Preference.SERVICE_GSON, "") //客服对象
    private val loginRepository = InjectorUtil.getLoginRepository()
    private val chatRepository = InjectorUtil.getChatRepository()
    val mLoginResult = MutableLiveData<Boolean>()
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }

    /**
     * 登录
     * 1.调用登录接口 2.保存客服信息到本地 3.调用环信的登录接口
     */
    fun doLogin(phone: String, password: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val loginResult = withContext(Dispatchers.IO) {
                    val loginResult = loginRepository.login(phone, password)
                    //保存登录用户信息
                    if (loginResult.status == 200 && loginResult.data != null) {
                        BaseApplication.instance().mUserModel = loginResult.data
                        mUserGson = Gson().toJson(loginResult.data)
                        //保存客服信息
                        val serviceResult = loginRepository.getUseInfoByUserName(SERVICE_USERNAME)
                        if (serviceResult.isSuccess()) {
                            val serviceModel = serviceResult.result[0]
                            BaseApplication.instance().mServiceModel = serviceModel
                            mServiceGson = Gson().toJson(serviceModel)
                        }
                        //拉取好友到本地
                        loadServiceContactsList()
                    } else {
                        defUI.toastEvent.postValue(loginResult.message ?: "登录失败")
                    }
                    loginResult.status == 200
                }
                if (loginResult) {
                    doChatLogin(phone, password)
                } else {
                    mLoginResult.postValue(false)
                    defUI.dismissDialog.call()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                defUI.dismissDialog.call()
                defUI.toastEvent.postValue("登录失败")
            }
        }
    }

    /**
     * 加载后台服务
     */
    private suspend fun loadServiceContactsList() {
        return withContext(Dispatchers.IO) {
            val result = chatRepository.getFriendList()
            val userEntityList = mUserDao?.queryAll()
            if (result.isSuccess()) {
                val list = result.result.map { chatUserModel ->
                    val localUserEntity = userEntityList?.find {
                        it.uid == chatUserModel.id.toLong()
                    }
                    UserEntity(
                        uid = chatUserModel.id.toLong(),
                        nickName = chatUserModel.nickName,
                        userName = chatUserModel.mobilePhone,
                        avatarUrl = chatUserModel.headUrl?:"",
                        age = chatUserModel.age.toString(),
                        sex = chatUserModel.sex.toString(),
                        singUp = chatUserModel.signature?: "",
                        remarkName = localUserEntity?.remarkName ?: ""
                    )
                }
                //删除服务器未存在的本地好友信息
                userEntityList?.forEach {
                    mUserDao?.delete(it)
                }
                list.forEach {
                    mUserDao?.insert(it)
                }
            }
        }
    }

    /**
     * 登录环信
     */
    private fun doChatLogin(phone: String, password: String) {
        //首先退出登录
        EMClient.getInstance().logout(false)
        //然后登录
        EMClient.getInstance().login(phone, password, object : EMCallBack {
            override fun onSuccess() {
                //加载全部会话
                EMClient.getInstance().chatManager().loadAllConversations()
                defUI.toastEvent.postValue("登录成功")
                mLoginResult.postValue(true)
            }

            override fun onProgress(progress: Int, status: String?) {
            }

            override fun onError(code: Int, error: String?) {
                defUI.toastEvent.postValue("登录失败:$error")
                mLoginResult.postValue(false)
            }
        })
    }
}