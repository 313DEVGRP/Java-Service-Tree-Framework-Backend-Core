package com.arms.api.analysis.cost.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandleStick implements Serializable {
    Integer 시가;
    Integer 종가;
    Integer 최저가;
    Integer 최고가;
}
