package com.arms.api.util.external_communicate.dto.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class 검색결과 {

    private String 필드명;
    private long 개수;
    private Map<String, List<검색결과>> 하위검색결과 = new HashMap<>();

}
