# Flink 实时数仓Demo

## 使用方法

1. 拉起 docker-compose 集群

```bash
# 重置环境
docker-compose down -v
rm -rf logs

# 启动集群
docker-compose -f docker-compose-flink.yaml up -d
docker-compose -f docker-compose-mysql.yaml up -d
```

在集群启动后，可以通过 localhost:8081 查看 flink dashboard。

2. 注册任务环境

在 MySQL 和 TiDB 中注册好表

```bash
docker exec -it flink-mysql mysql -uroot -p

# DROP DATABASE IF EXISTS test;
# CREATE DATABASE test; 
USE test;

create table src_base (
  base_id int primary key,
  base_location varchar(20)
);
create table src_stuff(
  stuff_id int primary key,
  stuff_base_id int,
  stuff_name varchar(20)
);

# 通过flink sink写入数据
create table app_wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);

docker exec -it flink-mysql mysql -htidb -uroot -P4000

use test;
create table app_wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);
```


3. 通过 Flink SQL Client 编写作业

```bash
docker exec -it flink-jobmanager ./bin/sql-client.sh embedded -l ./connector-lib
```

注册 Flink 表

```sql
-- drop table if exists base;

create table src_base (
    base_id int primary key,
    base_location varchar(20)
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql',
    'port' = '3306',
    'username' = 'root',
    'password' = 'mysql',
    'database-name' = 'test',
    'table-name' = 'src_base'
);

-- drop table if exists stuff;

create table src_stuff(
    stuff_id int primary key,
    stuff_base_id int,
    stuff_name varchar(20)
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql',
    'port' = '3306',
    'username' = 'root',
    'password' = 'mysql',
    'database-name' = 'test',
    'table-name' = 'src_stuff'
); 

-- drop table if exists wide_stuff;

create table mysql_app_wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
	'connector'  = 'jdbc',
    'driver'     = 'com.mysql.cj.jdbc.Driver',
    'url'        = 'jdbc:mysql://mysql:3306/test?rewriteBatchedStatements=true',
    'table-name' = 'app_wide_stuff',
    'username'   = 'root',
    'password'   = 'mysql'
);

create table tidb_app_wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
    'connector'  = 'jdbc',
    'driver'     = 'com.mysql.cj.jdbc.Driver',
    'url'        = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'app_wide_stuff',
    'username'   = 'root',
    'password'   = ''
);

create table print_base WITH ('connector' = 'print') LIKE src_base (EXCLUDING ALL);

create table print_stuff WITH ('connector' = 'print') LIKE src_stuff (EXCLUDING ALL);

create table print_wide_stuff WITH ('connector' = 'print') LIKE mysql_app_wide_stuff (EXCLUDING ALL);
```

提交作业到 Flink 集群

```sql
insert into mysql_app_wide_stuff
-- insert into tidb_app_wide_stuff
select s.stuff_id
    , b.base_id
    , b.base_location
    , s.stuff_name
from src_stuff as s
inner join src_base as b on s.stuff_base_id = b.base_id
;

insert into print_base select * from src_base;

insert into print_stuff select * from src_stuff;

insert into print_wide_stuff
select s.stuff_id
    , b.base_id
    , b.base_location
    , s.stuff_name
from src_stuff as s
inner join src_base as b on s.stuff_base_id = b.base_id
;
```

此时可以在 localhost:8081 看到一共注册了 4 个任务，分别是：
- 将 mysql 中 base 表的修改打印在标准输出中。
- 将 mysql 中 stuff 表的修改打印标准输出中。
- 将 base 表和 stuff 表 join 成 wide_stuff 表，将 wide_stuff 表的修改打印在标准输出中。
- 将 wide_stuff 表的修改写入到 tidb 中。

标准输出可以在本目录下执行 `docker-compose logs -f taskmanager` 持续查看。

4. 在 MySQL 中写入数据，进行测试

```bash
docker exec -it flink-mysql mysql -uroot

use test;
insert into src_base values (1, 'bj');
insert into src_stuff values (1, 1, 'zhangsan');
insert into src_stuff values (2, 1, 'lisi');
insert into src_base values (2, 'sh');
insert into src_stuff values (3, 2, 'wangliu');
update src_stuff set stuff_name = 'wangwu' where stuff_id = 3;
delete from src_stuff where stuff_name = 'lisi';
```

此时可以在标准输出中看到对应的变化：

```bash
taskmanager_1   | +I(1,bj)
taskmanager_1   | +I(1,1,zhangsan)
taskmanager_1   | +I(2,sh)
taskmanager_1   | +I(2,1,lisi)
taskmanager_1   | +I(3,2,wangliu)
taskmanager_1   | -U(3,2,wangliu)
taskmanager_1   | +U(3,2,wangwu)
taskmanager_1   | +I(1,1,bj,zhangsan)
taskmanager_1   | +I(2,1,bj,lisi)
taskmanager_1   | +I(3,2,sh,wangliu)
taskmanager_1   | -U(3,2,sh,wangliu)
taskmanager_1   | +U(3,2,sh,wangwu)
taskmanager_1   | -D(2,1,lisi)
taskmanager_1   | -D(2,1,bj,lisi)
```

也可以在 TiDB 中查看结果：

```bash
docker exec -it flink-mysql mysql -uroot -p -e "select * from test.app_wide_stuff;";

+----------+---------+---------------+------------+
| stuff_id | base_id | base_location | stuff_name |
+----------+---------+---------------+------------+
|        1 |       1 | bj            | zhangsan   |
|        3 |       2 | sh            | wangwu     |
+----------+---------+---------------+------------+
```

## 注意点

1. Flink 需要内存较大，请将 docker-compose 集群可用的内存调大，建议 6G 及以上。
2. Flink SQL Client 设计为交互式执行，目前不支持一次输入多条语句，一个可用的替代方案是 apache zeppelin。
3. 可以使用如下命令测试 Kafka 是否接收到了数据

```bash
docker-compose -f docker-compose-kafka.yaml up -d
docker exec -it flink-kafka /opt/kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181  
docker exec -it flink-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 \
--topic test.base --from-beginning

docker-compose down -v && \
rm -rf ./logs && \
find ./config/canal-config -name "meta.dat"|xargs rm -f && \
docker-compose up -d
```

TODO: 增加一些例子，比如 mysql 中异步读取维表、使用 flink 进行数据异构。


## Demo1: Datagen to Print

```sql
create table `source`(`a` int) with ('connector' = 'datagen', 'rows-per-second'='1');
create table `sink`(`a` int) with ('connector' = 'print');
insert into `sink` select * from `source`;
```

## Demo2: Datagen to MySQL

```bash
docker exec -it flink-mysql mysql -uroot -p # mysql
docker exec -it flink-mysql mysql -uroot -htidb -P4000 # tidb
docker exec -it flink-jobmanager ./bin/sql-client.sh embedded -l ./connector-lib
```

在 mysql 中执行
```sql
create database if not exists test; 

use test;
drop table if exists username;
create table username (
    id int primary key,
    name varchar(10),
    create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
```

在 tidb 中执行
```sql
create database if not exists test; use test;
drop table if exists username;
create table username (
    id int primary key,
    name varchar(10),
    mysql_create_time timestamp(6) NULL,
    tidb_create_time timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
```

flink sql 中执行

```sql
create table source (
    id int primary key,
    name varchar(10),
    create_time timestamp(6)
) with (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 
    'port' = '3306',
    'username' = 'root', 
    'password' = 'mysql',
    'database-name' = 'test', 
    'table-name' = 'username'
);


https://github.com/ververica/flink-cdc-connectors/wiki/MySQL-CDC-Connector#connector-options

create table sink (
    id int primary key,
    name varchar(10),
    mysql_create_time timestamp(6)
) WITH (
	'connector' = 'jdbc', 
    'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 
    'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'username', 
    'sink.buffer-flush.max-rows' = '1', 
    'sink.buffer-flush.interval' = '0'
);
https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/table/connectors/jdbc.html#connector-options

insert into sink select * from source;
```

可以通过 TiDB 的日志来查看实际在 TiDB 中执行的语句。

```sql
-- 设置 tidb 的慢日志记录时限
set tidb_slow_log_threshold = 0;

-- 在 mysql 中执行语句
insert into `username`(`id`, `name`) values (1, 'a'), (2, 'b'), (3, 'c');
update username set name='d' where id=2; select * from username;
delete from username where id=1; select * from username;

-- 在 tidb 中实际执行的语句
INSERT INTO `username` (`id`, `name`, `mysql_create_time`) VALUES 
(1, 'a', '2020-09-14 12:44:24.581219')
ON DUPLICATE KEY UPDATE `id`=VALUES(`id`), `name`=VALUES(`name`), `mysql_create_time`=VALUES(`mysql_create_time`);


INSERT INTO `username`(`id`, `name`) VALUES (1, 'c')  ON DUPLICATE KEY UPDATE `id`=VALUES(`id`), `name`=VALUES(`name`); -- 攒批
DELETE FROM `username` WHERE `id`=1;
```
## datagen to mysql

```sql
create table data_gen (
    id int primary key,
    name varchar(10)
) with (
    'connector' = 'datagen', 
    'rows-per-second'='100000',
    'fields.id.kind'='sequence', 
    'fields.id.start'='1', 
    'fields.id.end'='1000000',
    'fields.name.length'='10'
);

create table mysql_sink (
    id int primary key,
    name varchar(10)
) WITH (
	'connector' = 'jdbc', 
    'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 
    'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'username', 
    'sink.buffer-flush.max-rows' = '10000', 
    'sink.buffer-flush.interval' = '1'
);

insert into mysql_sink (id, name) select * from data_gen;
```

## 双流 join

MySQL 中执行

```sql
create database if not exists test; use test;
drop table if exists base;
create table base (
    id int primary key,
    location varchar(20)
);
drop table if exists stuff;
create table stuff(
    id int primary key,
    base_id int,
    name varchar(20)
);
```

TiDB 中执行

```sql
create database if not exists test; use test;

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
);
```

Flink 任务

```sql
create table base (
    id int primary key,
    location varchar(20)
) with (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 
    'port' = '3306',
    'username' = 'root', 
    'password' = 'mysql',
    'database-name' = 'test', 
    'table-name' = 'base'
);

create table stuff(
    id int primary key,
    base_id int,
    name varchar(20)
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 
    'port' = '3306',
    'username' = 'root', 
    'password' = 'mysql',
    'database-name' = 'test', 
    'table-name' = 'stuff'
);

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
	'connector' = 'jdbc', 
    'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 
    'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff', 
    'sink.buffer-flush.max-rows' = '1', 
    'sink.buffer-flush.interval' = '0'
);

explain
insert into wide_stuff
select s.id, b.id, b.location, s.name
from stuff s, base b
where s.base_id = b.id;
```


测试

```sql
insert into base values (1, 'bj');
insert into stuff values (1, 1, 'zhangsan');
insert into stuff values (2, 1, 'lisi');
insert into base values (2, 'sh');
insert into stuff values (3, 2, 'wangliu');
update stuff set name = 'wangwu' where id = 3;
delete from stuff where name = 'lisi';
update base set location = 'gz' where location = 'bj';
```

## 维表 Join

flink sql

```sql
create table stuff(
    id int primary key,
    base_id int,
    name varchar(20),
    proc_time as PROCTIME()
) WITH (
    'connector' = 'mysql-cdc',
    'hostname' = 'mysql', 
    'port' = '3306',
    'username' = 'root', 
    'password' = 'mysql',
    'database-name' = 'test', 
    'table-name' = 'stuff'
);

create table base (
    id int primary key,
    location varchar(20)
) WITH (
    'connector' = 'jdbc', 
    'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 
    'password' = 'mysql',
    'url' = 'jdbc:mysql://mysql:3306/test', 
    'table-name' = 'base',
    'lookup.cache.max-rows' = '10000', 
    'lookup.cache.ttl' = '5s'
);

create table wide_stuff(
    stuff_id int primary key,
    base_id int,
    base_location varchar(20),
    stuff_name varchar(20)
) WITH (
    'connector' = 'jdbc', 
    'driver' = 'com.mysql.cj.jdbc.Driver',
    'username' = 'root', 
    'password' = '',
    'url' = 'jdbc:mysql://tidb:4000/test?rewriteBatchedStatements=true',
    'table-name' = 'wide_stuff',
    'sink.buffer-flush.max-rows' =  '10000', 
    'sink.buffer-flush.interval' = '1s'
);

insert into wide_stuff
select s.id, b.id, b.location, s.name
from stuff as s
join base FOR SYSTEM_TIME AS OF s.proc_time  b on s.base_id = b.id;
```

测试

```sql
insert into stuff values (1, 1, 'zhangsan');
insert into base values (1, 'bj');
insert into stuff values (2, 1, 'lisi');
insert into stuff values (3, 1, 'wangliu');
update base set location = 'gz' where location = 'bj';
insert into stuff values (4, 1, 'zhaoliu');
update stuff set name = 'wangwu' where name = 'wangliu';
```
