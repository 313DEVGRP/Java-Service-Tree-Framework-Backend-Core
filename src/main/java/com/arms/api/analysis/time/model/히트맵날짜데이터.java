package com.arms.api.analysis.time.model;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class 히트맵날짜데이터 {
    private Set<String> items;
    private Set<String> contents;
    private int count = 0;
}
