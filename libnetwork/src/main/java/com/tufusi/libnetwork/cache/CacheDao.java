package com.tufusi.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Created by 鼠夏目 on 2020/9/24.
 *
 * @author 鼠夏目
 * @description 真正的数据访问对象
 */
@Dao
public interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Cache cache);

    /**
     * 只可以传递对象，删除时根据 Cache 中的主键 进行比对
     */
    @Delete
    int delete(Cache cache);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Cache cache);

    /**
     * 注意：冒号后面必须紧跟参数名，中间不能有空格。大于小于号和冒号中间是有空格的。
     * select * from cache where 【表中列名】=:【参数名】 ------> 等于
     * where 【表中列名】<:【参数名】 ------> 小于
     * where 【表中列名】between:【参数名1】 and:【参数名2】 ------> 介于区间
     * where 【表中列名】like:【参数名】 ------> 模糊查询
     * where 【表中列名】in(:【参数名集合】) ------> 查询符合集合内指定字段值的记录
     *
     * @param key 查询键值
     * @return 返回查询对象  如果是一对多，这里可以返回 List<Cache>
     */
    @Query("select * from cache where `key`=:key")
    Cache queryCache(String key);

} 