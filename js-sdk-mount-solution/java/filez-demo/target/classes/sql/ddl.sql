-- SQLite database table structure
-- ----------------------------
-- Table structure for sys_user  
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  display_name VARCHAR(64) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  photo_url VARCHAR(255) DEFAULT NULL,
  name VARCHAR(64) DEFAULT NULL,
  password VARCHAR(255) DEFAULT NULL,
  job_title VARCHAR(64) DEFAULT NULL,
  org_name VARCHAR(128) DEFAULT NULL,
  org_id VARCHAR(64) DEFAULT NULL
);

-- ----------------------------
-- Table structure for doc_meta
-- ----------------------------
DROP TABLE IF EXISTS doc_meta;
CREATE TABLE doc_meta (
  id VARCHAR(255) NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  created_at DATETIME,
  modified_at DATETIME,
  size BIGINT,
  version VARCHAR(50),
  filepath VARCHAR(500),
  role VARCHAR(50),
  created_by_id VARCHAR(36),
  created_by_name VARCHAR(64),
  created_by_email VARCHAR(128),
  modified_by_id VARCHAR(36),
  modified_by_name VARCHAR(64),
  modified_by_email VARCHAR(128),
  owner_id VARCHAR(36),
  owner_name VARCHAR(64),
  owner_email VARCHAR(128),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------
-- Table structure for doc_control  
-- ----------------------------
DROP TABLE IF EXISTS doc_control;
CREATE TABLE doc_control (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id VARCHAR(36) NOT NULL,
  doc_id VARCHAR(255) NOT NULL,
  permissions_json TEXT,
  extension_json TEXT,
  watermark_json TEXT,
  role VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, doc_id)
);
