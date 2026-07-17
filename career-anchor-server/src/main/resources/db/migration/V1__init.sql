CREATE TABLE users (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  openid        VARCHAR(64)  NOT NULL COMMENT '微信 openid',
  nickname      VARCHAR(64)  DEFAULT NULL,
  avatar_url    VARCHAR(512) DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login_at DATETIME     DEFAULT NULL,
  UNIQUE KEY uk_openid (openid)
) ENGINE=InnoDB COMMENT='微信用户';

CREATE TABLE admin_accounts (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(32)  NOT NULL DEFAULT 'admin',
  password_hash VARCHAR(100) NOT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='管理员';

CREATE TABLE invite_codes (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code        VARCHAR(16)  NOT NULL COMMENT '码值，如 8 位大写字母数字',
  max_uses    INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '可用总次数',
  used_count  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已用次数',
  expires_at  DATETIME     DEFAULT NULL COMMENT 'NULL=永不过期',
  status      ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  remark      VARCHAR(128) DEFAULT NULL,
  created_by  BIGINT UNSIGNED DEFAULT NULL COMMENT '生成者 admin_accounts.id',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code),
  KEY idx_status (status)
) ENGINE=InnoDB COMMENT='邀请码';

CREATE TABLE invite_code_usages (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  invite_code_id BIGINT UNSIGNED NOT NULL,
  user_id        BIGINT UNSIGNED NOT NULL,
  used_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_code (invite_code_id),
  KEY idx_user (user_id)
) ENGINE=InnoDB COMMENT='邀请码使用记录';

CREATE TABLE questions (
  id          INT UNSIGNED PRIMARY KEY COMMENT '即题号 1-40',
  content     VARCHAR(512) NOT NULL COMMENT '题干',
  anchor_code VARCHAR(20)  NOT NULL COMMENT '所属职业锚编码',
  enabled     TINYINT(1)   NOT NULL DEFAULT 1,
  KEY idx_anchor (anchor_code)
) ENGINE=InnoDB COMMENT='题库';

CREATE TABLE anchor_profiles (
  anchor_code VARCHAR(20) PRIMARY KEY,
  name_cn     VARCHAR(32)  NOT NULL,
  name_en     VARCHAR(64)  NOT NULL,
  tagline     VARCHAR(128) NOT NULL COMMENT '一句话定位',
  traits      JSON NOT NULL COMMENT '特点描述',
  strengths   JSON NOT NULL COMMENT '职场优势',
  risks       JSON NOT NULL COMMENT '潜在挑战',
  advices     JSON NOT NULL COMMENT '发展建议',
  careers     JSON NOT NULL COMMENT '适合职业'
) ENGINE=InnoDB COMMENT='职业锚解析文案';

CREATE TABLE test_results (
  id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT UNSIGNED NOT NULL,
  invite_code_id    BIGINT UNSIGNED NOT NULL,
  scale_max         TINYINT UNSIGNED NOT NULL COMMENT '本次测评量表上限',
  answers           JSON NOT NULL COMMENT '原始作答',
  score_technical   TINYINT UNSIGNED NOT NULL,
  score_managerial  TINYINT UNSIGNED NOT NULL,
  score_autonomy    TINYINT UNSIGNED NOT NULL,
  score_security    TINYINT UNSIGNED NOT NULL,
  score_creativity  TINYINT UNSIGNED NOT NULL,
  score_service     TINYINT UNSIGNED NOT NULL,
  score_challenge   TINYINT UNSIGNED NOT NULL,
  score_lifestyle   TINYINT UNSIGNED NOT NULL,
  top1              VARCHAR(20) NOT NULL,
  top2              VARCHAR(20) NOT NULL,
  top3              VARCHAR(20) NOT NULL,
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user_time (user_id, created_at DESC),
  KEY idx_time (created_at DESC),
  CONSTRAINT fk_result_user   FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_result_invite FOREIGN KEY (invite_code_id) REFERENCES invite_codes(id)
) ENGINE=InnoDB COMMENT='测评结果';
