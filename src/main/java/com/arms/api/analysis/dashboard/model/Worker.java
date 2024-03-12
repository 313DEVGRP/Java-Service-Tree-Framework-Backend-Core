package com.arms.api.analysis.dashboard.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Worker {
    String id;
    String name;
    Map<String, Integer> data;
    List<TaskList> children = new ArrayList<>();

    public Worker(String id, String name) {
        this.id = id;
        this.name = name;
    }
}