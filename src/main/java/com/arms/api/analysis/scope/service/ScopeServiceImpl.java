package com.arms.api.analysis.scope.service;


import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.analysis.scope.dto.요구사항_버전명추가_DTO;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.util.TreeServiceUtils;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.external.request.aggregation.요구사항_버전_이슈_키_상태_작업자수;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.api.util.communicate.internal.내부통신기;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.api.util.버전유틸;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScopeServiceImpl implements ScopeService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReqAdd reqAdd;

    private final PdService pdService;

    private final 내부통신기 내부통신기;

    private final 통계엔진통신기 통계엔진통신기;

    @Override
    public List<지라이슈> 제품서비스_버전목록으로_조회(Long pdServiceLink, List<Long> pdServiceVersionLinks) {
        List<지라이슈> result = 통계엔진통신기.제품서비스_버전목록으로_조회(pdServiceLink, pdServiceVersionLinks);
        return result;
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

                    String 요구사항의버전명 = String.join(",", 제품버전명목록);

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
                for (int i = 0; i < versionArr.length; i++) {
                    if (i == 0) {
                        keyBuilder.append(버전_아이디_이름_맵.get(versionArr[i]));
                    } else {
                        keyBuilder.append(", ").append(버전_아이디_이름_맵.get(versionArr[i]));
                    }
                }
            }
            String key = keyBuilder.toString();
            log.info("[ScopeServiceImple  :: 버전_요구사항_자료] :: 만들어진Key ==> {}", key);

            List<요구사항_버전_이슈_키_상태_작업자수> 리스트 = 버전_요구사항_상태_작업자_맵.get(key);
            if (리스트 == null) {
                리스트 = new ArrayList<>();
                버전_요구사항_상태_작업자_맵.put(key, 리스트);
            }
            리스트.add(묶음);
        }

        return 버전_요구사항_상태_작업자_맵;
    }



    @Override
    public Map<String, Long> 버전_요구사항_자료(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        SessionUtil.setAttribute("getReqAddListByFilter", changeReqTableName);

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
        검색용도_객체.getCriterions().add(Restrictions.eq("c_type", "default"));

        List<ReqAddEntity> 검색_결과_목록 = reqAdd.getChildNode(검색용도_객체);

        Map<String, Long> 버전_요구사항_맵 = new HashMap<>();
        for (ReqAddEntity 요구사항 : 검색_결과_목록) {
            // 정렬된 값을 문자열로 만듭니다.
            StringBuilder keyBuilder = new StringBuilder();
            String 버전세트_문자열 = 요구사항.getC_req_pdservice_versionset_link();
            Long[] 버전_아이디_배열 = 버전유틸.convertToLongArray(버전세트_문자열);

            if (버전_아이디_배열.length != 0) {
                for (int i = 0; i < 버전_아이디_배열.length; i++) {
                    if (i == 0) {
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

    @Override
    public List<요구사항_버전명추가_DTO> 버전_요구사항_상태(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception {

        SessionUtil.setAttribute("getReqAddListByFilter", changeReqTableName);

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
        검색용도_객체.getCriterions().add(Restrictions.eq("c_type", "default"));

        List<ReqAddEntity> 검색_결과_목록 = reqAdd.getChildNode(검색용도_객체);
        List<요구사항_버전명추가_DTO> 결과_목록 = new ArrayList<>();
        Map<String, String> 버전세트문자열_버전명_맵 = new HashMap<>();

        for(ReqAddEntity 요구사항 : 검색_결과_목록) {
            요구사항_버전명추가_DTO dto = new 요구사항_버전명추가_DTO();
            dto.setReqAddEntity(요구사항);

            String 버전세트_문자열 = 요구사항.getC_req_pdservice_versionset_link();
            Long[] 버전_아이디_배열 = 버전유틸.convertToLongArray(버전세트_문자열);
            StringBuilder keyBuilder = new StringBuilder();

            if(버전세트문자열_버전명_맵.containsKey(버전세트_문자열)) {
                dto.setVersion_name(버전세트문자열_버전명_맵.get(버전세트_문자열));
                결과_목록.add(dto);
            } else {
                if (버전_아이디_배열.length != 0) {
                    for (int i = 0; i < 버전_아이디_배열.length; i++) {
                        if (i == 0) {
                            keyBuilder.append(버전_아이디_이름_맵.get(버전_아이디_배열[i]));
                        } else {
                            keyBuilder.append(", ").append(버전_아이디_이름_맵.get(버전_아이디_배열[i]));
                        }
                    }
                    String 버전_명_문자열 = keyBuilder.toString();
                    log.info("[ScopeServiceImple  :: 버전_요구사항_상태] :: 만들어진 버전_명_문자열 ==> {}", 버전_명_문자열);
                    // Map에 해당하는 값을 증가시킵니다.
                    버전세트문자열_버전명_맵.putIfAbsent(버전세트_문자열,버전_명_문자열);
                }
                dto.setVersion_name(버전세트문자열_버전명_맵.get(버전세트_문자열));
                결과_목록.add(dto);
            }

        }
        SessionUtil.removeAttribute("getReqAddListByFilter");
        return 결과_목록;
    }

    @Override
    public List<TreeBarDTO> treeBar(AggregationRequestDTO aggregationRequestDTO) throws Exception {

        Long pdServiceLink = aggregationRequestDTO.getPdServiceLink();

        List<TreeBarDTO> treeBarList = new ArrayList<>();

        // 1. 제품 조회
        PdServiceEntity product = TreeServiceUtils.getNode(pdService, pdServiceLink, PdServiceEntity.class);

        if (product == null) {
            log.info("ScopeServiceImpl :: treebar :: product is null");
            return treeBarList;
        }

        // 2. 제품 등록
        treeBarList.add(addProduct(product));

        // 3. 제품 버전 조회
        List<PdServiceVersionEntity> productVersions = product.getPdServiceVersionEntities().stream().filter(
                pdServiceVersionEntity -> aggregationRequestDTO.getPdServiceVersionLinks().contains(pdServiceVersionEntity.getC_id())
        ).sorted(Comparator.comparing(PdServiceVersionEntity::getC_id)).collect(Collectors.toList());

        if (productVersions.isEmpty()) {
            log.info("ScopeServiceImpl :: treebar :: productVersions is empty");
            return treeBarList;
        }

        // 4. 엔진 통신
        ResponseEntity<검색결과_목록_메인> 외부API응답 = 통계엔진통신기.제품_혹은_제품버전들의_집계_flat(aggregationRequestDTO);

        검색결과_목록_메인 검색결과목록메인 = 외부API응답.getBody();

        if (검색결과목록메인 == null) {
            log.info("ScopeServiceImpl :: treebar :: 검색결과목록메인 is null");
            return treeBarList;
        }

        Map<String, List<검색결과>> searchResultMap = 검색결과목록메인.get검색결과();

        List<검색결과> searchResultList = searchResultMap.get("group_by_cReqLink");

        // 5. 가장 많은 하위 이슈를 가진 요구사항 top 10 추출
        List<검색결과> top10 = searchResultList.stream().sorted(Comparator.comparing(검색결과::get개수).reversed()).limit(10).collect(Collectors.toList());

        // 6. REQADD.cid 리스트 추출
        List<Long> cReqLinks = top10.stream().map(검색결과::get필드명).map(Long::parseLong).collect(Collectors.toList());

        // 7. cReqLinks 값을 이용하여 요구사항(REQADD) 10개 조회
        ResponseEntity<List<LoadReqAddDTO>> 요구사항목록조회 = 내부통신기.요구사항목록조회("T_ARMS_REQADD_" + pdServiceLink, cReqLinks);

        List<LoadReqAddDTO> loadReqAddDTOList = 요구사항목록조회.getBody();

        Set<Long> reqAddCidSet = loadReqAddDTOList.stream().map(LoadReqAddDTO::getC_id).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(loadReqAddDTOList)) {
            log.info("ScopeServiceImpl :: treebar :: loadReqAddDTOList is empty");
            return treeBarList;
        }

        // 8. 요구사항을 TreeBarDTO 로 변환
        List<TreeBarDTO> requirements = loadReqAddDTOList.stream().map(TreeBarDTO::new).collect(Collectors.toList());

        // 9. 제품 버전 등록
        List<TreeBarDTO> versionList = addProductVersions(product, productVersions, requirements);
        treeBarList.addAll(versionList);

        // 10. 요구사항 등록
        treeBarList.addAll(requirements);

        // 11. 담당자 등록
        treeBarList.addAll(addAssignees(top10,reqAddCidSet));
        return treeBarList;
    }

    private List<TreeBarDTO> addAssignees(List<검색결과> top10Requirements, Set<Long> reqAddCidSet) {
        Map<String, String> assigneeToColorMap = new HashMap<>();

        return top10Requirements.stream()
                .filter(cReqLink -> reqAddCidSet.contains(Long.valueOf(cReqLink.get필드명())))
                .flatMap(cReqLink -> {
                    String parent = "requirement-" + cReqLink.get필드명();
                    return cReqLink.get하위검색결과().get("group_by_assignee.assignee_displayName.keyword").stream()
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
}
