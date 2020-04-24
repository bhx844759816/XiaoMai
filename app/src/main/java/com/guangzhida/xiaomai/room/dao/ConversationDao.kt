package com.guangzhida.xiaomai.room.dao

import androidx.room.*
import com.guangzhida.xiaomai.room.entity.ConversationEntity

@Dao
interface ConversationDao {

    @Query("SELECT * FROM ConversationEntity")
    fun queryAll(): List<ConversationEntity>?

    @Query("SELECT * FROM ConversationEntity WHERE userName == :userName limit 1")
    fun queryConversationByUserName(userName: String): ConversationEntity?

    @Query("SELECT * FROM ConversationEntity WHERE parentUserName == :parentUserName ")
    fun queryConversationByParentUserName(parentUserName: String): List<ConversationEntity>?

    /**
     * 插入消息列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: ConversationEntity?)

    /**
     * 更新消息列表
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg users: ConversationEntity?)

    /**
     * 删除消息列表
     */
    @Delete
    fun delete(vararg users: ConversationEntity?)
}