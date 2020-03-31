package com.guangzhida.xiaomai.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guangzhida.xiaomai.room.dao.UserDao
import com.guangzhida.xiaomai.room.entity.UserEntity


@Database(entities = [UserEntity::class],version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao?
}