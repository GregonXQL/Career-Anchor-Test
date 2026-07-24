ALTER TABLE invite_codes
  ADD COLUMN retired_at DATETIME DEFAULT NULL COMMENT '用尽或过期满 24 小时后的归档时间',
  ADD KEY idx_invite_retired (retired_at);
