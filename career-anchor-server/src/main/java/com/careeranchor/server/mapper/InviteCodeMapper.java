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
              AND (expires_at IS NULL OR expires_at > NOW())
              AND used_count < max_uses
            """)
    int consumeAtomically(@Param("code") String code);
}
