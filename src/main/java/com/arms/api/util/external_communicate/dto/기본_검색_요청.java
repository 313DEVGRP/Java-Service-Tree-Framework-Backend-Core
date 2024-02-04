package com.arms.api.util.external_communicate.dto;

import lombok.*;

@Setter
@Getter
public abstract class 기본_검색_요청 {
    private int 크기 = 1000;
    private int 하위_크기 = 1000;
}
