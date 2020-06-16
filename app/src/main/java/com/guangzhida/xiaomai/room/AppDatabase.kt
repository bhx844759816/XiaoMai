package com.guangzhida.xiaomai.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.guangzhida.xiaomai.room.dao.ConversationDao
import com.guangzhida.xiaomai.room.dao.InviteMessageDao
import com.guangzhida.xiaomai.room.dao.UserDao
import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.guangzhida.xiaomai.room.entity.InviteMessageEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.utils.LogUtils


@Database(
    entities = [UserEntity::class, InviteMessageEntity::class, ConversationEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao? //好友列表
    abstract fun inviteMessageDao(): InviteMessageDao? //好友申请
    abstract fun conversationDao(): ConversationDao?//会话
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()


        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }


        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "xiaomai.db")
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

}

//新增客服表
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        LogUtils.i("db update MIGRATION_1_2")
        //会话表添加个字段
        database.execSQL("ALTER TABLE ConversationEntity add type INTEGER NOT NULL DEFAULT 0")
//        database.execSQL(
//            "CREATE TABLE IF NOT EXISTS ServiceEntity (" +
//                    "id TEXT PRIMARY KEY NOT NULL, " +
//                    "age INTEGER NOT NULL, " +
//                    "headId TEXT NOT NULL, " +
//                    "headUrl TEXT NOT NULL, " +
//                    "mobilePhone TEXT NOT NULL, " +
//                    "nickName TEXT NOT NULL, " +
//                    "userName TEXT NOT NULL, " +
//                    "schoolName TEXT NOT NULL, " +
//                    "schoolId TEXT NOT NULL" +
//                    ")"
//        );
    }
}
