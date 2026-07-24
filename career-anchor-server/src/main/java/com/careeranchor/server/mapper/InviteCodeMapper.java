package com.careeranchor.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.careeranchor.server.entity.InviteCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InviteCodeMapper extends BaseMapper<InviteCode> {
    @Update("""
            UPDATE invite_codes
            SET used_count = used_count + 1
            WHERE code = #{code}
              AND status = 'ACTIVE'
              AND retired_at IS NULL
              AND (expires_at IS NULL OR expires_at > NOW())
              AND used_count < max_uses
            """)
    int consumeAtomically(@Param("code") String code);

    @Update("""
            UPDATE invite_codes ic
            LEFT JOIN (
                SELECT invite_code_id, MAX(used_at) AS last_used_at
                FROM invite_code_usages
                GROUP BY invite_code_id
            ) usage_time ON usage_time.invite_code_id = ic.id
            SET ic.retired_at = NOW()
            WHERE ic.retired_at IS NULL
              AND (
                (ic.expires_at IS NOT NULL AND ic.expires_at <= #{cutoff})
                OR (
                    ic.used_count >= ic.max_uses
                    AND COALESCE(usage_time.last_used_at, ic.created_at) <= #{cutoff}
                )
              )
            """)
    int archiveExpiredAndExhausted(@Param("cutoff") java.time.LocalDateTime cutoff);
}
