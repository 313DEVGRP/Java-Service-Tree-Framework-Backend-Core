package com.arms.dashboard.model.sankey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SankeyData {
    private List<SankeyNode> nodes = new ArrayList<>();
    private List<SankeyLink> links = new ArrayList<>();

}
