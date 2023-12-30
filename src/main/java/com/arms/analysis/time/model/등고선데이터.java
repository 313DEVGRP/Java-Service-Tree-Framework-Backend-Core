package com.arms.analysis.time.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 등고선데이터 {
    private Long version;
    private String date;
    private String name;
    private int value;
    private String summary;
}
