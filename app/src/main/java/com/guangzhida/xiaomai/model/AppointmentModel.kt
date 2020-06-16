package com.guangzhida.xiaomai.model

data class AppointmentModel(
    val id: Long,
    val schoolId: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val signEndTime: Long,
    val activityStartTime: Long,
    val activityAddress: String,
    val activityPic: String,
    val activityMoney: Double,
    val boyCount: Int,
    val girlCount: Int,
    val feeType: Int,
    val isExpire: Int,
    val isSign: Int,
    val count: Int,
    val examineTime: Long,
    @Transient
    var isChecked: Boolean,//不进行序列化 是否选中
    @Transient
    var isEdit: Boolean//是否可编辑
)


//{"id":"1270962418546909186","schoolId":"1188013109260849154",
//    "title":"吃急急急","content":"李诺破诺克","signStartTime":null,
//    "signEndTime":1592115180000,"activityStartTime":1593065580000,
//    "activityEndTime":null,"userId":"1258573997231050754",
//    "collectionAddress":null,
//    "activityAddress":"ing摸头欧诺",
//    "activityPic":",, ,, ,, ,, ,",
//    "activityMoney":6.0,"boyCount":3,"girlCount":3,"releaseStatus":null,"releaseTime":null,
//    "createTime":null,"createUser":null,"examineStatus":null,
//    "examineTime":null,"isShow":null,"advanceCancelTime":null,"isStopSign":0,"isDelete":null,
//    "updateUser":null,"updateTime":null,
//    "note":null,"attr1":null,"attr2":null,"attr3":null,"feeType":null,"isExpire":0,"isSign":0,"count":null}