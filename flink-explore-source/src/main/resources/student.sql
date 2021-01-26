DROP TABLE IF EXISTS student;
CREATE TABLE student (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  name varchar(25) COLLATE utf8_bin DEFAULT NULL,
  password varchar(25) COLLATE utf8_bin DEFAULT NULL,
  age int(10) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



INSERT INTO student VALUES
    ('1', 'test01', '123456', '18')
  , ('2', 'test02', '123', '17')
  , ('3', 'test03', '1234', '18')
  , ('4', 'test04', '12345', '16')
;
COMMIT;
