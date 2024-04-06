package com.arms.api.analysis.cost.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCostResponse {
    TreeMap<String, Integer> line;
    TreeMap<String, Integer> bar;
}
