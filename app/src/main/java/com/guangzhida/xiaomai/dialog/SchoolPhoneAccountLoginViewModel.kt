package com.guangzhida.xiaomai.dialog

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.Preference
import com.guangzhida.xiaomai.utils.ToastUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SchoolPhoneAccountLoginViewModel : BaseViewModel() {
    private val loginRepository = InjectorUtil.getLoginRepository()
    private val chatRepository = InjectorUtil.getChatRepository()
    //用户对象
    private var mUserGson by Preference(Preference.USER_GSON, "")
    val mSmsCodeLiveData = MutableLiveData<Boolean>()
    val mUserExistObserver = MutableLiveData<Boolean>()
    val mBindSchoolAccountLoginResult = MutableLiveData<Boolean>()
    val loginSuccessObserver = MutableLiveData<Boolean>() //登录结果回调
    private val mUserDao by lazy {
        AppDatabase.invoke(BaseApplication.instance().applicationContext).userDao()
    }

    /**
     * 发送验证码
     */
    fun sendSmsCode(phone: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val result = withContext(Dispatchers.IO) {
                    loginRepository.getUserByPhone(phone)
                }
                if (result.isSuccess()) {
                    mUserExistObserver.postValue(true)
                } else {
                    val schoolModelWrap = withContext(Dispatchers.IO) {
                        loginRepository.sendSmsCode(phone, "1")
                    }
                    if (schoolModelWrap.status == 200) {
                        //获取验证码成功
                        mSmsCodeLiveData.postValue(true)
                    } else {
                        defUI.toastEvent.postValue(schoolModelWrap.message)
                        mSmsCodeLiveData.postValue(false)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                defUI.dismissDialog.call()
            }
        }

    }

    /**
     * 注册和登录通过校园卡账号
     */
    fun registerAndLoginBySchoolAccount(
        phone: String,
        password: String,
        smsCode: String,
        schoolAccount: String,
        schoolPassword: String
    ) {
        launchUI {
            try {
                defUI.showDialog.call()
                val result = withContext(Dispatchers.IO) {
                    loginRepository.getUserByPhone(phone)
                }
                if (result.isSuccess()) {
                    mUserExistObserver.postValue(true)
                } else {
                    val params = mapOf(
                        "mobilePhone" to phone,
                        "code" to smsCode,
                        "password" to password,
                        "campusNetworkNum" to schoolAccount,
                        "campusNetworkPwd" to schoolPassword
                    )
                    val registerResult = withContext(Dispatchers.IO) {
                        loginRepository.register(params)
                    }
                    if (registerResult.isSuccess()) {
                        val loginResult = withContext(Dispatchers.IO) {
                            val loginResult = loginRepository.login(phone, password)
                            //保存登录用户信息
                            if (loginResult.status == 200 && loginResult.data != null) {
                                BaseApplication.instance().mUserModel = loginResult.data
                                mUserGson = Gson().toJson(loginResult.data)
                                //拉取好友到本地
                                loadServiceContactsList()
                            } else {
                                defUI.toastEvent.postValue(loginResult.message ?: "绑定手机号失败")
                            }
                            loginResult.status == 200
                        }
                        if (loginResult) {
                            doChatLogin(phone, password)
                        } else {
                            mBindSchoolAccountLoginResult.postValue(false)
                        }
                    } else {
                        defUI.toastEvent.postValue(if (registerResult.message.isEmpty()) "绑定手机号失败" else registerResult.message)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                defUI.dismissDialog.call()
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
                val list = result.data.map { chatUserModel ->
                    val localUserEntity = userEntityList?.find {
                        it.uid == chatUserModel.id.toLong()
                    }
                    UserEntity(
                        uid = chatUserModel.id.toLong(),
                        nickName = chatUserModel.nickName,
                        userName = chatUserModel.mobilePhone,
                        avatarUrl = chatUserModel.headUrl ?: "",
                        age = chatUserModel.age.toString(),
                        sex = chatUserModel.sex.toString(),
                        singUp = chatUserModel.signature ?: "",
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
                mBindSchoolAccountLoginResult.postValue(true)
            }

            override fun onProgress(progress: Int, status: String?) {
            }

            override fun onError(code: Int, error: String?) {
                mBindSchoolAccountLoginResult.postValue(false)
            }
        })
    }

}