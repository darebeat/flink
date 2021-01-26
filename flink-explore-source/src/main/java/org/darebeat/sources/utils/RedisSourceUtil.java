package org.darebeat.sources.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashMap;
import java.util.Map;


/**
 *
 * 在redis中保存的有国家和大区的关系
 * hset  areas AREA_US US
 * hset  areas AREA_CT TW,HK
 * hset  areas AREA_AR PK,KW,SA
 * hset  areas AREA_IN IN
 *./bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic allDataClean--from-beginning
 *
 * 我们需要返回kv对的，就要考虑HashMap
 */
@Slf4j
public class RedisSourceUtil implements SourceFunction<HashMap<String,String>> {

    private boolean isRunning =true;
    private Jedis jedis=null;
    private final long SLEEP_MILLION=5000;

    @Override
    public void run(SourceContext<HashMap<String, String>> ctx) throws Exception {
        this.jedis = new Jedis("127.0.0.1", 6379);
        HashMap<String, String> kVMap = new HashMap<String, String>();

        while(isRunning){
            try{
                kVMap.clear();
                Map<String, String> areas = jedis.hgetAll("areas");
                for(Map.Entry<String,String> entry:areas.entrySet()){
                    // key :大区 value：国家
                    String key = entry.getKey();
                    String value = entry.getValue();
                    String[] splits = value.split(",");
                    System.out.println("key:"+key+"，--value："+value);
                    for (String split:splits){
                        // key :国家value：大区
                        kVMap.put(split, key);
                    }
                }
                if(kVMap.size()>0){
                    ctx.collect(kVMap);
                }else {
                    log.warn("从redis中获取的数据为空");
                }
                Thread.sleep(SLEEP_MILLION);
            }catch (JedisConnectionException e){
                log.warn("redis连接异常，需要重新连接",e.getCause());
                jedis = new Jedis("hadoop01", 6379);
            }catch (Exception e){
                log.warn(" source 数据源异常",e.getCause());
            }
        }
    }

    @Override
    public void cancel() {
        isRunning=false;
        while(jedis!=null){
            jedis.close();
        }
    }
}
