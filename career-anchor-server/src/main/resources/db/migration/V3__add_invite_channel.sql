ALTER TABLE invite_codes
  ADD COLUMN channel ENUM('MANUAL','QR') NOT NULL DEFAULT 'MANUAL'
  COMMENT '分发方式：手动输入码 / 二维码邀请'
  AFTER status;
