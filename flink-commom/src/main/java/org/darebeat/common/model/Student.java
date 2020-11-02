package org.darebeat.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by darebeat on 2020/11/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    public int id;
    public String name;
    public String password;
    public int age;
}
