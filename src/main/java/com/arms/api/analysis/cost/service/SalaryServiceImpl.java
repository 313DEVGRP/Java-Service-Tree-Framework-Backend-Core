package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.model.SampleDTO;
import com.arms.api.analysis.cost.model.SalaryDTO;
import com.arms.api.analysis.cost.model.SalaryEntity;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SalaryServiceImpl extends TreeServiceImpl implements SalaryService {
    @Override
    public List<SampleDTO> 샘플연봉정보() {

        List<SampleDTO> 샘플데이터 = new ArrayList<>();

        AtomicInteger 번호 = new AtomicInteger(1);
        샘플데이터.add(new SampleDTO(String.valueOf(번호.getAndIncrement()), "이름", "ALM(Jira, Redmine, GitLab) 에 설정된 사용자 이름 입니다.", "ex) moon", "최초 등록 후 수정 불가"));
        샘플데이터.add(new SampleDTO(String.valueOf(번호.getAndIncrement()), "고유 키", "ALM(Jira, Redmine, GitLab) 에 설정된 고유한 사용자의 아이디 입니다.", "ex) 6265535bfff19d0069259584", "최초 등록 후 수정 불가"));
        샘플데이터.add(new SampleDTO(String.valueOf(번호.getAndIncrement()), "연봉", "사용자의 연봉 정보입니다. (단위: 만원)", "ex) 5000", "최초 등록 후 수정 가능"));

        return 샘플데이터;
    }

    @Override
    @Transactional
    public List<SalaryEntity> 연봉정보비교(List<SalaryDTO> SalaryDTO) throws Exception {

        log.info(" [ " + this.getClass().getName() + " :: 연봉정보비교 ]");

        // 테스트용으로 데이터 추가
        /*연봉엔티티 연봉테스트 = new 연봉엔티티();
        연봉테스트.setC_name("admin");
        연봉테스트.setC_key("admin");
        연봉테스트.setC_annual_income("8000");
        연봉테스트.setRef(TreeConstant.First_Node_CID);
        연봉테스트.setC_type(TreeConstant.Leaf_Node_TYPE);
        this.addNode(연봉테스트);*/

        // DB에 저장된 데이터 조회
        SalaryEntity SalaryEntity = new SalaryEntity();
        List<SalaryEntity> DB_데이터 = this.getNodesWithoutRoot(SalaryEntity);

        // DB 데이터와 받은 데이터 비교
        if (DB_데이터.size() != 0) {
            Map<String, SalaryEntity> DB_맵 = DB_데이터.stream()
                    .collect(Collectors.toMap(엔티티 -> 엔티티.getC_name() + 엔티티.getC_key(), 엔티티 -> 엔티티));

            SalaryDTO.stream()
                    .filter(데이터 -> DB_맵.containsKey(데이터.getC_name() + 데이터.getC_key()))
                    .forEach(데이터 -> 데이터.setC_annual_income(DB_맵.get(데이터.getC_name() + 데이터.getC_key()).getC_annual_income())); // DB에 저장된 값으로 세팅
        } else {
            log.info(" [ " + this.getClass().getName() + " :: 연봉정보비교 ] :: DB에 저장된 데이터가 없음");
        }

        // 엔티티로 변환
        List<SalaryEntity> 연봉엔티티리스트 = SalaryDTO.stream()
                .map(데이터 -> {
                    SalaryEntity 엔티티 = new SalaryEntity();
                    엔티티.setC_name(데이터.getC_name());
                    엔티티.setC_key(데이터.getC_key());
                    엔티티.setC_annual_income(데이터.getC_annual_income());
                    return 엔티티;
                })
                .collect(Collectors.toList());

        // json으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(SalaryDTO);
        log.info(" [ " + this.getClass().getName() + " :: 연봉정보비교 ] :: 연봉 데이터 -> " + json);

        return 연봉엔티티리스트;

    }

    @Override
    @Transactional
    public List<SalaryDTO> 엑셀데이터_DB저장(List<SalaryEntity> 엑셀데이터) throws Exception {

        SalaryEntity SalaryEntity = new SalaryEntity();
        List<SalaryEntity> DB_데이터 = this.getNodesWithoutRoot(SalaryEntity);

        List<SalaryEntity> 엑셀_데이터 = 엑셀데이터.stream()
                .filter(엔티티 -> {
                    String 이름 = 엔티티.getC_name();
                    String 키 = 엔티티.getC_key();
                    // 이름과 키 모두 값이 있어야 함
                    return 이름 != null && !이름.isEmpty() && 키 != null && !키.isEmpty();
                }).collect(Collectors.toList());

        // DB 데이터와 비교해서 없으면 insert, 있으면 update
        if (DB_데이터.size() != 0) {
            // DB 데이터와 비교
            Map<String, SalaryEntity> DB_맵 = DB_데이터.stream()
                    .collect(Collectors.toMap(엔티티 -> 엔티티.getC_name() + 엔티티.getC_key(), 엔티티 -> 엔티티));

            Map<String, SalaryEntity> 엑셀_맵 = 엑셀_데이터.stream()
                    .map(엔티티 -> {
                        String 연봉 = 엔티티.getC_annual_income();
                        // 연봉 값이 없거나 빈 문자열인 경우 0으로 설정
                        if (연봉 == null || 연봉.isEmpty()) {
                            엔티티.setC_annual_income("0");
                        }
                        return 엔티티;
                    })
                    .collect(Collectors.toMap(엔티티 -> 엔티티.getC_name() + 엔티티.getC_key(), 엔티티 -> 엔티티));

            for (Map.Entry<String, SalaryEntity> entry : 엑셀_맵.entrySet()) {
                String 키 = entry.getKey();
                SalaryEntity 값 = entry.getValue();

                // DB_맵에 같은 키의 엔티티가 있는 경우, 그 엔티티의 연봉을 업데이트
                if (DB_맵.containsKey(키)) {
                    SalaryEntity DB_엔티티 = DB_맵.get(키);
                    if (!DB_엔티티.getC_annual_income().equals(값.getC_annual_income())) {
                        log.info(" [ " + this.getClass().getName() + " :: 엑셀데이터_DB저장 ] :: 업데이트 -> " + 키);
                        SalaryEntity 업데이트_엔티티 = new SalaryEntity();
                        업데이트_엔티티.setC_id(DB_엔티티.getC_id());
                        업데이트_엔티티.setC_name(DB_엔티티.getC_name());
                        업데이트_엔티티.setC_key(DB_엔티티.getC_key());
                        업데이트_엔티티.setC_annual_income(값.getC_annual_income());
                        this.updateNode(업데이트_엔티티);
                        DB_엔티티.setC_annual_income(값.getC_annual_income());
                    }
                }
                // DB_맵에 같은 키의 엔티티가 없는 경우, 엑셀_엔티티를 DB_맵에 추가
                else {
                    log.info(" [ " + this.getClass().getName() + " :: 엑셀데이터_DB저장 ] :: 저장 -> " + 키);
                    this.addNode(new SalaryEntity(값.getC_name(), 값.getC_key(), 값.getC_annual_income()));
                    DB_맵.put(키, 값);
                }
            }

            // 변환
            List<SalaryDTO> 결과리스트 = DB_맵.values().stream()
                    .map(엔티티 -> {
                        SalaryDTO 데이터 = new SalaryDTO();
                        데이터.setC_name(엔티티.getC_name());
                        데이터.setC_key(엔티티.getC_key());
                        데이터.setC_annual_income(엔티티.getC_annual_income());
                        return 데이터;
                    })
                    .collect(Collectors.toList());

            return 결과리스트;

        } else {
            // DB에 처음 저장하는 경우
            for (SalaryEntity 연봉정보 : 엑셀_데이터) {
                log.info(" [ " + this.getClass().getName() + " :: 엑셀데이터_DB저장 ] :: 처음 저장 -> " + 연봉정보.getC_name() + 연봉정보.getC_key());
                this.addNode(new SalaryEntity(연봉정보.getC_name(), 연봉정보.getC_key(), 연봉정보.getC_annual_income()));
            }
        }

        // 변환
        List<SalaryDTO> 결과리스트 = 엑셀_데이터.stream()
                .map(엔티티 -> {
                    SalaryDTO 데이터 = new SalaryDTO();
                    데이터.setC_name(엔티티.getC_name());
                    데이터.setC_key(엔티티.getC_key());
                    데이터.setC_annual_income(엔티티.getC_annual_income());
                    return 데이터;
                })
                .collect(Collectors.toList());

        return 결과리스트;
    }

    @Override
    public Map<String, SalaryEntity> 모든_연봉정보_맵() throws Exception {

        SalaryEntity 엔티티 = new SalaryEntity();
        Function<SalaryEntity, String> 키 = SalaryEntity::getC_key;
        Function<SalaryEntity, SalaryEntity> 값 = Function.identity();
        return this.getNodesWithoutRootMap(엔티티, 키, 값);
    }

    @Override
    @Transactional
    public int updateSalary(List<SalaryEntity> salaryEntityList) throws Exception {
        Map<String, SalaryEntity> 모든_연봉정보_맵 = this.모든_연봉정보_맵();
        for (SalaryEntity salaryEntity : salaryEntityList) {
            SalaryEntity salary = 모든_연봉정보_맵.get(salaryEntity.getC_key());
            if (salary == null) {
                salaryEntity.setRef(TreeConstant.First_Node_CID);
                salaryEntity.setC_type(TreeConstant.Leaf_Node_TYPE);
                this.addNode(salaryEntity);
            } else {
                salary.setC_annual_income(salaryEntity.getC_annual_income());
                this.updateNode(salary);
            }
        }
        return 1;
    }
}