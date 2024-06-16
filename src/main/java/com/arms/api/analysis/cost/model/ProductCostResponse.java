package com.arms.api.analysis.cost.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.TreeMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCostResponse {
    TreeMap<String, Integer> line;
    TreeMap<String, Integer> bar;
    TreeMap<String, List<Integer>> candleStick;
}
