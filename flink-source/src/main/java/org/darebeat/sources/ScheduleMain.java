package org.darebeat.sources;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.darebeat.common.model.MetricEvent;
import org.darebeat.sources.model.Rule;
import org.darebeat.common.utils.ExecutionEnvUtil;
import org.darebeat.common.utils.KafkaConfigUtil;
import org.darebeat.sources.utils.MySQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by darebeat on 2020/11/2.
 */
@Slf4j
public class ScheduleMain {
    public static List<Rule> rules;

    public static void main(String[] args) throws Exception {
        //定时捞取规则，每隔一分钟捞一次
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.scheduleAtFixedRate(new GetRulesJob(), 0, 1, TimeUnit.MINUTES);

        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);

        DataStreamSource<MetricEvent> source = KafkaConfigUtil.buildSource(env);
        source.map(new MapFunction<MetricEvent, MetricEvent>() {
            @Override
            public MetricEvent map(MetricEvent value) throws Exception {
                if (rules.size() <= 2) {
                    System.out.println("===========2");
                } else {
                    System.out.println("===========3");
                }
                return value;
            }
        }).print();

        env.execute("schedule");
    }

    private static class GetRulesJob implements Runnable {

        @Override
        public void run() {
            try {
                rules = getRules();
            } catch (SQLException e) {
                log.error("get rules from mysql has an error {}", e.getMessage());
            }
        }

        private List<Rule> getRules() throws SQLException{
            System.out.println("-----get rule");
            String sql = "select * from rule";

            Connection connection = MySQLUtil.getConnection("com.mysql.jdbc.Driver",
                    "jdbc:mysql://127.0.0.1:13306/test?useUnicode=true&characterEncoding=UTF-8",
                    "root",
                    "mysql");

            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<Rule> list = new ArrayList<>();
            while (rs.next()){
                list.add(Rule.builder()
                        .id(rs.getString("id"))
                        .name(rs.getString("name"))
                        .type(rs.getString("type"))
                        .measurement(rs.getString("measurement"))
                        .threshold(rs.getString("threshold"))
                        .level(rs.getString("level"))
                        .targetType(rs.getString("target_type"))
                        .targetId(rs.getString("target_id"))
                        .webhook(rs.getString("webhook"))
                        .build()
                );
            }
            return list;
        }
    }
}
