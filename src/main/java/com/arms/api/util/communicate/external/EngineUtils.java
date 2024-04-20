package com.arms.api.util.communicate.external;

import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EngineUtils {
    public static List<검색결과> flat(검색결과_목록_메인 nullableBody, String mainAggregationGroupField) {
        if (nullableBody == null) {
            return Collections.emptyList();
        }
        Map<String, List<검색결과>> nullableMap = nullableBody.get검색결과();
        List<검색결과> nullableList = nullableMap.get("group_by_" + mainAggregationGroupField);
        if (nullableList == null) {
            return Collections.emptyList();
        }
        return nullableList;
    }


}
