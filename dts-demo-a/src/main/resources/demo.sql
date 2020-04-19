CREATE TABLE t_user (
  id int NOT NULL AUTO_INCREMENT,
  username varchar(50),
  password varchar(50),
  create_time datetime,
  PRIMARY KEY (id)
);
CREATE TABLE t_user_account (
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  total int,
  PRIMARY KEY (id)
);
CREATE TABLE t_user_ext (
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  nickname varchar(50),
  gendar tinyint,
  PRIMARY KEY (id)
);
CREATE TABLE t_user_log (
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  log varchar(200),
  PRIMARY KEY (id)
);
