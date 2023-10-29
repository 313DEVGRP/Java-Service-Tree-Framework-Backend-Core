package com.arms.dashboard.model.power;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Worker {
    String id;
    String name;
    List<TaskList> children = new ArrayList<>();
    int totalInvolvedCount = 0;

    public Worker(String id, String name) {
        this.id = id;
        this.name = name;
    }
}