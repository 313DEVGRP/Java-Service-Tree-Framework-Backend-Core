package com.arms.api.util.communicate.external.response.aggregation;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class 검색결과_목록 {

	private  List<검색결과> 결과 = new ArrayList<>();
}
