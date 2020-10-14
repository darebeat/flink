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
# 监控9000端口
nc -l 9000
flink run -c org.darebeat.wordcount.SocketTextStreamWordCount ./target/SocketTextStreamWordCount-1.0-SNAPSHOT.jar 127.0.0.1 9000
# 查看flink版本以及位置信息
brew info apache-flink
# 查看日志,输出统计结果
tail -f libexec/log/flink-$(whoami)-taskexecutor-0-$(whoami).out
```
