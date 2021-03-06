CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT NOT NULL,
  uuid          VARCHAR(36)           NOT NULL UNIQUE,
  username      VARCHAR(255),
  email_address VARCHAR(255)          NOT NULL,
  token         VARCHAR(36),
  password      VARCHAR(255),
  company_id    BIGINT,
  created_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE UNIQUE INDEX users_idx01
  ON users (email_address);

CREATE TABLE IF NOT EXISTS companies (
  id   BIGINT AUTO_INCREMENT NOT NULL,
  company_name VARCHAR(255),
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS roles (
  id   BIGINT AUTO_INCREMENT NOT NULL,
  name VARCHAR(36) UNIQUE,
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS permissions (
  id   BIGINT AUTO_INCREMENT NOT NULL,
  name VARCHAR(36) UNICODE,
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS role_2_permission (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
CREATE UNIQUE INDEX role_2_permission_idx01
  ON role_2_permission (role_id, permission_id);

CREATE TABLE IF NOT EXISTS user_2_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE UNIQUE INDEX user_2_role_idx01
  ON user_2_role (user_id, role_id);

INSERT INTO roles (name) VALUES ('USER');
INSERT INTO roles (name) VALUES ('AGENT');
INSERT INTO roles (name) VALUES ('ADMIN');

INSERT INTO permissions (name) VALUES ('ACCOUNT_READ');
INSERT INTO permissions (name) VALUES ('ACCOUNT_UPDATE');
INSERT INTO permissions (name) VALUES ('ACCOUNT_ADMIN');
INSERT INTO permissions (name) VALUES ('ORDER_CREATE');
INSERT INTO permissions (name) VALUES ('ORDER_UPDATE');
INSERT INTO permissions (name) VALUES ('ORDER_READ');


# USER PERMISSION
INSERT INTO role_2_permission VALUES (1, 1);
INSERT INTO role_2_permission VALUES (1, 2);
INSERT INTO role_2_permission VALUES (1, 4);
INSERT INTO role_2_permission VALUES (1, 6);

# AGENT PERMISSION
INSERT INTO role_2_permission VALUES (2, 1);
INSERT INTO role_2_permission VALUES (2, 2);
INSERT INTO role_2_permission VALUES (2, 4);
INSERT INTO role_2_permission VALUES (2, 5);
INSERT INTO role_2_permission VALUES (2, 6);

# ADMIN PERMISSION
INSERT INTO role_2_permission VALUES (3, 1);
INSERT INTO role_2_permission VALUES (3, 2);
INSERT INTO role_2_permission VALUES (3, 3);
INSERT INTO role_2_permission VALUES (3, 4);
INSERT INTO role_2_permission VALUES (3, 5);
INSERT INTO role_2_permission VALUES (3, 6);
