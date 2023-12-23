package com.arms.dashboard.model;

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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SankeyLink {
        private String source;
        private String target;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SankeyNode {
        private String id;
        private String name;
        private String type;
    }
}
