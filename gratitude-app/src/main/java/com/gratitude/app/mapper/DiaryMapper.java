package com.gratitude.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 日记 Mapper
 */
@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {

    /**
     * 广场分页查询（含作者信息）
     * @param page     分页参数
     * @param sortType 排序类型 1最新 2最多点赞 3最热评论
     * @param keyword  关键词搜索（可为null）
     */
    IPage<Diary> selectSquarePage(
            Page<?> page,
            @Param("sortType") Integer sortType,
            @Param("keyword") String keyword
    );
}
