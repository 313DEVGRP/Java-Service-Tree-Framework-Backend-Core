package com.arms.api.analysis.cost.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCostResponse {
    private Long totalAnnualIncome;
    private Map<String, Long> monthlyCost;
}
