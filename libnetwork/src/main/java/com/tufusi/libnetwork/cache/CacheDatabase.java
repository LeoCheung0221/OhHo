package com.tufusi.libnetwork.cache;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tufusi.libcommon.AppGlobal;

/**
 * Created by 鼠夏目 on 2020/9/24.
 *
 * @author 鼠夏目
 * @description 提供数据缓存能力库
 */
@Database(entities = {Cache.class}, version = 1, exportSchema = true)
public abstract class CacheDatabase extends RoomDatabase {

    private static final CacheDatabase database;

    static {
        // 创建一个内存数据库
        // 这种数据库的数据只会保存在内存中，即一旦应用进程被杀死，数据也就随之消失了
        // Room.inMemoryDatabaseBuilder()
        database = Room.databaseBuilder(AppGlobal.getsApplication(), CacheDatabase.class, "ohho_cache")
                // 是否允许在主线程中进行回调 默认false，如果不设置，在主线程中进行操作会报异常
                .allowMainThreadQueries()
                // 添加数据库的创建和打开操作的回调
                // .addCallback()
                // 设置查询的线程池
                // .setQueryExecutor()
                // 设置数据库工厂 不过不设置，默认使用 FrameworkSQLiteOpenHelperFactory
                // .openHelperFactory()
                // Room 日志模式设置
                // .setJournalMode()
                // 数据库升级异常后的进行回滚操作
                // .fallbackToDestructiveMigration()
                // 数据库升级异常后的根据指定版本进行回滚操作
                // .fallbackToDestructiveMigrationFrom()
                // 添加数据迁移操作
                // .addMigrations(CacheDatabase.sMigration)
                .build();
    }

    public abstract CacheDao getCache();

    public static CacheDatabase get() {
        return database;
    }

//    static Migration sMigration = new Migration(3, 5) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("alter table teacher rename to student");
//            database.execSQL("alter table teacher add column teacher_salary INTEGER NOT NULL default 5000");
//        }
//    };
}