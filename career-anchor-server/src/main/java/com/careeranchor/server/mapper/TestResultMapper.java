package com.careeranchor.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careeranchor.server.entity.TestResult;
import com.careeranchor.server.dto.AdminResultSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.time.LocalDateTime;

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

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM test_results tr
            JOIN users u ON u.id = tr.user_id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
              AND (u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.openid LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="from != null">AND tr.created_at &gt;= #{from}</if>
            <if test="toExclusive != null">AND tr.created_at &lt; #{toExclusive}</if>
            </script>
            """)
    long countAdminResults(@Param("keyword") String keyword,
                           @Param("from") LocalDateTime from,
                           @Param("toExclusive") LocalDateTime toExclusive);

    @Select("""
            <script>
            SELECT tr.id, tr.user_id AS userId,
                   COALESCE(NULLIF(u.nickname, ''), '微信用户') AS nickname,
                   u.avatar_url AS avatarUrl, RIGHT(u.openid, 6) AS openidSuffix,
                   DATE_FORMAT(tr.created_at, '%Y-%m-%d %H:%i:%s') AS createdAt,
                   tr.top1, tr.top2, tr.top3
            FROM test_results tr
            JOIN users u ON u.id = tr.user_id
            WHERE 1 = 1
            <if test="keyword != null and keyword != ''">
              AND (u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.openid LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="from != null">AND tr.created_at &gt;= #{from}</if>
            <if test="toExclusive != null">AND tr.created_at &lt; #{toExclusive}</if>
            ORDER BY tr.created_at DESC, tr.id DESC
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<AdminResultSummary> selectAdminResults(@Param("keyword") String keyword,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("toExclusive") LocalDateTime toExclusive,
                                                 @Param("offset") long offset,
                                                 @Param("size") int size);

    @Select("SELECT COUNT(*) FROM test_results WHERE created_at >= #{from} AND created_at < #{to}")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
