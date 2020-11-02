package org.darebeat.sources.utils;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by darebeat on 2020/11/2.
 */
public class CustomSourceUtil extends RichSourceFunction<Tuple2<String, Long>> {

    private boolean flag = true;

    @Override
    public void run(SourceContext<Tuple2<String, Long>> ctx) throws Exception {
        List<String> data = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        Random random = new Random();
        while (flag) {
            Thread.sleep(100);
            // 随机取一个值
            String key = data.get(random.nextInt(data.size()));
            long value = System.currentTimeMillis();
            ctx.collect(Tuple2.of(key, value));
        }
    }

    @Override
    public void cancel() {
        flag = false;
    }
}
