package org.darebeat.sources.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Created by darebeat on 2020/11/2.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rule {
    private String id;
    private String name;
    private String type;
    private String measurement;
    private String expression;
    private String threshold;
    private String level;
    private String targetType;
    private String targetId;
    private String webhook;
}
