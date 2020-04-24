package com.guangzhida.xiaomai.model

import androidx.room.PrimaryKey
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity

data class NewFriendModel(val inviteMessageEntity: InviteMessageEntity, val userEntity: UserEntity)


fun generateList():MutableList<NewFriendModel> {
    val list = mutableListOf<NewFriendModel>()
    val model = NewFriendModel(
        InviteMessageEntity(
            id = 1242638517729759233,
            nickName = "侯浩然",
            headerUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            from = "19137629693",
            time = System.currentTimeMillis(),
            state = 0,
            reason = "",
            userName = ""
        ), UserEntity(
            uid = 1242638517729759233,
            nickName = "侯浩然",
            userName = "19137629693",
            avatarUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            age = "0"
        )
    )
    val model1 = NewFriendModel(
        InviteMessageEntity(
            id = 1242638517729759233,
            nickName = "侯浩然1",
            headerUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            from = "19137629693",
            time = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000),
            state = 2,
            reason = "",
            userName = ""
        ), UserEntity(
            uid = 1242638517729759233,
            nickName = "侯浩然1",
            userName = "19137629693",
            avatarUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            age = "0"
        )
    )
    val model2 = NewFriendModel(
        InviteMessageEntity(
            id = 1242638517729759233,
            nickName = "侯浩然2",
            headerUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            from = "19137629693",
            time = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000),
            state = 1,
            reason = "",
            userName = ""
        ), UserEntity(
            uid = 1242638517729759233,
            nickName = "侯浩然2",
            userName = "19137629693",
            avatarUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            age = "0"
        )
    )
    val model3 = NewFriendModel(
        InviteMessageEntity(
            id = 1242638517729759233,
            nickName = "侯浩然3",
            headerUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            from = "19137629693",
            time = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000),
            state = 2,
            reason = "",
            userName = ""
        ), UserEntity(
            uid = 1242638517729759233,
            nickName = "侯浩然3",
            userName = "19137629693",
            avatarUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            age = "0"
        )
    )
    val model4 = NewFriendModel(
        InviteMessageEntity(
            id = 1242638517729759233,
            nickName = "侯浩然4",
            headerUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            from = "19137629693",
            time = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000),
            state = 0,
            reason = "",
            userName = ""
        ), UserEntity(
            uid = 1242638517729759233,
            nickName = "侯浩然4",
            userName = "19137629693",
            avatarUrl = "/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg",
            age = "0"
        )
    )
    list.add(model)
//    list.add(model1)
//    list.add(model2)
//    list.add(model3)
//    list.add(model4)
    return list

}


// [ConversationModelWrap(emConversation=com.hyphenate.chat.EMConversation@99465b7f, userEntity=UserEntity(uid=1242638517729759233,
// nickName=侯浩然, userName=15225462583, avatarUrl=/pic/2020/4/11/164947rvff7v0j9zebdi7l.jpeg, age=20, sex=1, remarkName=uh, singUp=))]



