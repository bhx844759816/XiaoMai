package com.guangzhida.xiaomai.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.guangzhida.xiaomai.room.entity.UserEntity


@Dao
interface UserDao {

    @Query("SELECT * FROM UserEntity")
    fun queryAll(): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE userName == :userName")
    fun queryUserByUserName(userName: String): UserEntity?

    @Query("SELECT * FROM UserEntity WHERE nickName LIKE '%' || :key || '%'")
    fun queryUserByLike(key: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: UserEntity?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg users: UserEntity?)

    @Delete
    fun delete(vararg users: UserEntity?)


}