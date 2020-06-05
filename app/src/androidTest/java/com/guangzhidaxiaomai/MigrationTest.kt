package com.guangzhidaxiaomai

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.MIGRATION_1_2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MigrationTest() {
    private val TEST_DB = "migration-test"
    @Rule
    var helper: MigrationTestHelper? = null

    init {
        helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @Test
    public fun migrate1To2() {
        var db = helper?.createDatabase(TEST_DB, 1)
        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db?.execSQL("")
        // Prepare for the next version.
        db?.close();
        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper?.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }
}