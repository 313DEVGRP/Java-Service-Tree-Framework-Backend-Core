package com.arms.dashboard.model.sankey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SankeyLink {
    private String source;
    private String target;
}