package org.darebeat.fileopt;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Created by darebeat on 2020/9/26.
 */
public class fileOpt {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<String> data = env.readTextFile("file:///opt/flink/README.txt");
        data.print();

        //两种格式都行，另外还支持写入到 hdfs
//        data.writeAsText("file:///opt/flink/README1.txt");
        data.writeAsText("/opt/flink/README1.txt");

        env.execute();
    }
}
