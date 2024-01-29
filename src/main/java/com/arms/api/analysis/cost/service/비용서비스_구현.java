package com.arms.api.analysis.cost.service;

import com.arms.api.util.external_communicate.dto.IsReqType;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.external_communicate.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class 비용서비스_구현 implements 비용서비스 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Autowired
    통계엔진통신기 통계엔진통신기;

    public List<검색결과> 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        지라이슈_제품_및_제품버전_검색요청.setIsReqType(IsReqType.REQUIREMENT);
        ResponseEntity<List<검색결과>> 요구사항_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(지라이슈_제품_및_제품버전_검색요청);

        지라이슈_제품_및_제품버전_검색요청.setIsReqType(IsReqType.ISSUE);
        ResponseEntity<List<검색결과>> 하위이슈_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(지라이슈_제품_및_제품버전_검색요청);

        List<검색결과> 전체결과 = new ArrayList<>();

        전체결과.addAll(요구사항_결과.getBody());
        전체결과.addAll(하위이슈_결과.getBody());

//        Optional<List<검색결과>> optionalEsData = Optional.ofNullable(전체결과);
//        optionalEsData.ifPresent(esData -> {
//            esData.forEach(result -> {
//                String versionId = result.get필드명();
//                result.get하위검색결과().get("assignees").forEach(assignee -> {
//                    String assigneeAccountId = assignee.get필드명();
//                    assignee.get하위검색결과().get("displayNames").stream().forEach(displayName -> {
//                        String assigneeDisplayName = displayName.get필드명();
//                        String workerNodeId = versionId + "-" + assigneeAccountId;
//                    });
//                });
//            });
//        });

        return 전체결과;
    }

}
