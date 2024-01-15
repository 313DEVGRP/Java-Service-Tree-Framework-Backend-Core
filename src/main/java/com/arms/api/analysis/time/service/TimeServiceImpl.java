package com.arms.api.analysis.time.service;

import com.arms.api.analysis.time.model.등고선데이터;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.external_communicate.dto.요구사항_별_업데이트_데이터;
import com.arms.api.util.external_communicate.dto.지라이슈;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeServiceImpl implements TimeService{
    @Autowired
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    private com.arms.api.util.external_communicate.내부통신기 내부통신기;

    @Override
    public Map<String, String> getReqIssueList(Long service_id){

        ReqStatusDTO reqStatusDTO = new ReqStatusDTO();

        List<ReqStatusEntity> 결과 = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + service_id, reqStatusDTO);

        Map<String, String> 요구사항맵 = 결과.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ReqStatusEntity::getC_issue_key,
                        ReqStatusEntity::getC_title,
                        (title1, title2) -> title1   ));

        return 요구사항맵;
    }
    @Override
    public List<등고선데이터> 등고선데이터_변환(Map<Long, Map<String, Map<String,List<요구사항_별_업데이트_데이터>>>> 검색일자_범위_데이터, Map<String, String> 요구사항목록){
        List<등고선데이터> result = 검색일자_범위_데이터.entrySet().stream()
                .flatMap(versions -> {
                    Long version = versions.getKey();
                    return versions.getValue().entrySet().stream()
                            .flatMap(dates -> {
                                return dates.getValue().entrySet().stream()
                                        .map(nameEntry -> {
                                            String name = nameEntry.getKey();
                                            int value = nameEntry.getValue().size();
                                            String summary = 요구사항목록.get(nameEntry.getKey());
                                            return new 등고선데이터(version, dates.getKey(), name, value,summary);
                                        });
                            });
                })
                .collect(Collectors.toList());

        // 결과가 하나일 경우 앞 뒤로 날짜를 추가하고 value를 0으로 설정
        if (result.size() == 1) {
            등고선데이터 singleResult = result.get(0);
            String dateString = singleResult.getDate();

            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            LocalDate previousDay = date.minusDays(1);
            LocalDate nextDay = date.plusDays(1);

            String previousDayString = previousDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nextDayString = nextDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            result.add(0, new 등고선데이터(singleResult.getVersion(), previousDayString, singleResult.getName(), 0, singleResult.getSummary()));
            result.add(new 등고선데이터(singleResult.getVersion(), nextDayString, singleResult.getName(), 0, singleResult.getSummary()));
        }

        return result;
    }
}
