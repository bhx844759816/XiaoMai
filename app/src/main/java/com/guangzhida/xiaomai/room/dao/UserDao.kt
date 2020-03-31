package com.guangzhida.xiaomai.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.guangzhida.xiaomai.room.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM UserEntity")
    fun queryAll(): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE userName = :userName LIMIT 1")
    fun queryUserByUserName(userName: String): UserEntity


}