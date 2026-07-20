package com.careeranchor.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careeranchor.server.entity.TestResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestResultMapper extends BaseMapper<TestResult> {
    @Select("""
            SELECT id, user_id, invite_code_id, scale_max, answers,
                   score_technical, score_managerial, score_autonomy, score_security,
                   score_creativity, score_service, score_challenge, score_lifestyle,
                   top1, top2, top3, created_at
            FROM test_results
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<TestResult> selectHistory(@Param("userId") long userId,
                                   @Param("offset") long offset,
                                   @Param("size") int size);

    @Select("SELECT COUNT(*) FROM test_results WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") long userId);
}
