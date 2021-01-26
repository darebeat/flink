# README

基于Socket输入单次,进行词频统计

## 生成项目

```sh
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.flink \
  -DarchetypeArtifactId=flink-quickstart-java \
  -DarchetypeVersion=1.11.0 \
  -DgroupId=org.darebeat \
  -DartifactId=flink-wordcount \
  -Dversion=1.0-SNAPSHOT \
  -Dpackage=org.darebeat \
  -DinteractiveMode=false
```

## 打包项目

```sh
mvn clean package -Dmaven.test.skip=true
```

## 运行

```sh
# 在edge监控9000端口
docker exec -it flink-edge nc -l 9000

docker exec -it flink-jobmanager flink run \
-c org.darebeat.wordcount.SocketTextStreamWordCount \
/opt/flink/src/flink-streaming/target/original-flink-streaming-1.0-SNAPSHOT.jar \
172.18.0.100 9000
```

## 在对应task-manager Stdout中查看日志

[http://localhost:8081/#/task-manager](http://localhost:8081/#/task-manager)