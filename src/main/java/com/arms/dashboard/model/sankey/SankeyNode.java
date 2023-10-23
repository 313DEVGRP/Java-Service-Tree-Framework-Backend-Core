package com.arms.dashboard.model.sankey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SankeyNode {
    private String id;
    private String name;
    private String type;
}