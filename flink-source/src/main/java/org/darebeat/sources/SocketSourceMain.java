package org.darebeat.sources;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Created by darebeat on 2020/11/2.
 */
public class SocketSourceMain {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> dataDS = env.socketTextStream("127.0.0.1", 9000);

        dataDS.print("stream");

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
