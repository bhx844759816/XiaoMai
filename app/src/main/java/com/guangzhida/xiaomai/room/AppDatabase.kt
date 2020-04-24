package com.guangzhida.xiaomai.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.guangzhida.xiaomai.room.dao.ConversationDao
import com.guangzhida.xiaomai.room.dao.InviteMessageDao
import com.guangzhida.xiaomai.room.dao.UserDao
import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity


@Database(
    entities = [UserEntity::class, InviteMessageEntity::class, ConversationEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao?
    abstract fun inviteMessageDao(): InviteMessageDao?
    abstract fun conversationDao(): ConversationDao?

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "xiaomai.db"
        )
            .allowMainThreadQueries()
            .build()
    }
}