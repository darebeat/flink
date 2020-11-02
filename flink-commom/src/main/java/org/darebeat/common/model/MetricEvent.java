package org.darebeat.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by darebeat on 2020/11/2.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricEvent {
    private String name;
    private Long timestamp;
    private Map<String, Object> fields;
    private Map<String, String> tags;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTags(Map<String,String> tags) {
        this.tags = tags;
    }
    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
