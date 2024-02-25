package com.arms.api.analysis.scope.service;


import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.api.util.external_communicate.dto.요구사항_버전_이슈_키_상태_작업자수;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.external_communicate.내부통신기;
import com.arms.api.util.external_communicate.통계엔진통신기;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScopeServiceImpl implements ScopeService {

    @Autowired
    private ReqAdd reqAdd;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PdService pdService;
    private final 내부통신기 내부통신기;
    private final 통계엔진통신기 통계엔진통신기;
    private final Gson gson;

    @Override
    public List<TreeBarDTO> treeBar(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {
        ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
        Long pdServiceLink = 지라이슈_제품_및_제품버전_검색요청.getPdServiceLink();
        List<Long> pdServiceVersionLinks = 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks();
    
        List<ReqStatusEntity> reqStatuses = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceLink, reqStatusDTO);
    
        List<TreeBarDTO> treeBarList = new ArrayList<>();
    
        // 1. 제품 조회
        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(pdServiceLink);
        PdServiceEntity product = pdService.getNode(pdServiceEntity);
    
        // 2. 제품 등록
        treeBarList.add(addProduct(product));
    
        // 3. 제품 버전 조회
        List<PdServiceVersionEntity> productVersions = product.getPdServiceVersionEntities().stream().filter(
                pdServiceVersionEntity -> 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks().contains(pdServiceVersionEntity.getC_id())
        ).sorted(Comparator.comparing(PdServiceVersionEntity::getC_id)).collect(Collectors.toList());
    
        // 4. 요구사항 조회
        List<ReqStatusEntity> reqStatusEntityList = getRequirements(reqStatuses, pdServiceVersionLinks);
    
        // 4-1. 요구사항 DTO로 변환
        List<TreeBarDTO> requirements = reqStatusEntityList.stream().map(TreeBarDTO::new).collect(Collectors.toList());
    
        // 5. 각 요구사항 별 담당자와 빈도수를 조회 (Top 10)
        ResponseEntity<검색결과_목록_메인> 외부API응답 = 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(지라이슈_제품_및_제품버전_검색요청);
    
        검색결과_목록_메인 검색결과목록메인 = Optional.ofNullable(외부API응답.getBody()).orElse(new 검색결과_목록_메인());
    
        Map<String, List<검색결과>> 검색결과 = Optional.ofNullable(검색결과목록메인.get검색결과()).orElse(Collections.emptyMap());
    
        List<검색결과> groupByParentReqKey = Optional.ofNullable(검색결과.get("group_by_parentReqKey")).orElse(Collections.emptyList());
    
        // 6. 담당자가 많은 요구사항 Top 10 추출. 담당자가 없는 요구사항은 제외됩니다.
        List<검색결과> top10Requirements = groupByParentReqKey.stream()
                .sorted((a, b) -> Integer.compare(
                        b.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").size(),
                        a.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").size()))
                .limit(10)
                .collect(Collectors.toList());
    
        // 7. top10 요소들의 필드명 추출
        List<String> issueKeys = top10Requirements.stream()
                .map(com.arms.api.util.external_communicate.dto.search.검색결과::get필드명)
                .collect(Collectors.toList());
    
    
        // 8. requirements 리스트를 필터링하여 id 값이 issueKeys 에 있는 요소만 선택
        List<TreeBarDTO> filteredRequirements = filteredRequirements(requirements, issueKeys);
    
        // 9. Top 10 요구사항의 버전 정보 추출하여 리스트에 추가
        List<TreeBarDTO> versionList = addProductVersions(product, productVersions, filteredRequirements);
        treeBarList.addAll(versionList);
    
        // 10. 요구사항 추가
        treeBarList.addAll(filteredRequirements);
    
        // 11. 담당자 추가
        treeBarList.addAll(addAssignees(top10Requirements));
    
        return treeBarList;
     }
    
    private List<TreeBarDTO> addProductVersions(
        PdServiceEntity product,
        List<PdServiceVersionEntity> productVersions,
        List<TreeBarDTO> filteredRequirements
    ) {
    
        List<TreeBarDTO> 트리바제품버전데이터 = new ArrayList<>();
    
        String 제품아이디 = String.valueOf(product.getC_id());
    
        Set<String> 버전중복제거 = new HashSet<>();
    
        filteredRequirements.stream()
                .filter(filteredRequirement -> 버전중복제거.add(filteredRequirement.getParent()))
                .forEach(filteredRequirement -> {
                    String 요구사항의버전아이디 = filteredRequirement.getParent().replace("[", "").replace("]", "");
                    
                    List<String> 제품버전명목록 = productVersions.stream()
                            .filter(version -> 요구사항의버전아이디.contains(String.valueOf(version.getC_id())))
                            .map(PdServiceVersionEntity::getC_title)
                            .collect(Collectors.toList());
                    
                    String 요구사항의버전명 = String.join(", ", 제품버전명목록);
                    
                    트리바제품버전데이터.add(TreeBarDTO.builder()
                            .id(filteredRequirement.getParent()) // ["17","18","19"]
                            .name(요구사항의버전명) // "17","18","19"
                            .color("")
                            .type("version")
                            .parent(제품아이디)
                            .build());
                });
    
        return 트리바제품버전데이터;
    }
    
    private TreeBarDTO addProduct(PdServiceEntity product) {
         return TreeBarDTO.builder()
                 .id(product.getC_id().toString())
                 .name(product.getC_title())
                 .type("product")
                 .color("")
                 .parent("")
                 .build();
     }
    
    private List<ReqStatusEntity> getRequirements(List<ReqStatusEntity> reqStatuses, List<Long> pdServiceVersionLinks) {
        return reqStatuses.stream()
                .filter(entity -> {
                    List<Long> versionSet = gson.fromJson(entity.getC_req_pdservice_versionset_link(), new TypeToken<List<Long>>(){}.getType());
                    return versionSet.stream().anyMatch(pdServiceVersionLinks::contains);
                })
                .collect(Collectors.toList());
    }
    
    private List<TreeBarDTO> filteredRequirements(List<TreeBarDTO> requirements, List<String> issueKeys) {
         return requirements.stream()
                 .filter(req -> issueKeys.contains(req.getId()))
                 .collect(Collectors.toList());
     }
    
    private List<TreeBarDTO> addAssignees(List<검색결과> top10Requirements) {
        Map<String, String> assigneeToColorMap = new HashMap<>();
        return top10Requirements.stream()
                .flatMap(parentReqKey -> {
                    String parent = parentReqKey.get필드명();
                    return parentReqKey.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").stream()
                            .map(assignee -> {
                                String name = assignee.get필드명();
                                String color = assigneeToColorMap.computeIfAbsent(name, k ->
                                        String.format("#%02x%02x%02x", RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256))
                                );
                                long value = assignee.get개수();
                                return TreeBarDTO.builder()
                                        .id(parent + assignee.get필드명())
                                        .parent(parent)
                                        .type("assignee")
                                        .name(name + " (" + value + ")")
                                        .value(value)
                                        .color(color)
                                        .build();
                            });
                })
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, List<요구사항_버전_이슈_키_상태_작업자수>> 버전이름_매핑하고_같은_버전_묶음끼리_배치(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {
        List<요구사항_버전_이슈_키_상태_작업자수> 버전배열_요구사항_별_상태_및_관여_작업자_수 = 통계엔진통신기.버전배열_요구사항_별_상태_및_관여_작업자_수(pdServiceId, pdServiceVersionLinks).getBody();


        Map<String, List<요구사항_버전_이슈_키_상태_작업자수>> 버전_요구사항_상태_작업자_맵 = new HashMap<>();

        PdServiceEntity 검색용도_제품 = new PdServiceEntity();
        검색용도_제품.setC_id(pdServiceId);
        PdServiceEntity 제품_검색결과 = pdService.getNode(검색용도_제품);
        Set<PdServiceVersionEntity> 제품_버전_세트 = 제품_검색결과.getPdServiceVersionEntities();
        Map<Long, String> 버전_아이디_이름_맵 = 제품_버전_세트.stream()
                .collect(Collectors.toMap(PdServiceVersionEntity::getC_id, PdServiceVersionEntity::getC_title));

        for (요구사항_버전_이슈_키_상태_작업자수 묶음 : 버전배열_요구사항_별_상태_및_관여_작업자_수) {
            Long[] versionArr = 묶음.getVersionArr();
            StringBuilder keyBuilder = new StringBuilder();

            if (versionArr.length != 0) {
                for (int i=0; i< versionArr.length; i++) {
                    if (i==0) {
                        keyBuilder.append(버전_아이디_이름_맵.get(versionArr[i]));
                    } else {
                        keyBuilder.append(", ").append(버전_아이디_이름_맵.get(versionArr[i]));
                    }
                }
            }
            String key = keyBuilder.toString();
            log.info("[ScopeServiceImple  :: 버전_요구사항_자료] :: 만들어진Key ==> {}", key);

            List<요구사항_버전_이슈_키_상태_작업자수> 리스트 = 버전_요구사항_상태_작업자_맵.get(key);
            if(리스트 == null) {
                리스트 = new ArrayList<>();
                버전_요구사항_상태_작업자_맵.put(key, 리스트);
            }
            리스트.add(묶음);
        }

        return 버전_요구사항_상태_작업자_맵;
    }

    @Override
    public Map<String, Long> 버전_요구사항_자료(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        SessionUtil.setAttribute("getReqAddListByFilter",changeReqTableName);

        PdServiceEntity 검색용도_제품 = new PdServiceEntity();
        검색용도_제품.setC_id(pdServiceId);
        PdServiceEntity 제품_검색결과 = pdService.getNode(검색용도_제품);
        Set<PdServiceVersionEntity> 제품_버전_세트 = 제품_검색결과.getPdServiceVersionEntities();
        Map<Long, String> 버전_아이디_이름_맵 = 제품_버전_세트.stream()
                .collect(Collectors.toMap(PdServiceVersionEntity::getC_id, PdServiceVersionEntity::getC_title));

        ReqAddEntity 검색용도_객체 = new ReqAddEntity();

        if (pdServiceVersionLinks != null && !pdServiceVersionLinks.isEmpty()) {
            Disjunction orCondition = Restrictions.disjunction();
            for (Long 버전 : pdServiceVersionLinks) {
                String 버전_문자열 = "\\\"" + String.valueOf(버전) + "\\\"";
                orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", 버전_문자열, MatchMode.ANYWHERE));
            }
            검색용도_객체.getCriterions().add(orCondition);
        }

        List<ReqAddEntity> 검색_결과_목록 = reqAdd.getChildNode(검색용도_객체);

        Map<String, Long> 버전_요구사항_맵 = new HashMap<>();
        for(ReqAddEntity 요구사항 : 검색_결과_목록) {
            // 정렬된 값을 문자열로 만듭니다.
            StringBuilder keyBuilder = new StringBuilder();
            String 버전세트_문자열 = 요구사항.getC_req_pdservice_versionset_link();
            Long[] 버전_아이디_배열 = convertToLongArray(버전세트_문자열);

            if (버전_아이디_배열.length != 0) {
                for (int i=0; i< 버전_아이디_배열.length; i++) {
                    if (i==0) {
                        keyBuilder.append(버전_아이디_이름_맵.get(버전_아이디_배열[i]));
                    } else {
                        keyBuilder.append(", ").append(버전_아이디_이름_맵.get(버전_아이디_배열[i]));
                    }
                }
            }
            String key = keyBuilder.toString();
            log.info("[ScopeServiceImple  :: 버전_요구사항_자료] :: 만들어진Key ==> {}", key);
            // Map에 해당하는 값을 증가시킵니다.
            버전_요구사항_맵.put(key, 버전_요구사항_맵.getOrDefault(key, 0L) + 1);
            
        }

        SessionUtil.removeAttribute("getReqAddListByFilter");
        log.info("[ScopeServiceImple  :: 버전_요구사항_자료] :: 버전_요구사항_맵 ==> {}", 버전_요구사항_맵.toString());

        return 버전_요구사항_맵;
    }

    private static Long[] convertToLongArray(String input) {
        // 입력이 null이거나 비어있을 때, 길이 0 배열 반환
        if (input == null || input.isEmpty()) {
            return new Long[0];
        }
        // 문자열에서 대괄호 및 쌍따옴표를 제거하고 쉼표로 구분하여 문자열 배열로 변환
        String[] stringArray = input.substring(1, input.length() - 1).split(",");

        // 예외 처리: stringArray의 길이가 0인 경우
        if (stringArray.length == 0) {
            throw new IllegalArgumentException("[ScopeServiceImpl :: convertToLongArray] :: stringArray의 입력이 올바른 형식이 아닙니다.");
        }

        // Long 배열 생성
        Long[] longArray = new Long[stringArray.length];

        // 문자열 배열을 Long 배열로 변환
        for (int i = 0; i < stringArray.length; i++) {
            try {
                longArray[i] = Long.parseLong(stringArray[i].replaceAll("\"", "").trim());
            } catch (NumberFormatException e) {
                // 숫자로 변환할 수 없는 경우에는 null을 할당
                longArray[i] = null;
            } catch (ArrayIndexOutOfBoundsException e) {
                // 배열 인덱스가 범위를 벗어나는 경우, 예외 처리
                log.error("[ScopeServiceImpl :: convertToLongArray] :: longArray[{}]에서 배열 인덱스가 범위를 벗어났습니다.", i);
            } catch (Exception e) {
                log.error("[ScopeServiceImpl :: convertToLongArray] :: longArray[{}]에서 예상치 못한 예외가 발생했습니다 => {}", i, e.getMessage());
            }
        }

        return longArray;
    }
}
