package com.guangzhida.xiaomai.room.dao

import androidx.room.*
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity

@Dao
interface InviteMessageDao {

    @Query("SELECT * FROM InviteMessageEntity")
    fun queryAll(): List<InviteMessageEntity>


    /**
     * 查询当前好友下的所有消息列表
     */
    @Query("SELECT * FROM InviteMessageEntity WHERE userName == :userName")
    fun queryInviteMessageByUserName(userName: String): List<InviteMessageEntity>

    /**
     * 查询指定用户发送过来的
     */
    @Query("SELECT * FROM InviteMessageEntity WHERE `from` == :from limit 1")
    fun queryInviteMessageByFrom(from: String): InviteMessageEntity?

    /**
     * 插入消息列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: InviteMessageEntity?)

    /**
     * 更新消息列表
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg users: InviteMessageEntity?)

    /**
     * 删除消息列表
     */
    @Delete
    fun delete(vararg users: InviteMessageEntity?)
}