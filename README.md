# README

## 修改maven为国内源`~/.m2/settings.xml`

```xml
<mirror>
  <id>alimaven</id>
  <mirrorOf>central</mirrorOf>
  <name>aliyun maven</name>
  <url>https://maven.aliyun.com/repository/central</url>
</mirror>
```

## kafka 基础命令

```sh
# 创建topic
bin/kafka-topics.sh --create \
--zookeeper 127.0.0.1:2181 \
--replication-factor 1 \
--partitions 2 \
--topic metric

# topic生产者
bin/kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic metric --property parse.key=false

# topic消费者
bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic metric --from-beginning

# 查看topic信息
bin/kafka-topics.sh --describe --zookeeper 127.0.0.1:2181 --topic metric

# 删除topic
bin/kafka-topics  --delete --zookeeper 127.0.0.1:2181 --topic metric

# 查看group
bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list
```

## RoadMap

+ 1. [SocketTextStreamWordCount](./SocketTextStreamWordCount)