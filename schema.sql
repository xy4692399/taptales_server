-- TapTales Schema - Run CREATE DATABASE first, then this file
-- Step 1: CREATE DATABASE tap_tales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci; USE tap_tales;

CREATE TABLE admin_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO admin_user (username, password, real_name) VALUES ('admin', '0192023a7bbd73250516f069df18b500', 'admin');

CREATE TABLE usr (
  id BIGINT NOT NULL AUTO_INCREMENT,
  openid VARCHAR(64) NOT NULL,
  union_id VARCHAR(64),
  nickname VARCHAR(100),
  avatar_url VARCHAR(500),
  phone VARCHAR(20),
  total_drunk INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE shop_owner (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50),
  phone VARCHAR(20),
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO shop_owner (username, password, real_name) VALUES ('owner', '5be057accb25758101fa5eadbbd79503', 'test owner');

CREATE TABLE shop (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(200),
  longitude DECIMAL(10,7),
  latitude DECIMAL(10,7),
  phone VARCHAR(20),
  logo_url VARCHAR(500),
  description TEXT,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_owner_id (owner_id),
  CONSTRAINT fk_shop_owner FOREIGN KEY (owner_id) REFERENCES shop_owner(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE drink_menu (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_shop_id (shop_id),
  CONSTRAINT fk_menu_shop FOREIGN KEY (shop_id) REFERENCES shop(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE drink (
  id BIGINT NOT NULL AUTO_INCREMENT,
  menu_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  image_url VARCHAR(500),
  size VARCHAR(20),
  price DECIMAL(10,2) NOT NULL,
  category TINYINT NOT NULL,
  description TEXT,
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_menu_id (menu_id),
  CONSTRAINT fk_drink_menu FOREIGN KEY (menu_id) REFERENCES drink_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promo_activity (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  cover_image VARCHAR(500),
  description TEXT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  total_tickets INT NOT NULL,
  remaining_tickets INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  status TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_shop_id (shop_id),
  KEY idx_status (status),
  CONSTRAINT fk_activity_shop FOREIGN KEY (shop_id) REFERENCES shop(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promo_activity_drink (
  id BIGINT NOT NULL AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  drink_id BIGINT NOT NULL,
  count INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  KEY idx_activity_id (activity_id),
  KEY idx_drink_id (drink_id),
  CONSTRAINT fk_pad_activity FOREIGN KEY (activity_id) REFERENCES promo_activity(id) ON DELETE CASCADE,
  CONSTRAINT fk_pad_drink FOREIGN KEY (drink_id) REFERENCES drink(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE promo_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  total_count INT NOT NULL,
  status TINYINT DEFAULT 0,
  paid_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_user_id (user_id),
  KEY idx_shop_id (shop_id),
  KEY idx_status (status),
  CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES usr(id),
  CONSTRAINT fk_order_activity FOREIGN KEY (activity_id) REFERENCES promo_activity(id),
  CONSTRAINT fk_order_shop FOREIGN KEY (shop_id) REFERENCES shop(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE redemption (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  drink_id BIGINT,
  count INT NOT NULL DEFAULT 1,
  redeemed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_order_id (order_id),
  KEY idx_user_id (user_id),
  KEY idx_shop_id (shop_id),
  CONSTRAINT fk_redemption_order FOREIGN KEY (order_id) REFERENCES promo_order(id),
  CONSTRAINT fk_redemption_user FOREIGN KEY (user_id) REFERENCES usr(id),
  CONSTRAINT fk_redemption_shop FOREIGN KEY (shop_id) REFERENCES shop(id),
  CONSTRAINT fk_redemption_drink FOREIGN KEY (drink_id) REFERENCES drink(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;