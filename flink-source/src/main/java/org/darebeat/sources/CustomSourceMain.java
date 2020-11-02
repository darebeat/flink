package org.darebeat.sources;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.darebeat.sources.utils.CustomSourceUtil;

/**
 * Created by darebeat on 2020/11/2.
 */
public class CustomSourceMain {
    public static void main(String[] args) {
        // 创建env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 自定义SourceFunction
        CustomSourceUtil cs = new CustomSourceUtil();

        // 创建数据源
        DataStreamSource<Tuple2<String, Long>> customDS = env.addSource(cs);
        // 开始处理
        customDS.print();

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
