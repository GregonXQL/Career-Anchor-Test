package com.careeranchor.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careeranchor.server.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
