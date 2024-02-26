package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.샘플연봉데이터;
import com.arms.api.analysis.cost.dto.연봉데이터;
import com.arms.api.analysis.cost.dto.연봉엔티티;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("연봉서비스")
public class 연봉서비스_구현 extends TreeServiceImpl implements 연봉서비스 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected ModelMapper modelMapper;

    @Override
    public List<샘플연봉데이터> 샘플연봉정보() {

        List<샘플연봉데이터> 샘플데이터 = new ArrayList<>();

        AtomicInteger 번호 = new AtomicInteger(1);
        샘플데이터.add(new 샘플연봉데이터(String.valueOf(번호.getAndIncrement()), "이름", "지라에 설정된 사용자 이름 입니다.", "수정 X"));
        샘플데이터.add(new 샘플연봉데이터(String.valueOf(번호.getAndIncrement()), "고유 키", "지라에 설정된 고유한 사용자의 아이디 입니다.", "수정 X"));
        샘플데이터.add(new 샘플연봉데이터(String.valueOf(번호.getAndIncrement()), "연봉", "사용자의 연봉 정보입니다. (단위: 만원)", "수정 O"));

        return 샘플데이터;
    }

    @Override
    @Transactional
    public List<연봉엔티티> 연봉정보비교(List<연봉데이터> 연봉데이터) throws Exception {

        로그.info(" [ " + this.getClass().getName() + " :: 연봉정보비교 ]");

        // 테스트용으로 데이터 추가
        /*연봉엔티티 연봉테스트 = new 연봉엔티티();
        연봉테스트.setC_name("admin");
        연봉테스트.setC_key("admin");
        연봉테스트.setC_annual_income("8000");
        연봉테스트.setRef(TreeConstant.First_Node_CID);
        연봉테스트.setC_type(TreeConstant.Leaf_Node_TYPE);
        this.addNode(연봉테스트);*/

        // DB에 저장된 데이터 조회
        연봉엔티티 연봉엔티티 = new 연봉엔티티();
        List<연봉엔티티> DB_데이터 = this.getNodesWithoutRoot(연봉엔티티);

        // DB 데이터와 받은 데이터 비교
        if (DB_데이터.size() != 0) {
            Map<String, 연봉엔티티> DB_맵 = DB_데이터.stream()
                    .collect(Collectors.toMap(엔티티 -> 엔티티.getC_name() + 엔티티.getC_key(), 엔티티 -> 엔티티));

            연봉데이터.stream()
                    .filter(데이터 -> DB_맵.containsKey(데이터.getC_name() + 데이터.getC_key()))
                    .forEach(데이터 -> 데이터.setC_annual_income(DB_맵.get(데이터.getC_name() + 데이터.getC_key()).getC_annual_income())); // DB에 저장된 값으로 세팅
        } else {
            로그.info(" [ " + this.getClass().getName() + " :: 연봉정보비교 ] :: DB에 저장된 데이터가 없음");
        }

        // 엔티티로 변환
        List<연봉엔티티> 연봉엔티티리스트 = 연봉데이터.stream()
                .map(데이터 -> {
                    연봉엔티티 엔티티 = new 연봉엔티티();
                    엔티티.setC_name(데이터.getC_name());
                    엔티티.setC_key(데이터.getC_key());
                    엔티티.setC_annual_income(데이터.getC_annual_income());
                    return 엔티티;
                })
                .collect(Collectors.toList());

        // json으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(연봉데이터);
        로그.info(" [ " + this.getClass().getName() + " :: 연봉정보비교 ] :: 연봉 데이터 -> " + json);

        return 연봉엔티티리스트;

    }

}