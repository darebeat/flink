package org.darebeat.sources;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by darebeat on 2020/11/2.
 */
public class CollectionSourceMain {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        ArrayList<String> list = new ArrayList<>(
                Arrays.asList("name1", "name2", "name3")
        );

        env.fromCollection(list).print("stream");

//        env.fromElements("name", 18, 20.1).print("stream");

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
