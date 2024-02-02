package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.util.external_communicate.dto.IsReqType;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.external_communicate.통계엔진통신기;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class 비용서비스_구현 implements 비용서비스 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Autowired
    통계엔진통신기 통계엔진통신기;

    public 버전요구사항별_담당자데이터 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {

        지라이슈_제품_및_제품버전_검색요청.setIsReqType(IsReqType.REQUIREMENT);
        ResponseEntity<List<검색결과>> 요구사항_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(지라이슈_제품_및_제품버전_검색요청);

        지라이슈_제품_및_제품버전_검색요청.setIsReqType(IsReqType.ISSUE);
        ResponseEntity<List<검색결과>> 하위이슈_결과 = 통계엔진통신기.제품별_버전_및_요구사항별_작업자(지라이슈_제품_및_제품버전_검색요청);

        List<검색결과> 전체결과 = new ArrayList<>();

        전체결과.addAll(요구사항_결과.getBody());
        전체결과.addAll(하위이슈_결과.getBody());

        Map<String, 버전요구사항별_담당자데이터.버전데이터> 버전데이터Map = new HashMap<>();
        Map<String, 버전요구사항별_담당자데이터.담당자데이터> 전체담당자Map = new HashMap<>();

        Optional<List<검색결과>> optionalEsData = Optional.ofNullable(전체결과);
        optionalEsData.ifPresent(esData -> {
            esData.forEach(result -> {
                Map<String, 버전요구사항별_담당자데이터.요구사항데이터> 요구사항데이터Map = new HashMap<>();

                String versionId = result.get필드명();
                List<검색결과> requirements = new ArrayList<>();
                if (result.get하위검색결과().get("requirement") != null)
                    requirements.addAll(result.get하위검색결과().get("requirement"));
                if (result.get하위검색결과().get("parentRequirement") != null)
                    requirements.addAll(result.get하위검색결과().get("parentRequirement"));

                requirements.stream().forEach(requirement -> {
                    String requirementId = requirement.get필드명();

                    Map<String, 버전요구사항별_담당자데이터.담당자데이터> 담당자데이터Map = new HashMap<>();

                    requirement.get하위검색결과().get("assignees").forEach(assignee -> {
                        String assigneeAccountId = assignee.get필드명();

                        assignee.get하위검색결과().get("displayNames").stream().forEach(displayName -> {

                            String assigneeDisplayName = displayName.get필드명();
                            String 고유아이디 = versionId + "-" + assigneeAccountId;

                            버전요구사항별_담당자데이터.담당자데이터 담당자데이터 = 버전요구사항별_담당자데이터.담당자데이터.builder()
                                    .이름(assigneeDisplayName)
                                    .연봉(null)
                                    .성과(null)
                                    .build();

                            담당자데이터Map.put(assigneeAccountId, 담당자데이터);
                            전체담당자Map.put(assigneeAccountId, 담당자데이터);
                        });

                        버전요구사항별_담당자데이터.요구사항데이터 요구사항데이터 = 버전요구사항별_담당자데이터.요구사항데이터.builder()
                                .담당자(담당자데이터Map)
                                .build();

                        요구사항데이터Map.put(requirementId, 요구사항데이터);
                    });

                    버전요구사항별_담당자데이터.버전데이터 버전데이터 = 버전요구사항별_담당자데이터.버전데이터.builder()
                            .요구사항(요구사항데이터Map)
                            .build();

                    버전데이터Map.put(versionId, 버전데이터);
                });
            });
        });
        버전요구사항별_담당자데이터 버전요구사항별_담당자데이터 = new 버전요구사항별_담당자데이터();
        버전요구사항별_담당자데이터.set버전(버전데이터Map);
        버전요구사항별_담당자데이터.set전체담당자목록(전체담당자Map);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(버전요구사항별_담당자데이터);
            로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 버전요구사항별_담당자데이터 -> ");
            로그.info(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return 버전요구사항별_담당자데이터;
    }

}
