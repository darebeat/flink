package org.darebeat.sinks;


import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.darebeat.common.model.Student;
import org.darebeat.common.utils.ExecutionEnvUtil;
import org.darebeat.common.utils.GsonUtil;
import org.darebeat.common.utils.KafkaConfigUtil;

import java.util.Properties;

import static org.darebeat.common.constant.PropertiesConstants.METRICS_TOPIC;

/**
 * Created by darebeat on 2020/11/2.
 */
public class MysqlSinkMain {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        ParameterTool parameterTool = ExecutionEnvUtil.PARAMETER_TOOL;
        Properties props = KafkaConfigUtil.buildKafkaProps(parameterTool);

        SingleOutputStreamOperator<Student> stu = env.addSource(new FlinkKafkaConsumer011<>(
                parameterTool.get(METRICS_TOPIC),   //这个 kafka topic 需要和上面的工具类的 topic 一致
                new SimpleStringSchema(),
                props)).setParallelism(1)
                .map(string -> GsonUtil.fromJson(string, Student.class));

        //数据 sink 到 mysql
        stu.addSink(new Sink2Mysql());

        env.execute("Flink data sink to mysql");
    }
}
