package org.darebeat.sources;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.darebeat.sources.utils.MySQLUtil;

public class MysqlSourceMain {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.addSource(new MySQLUtil()).print();

		try {
			env.execute("Flink add data source");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
