package org.darebeat.sources;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Created by darebeat on 2020/11/2.
 */
public class TextSourceMain {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> dataDS = env.readTextFile(Thread.currentThread().getContextClassLoader().getResource("application.properties").getFile());

        dataDS.print("stream");

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
