package com.gratitude.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gratitude.app.entity.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
