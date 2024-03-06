package com.arms.api.analysis.time.service;

import com.arms.api.analysis.time.model.등고선데이터;
import com.arms.api.analysis.time.model.일자별_요구사항_연결된이슈_생성개수_및_상태데이터;
import com.arms.api.analysis.time.model.히트맵데이터;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.analysis.common.IsReqType;
import com.arms.api.util.communicate.external.request.aggregation.EngineAggregationRequestDTO;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일자별_제품_및_제품버전_검색요청;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.communicate.internal.내부통신기;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.api.util.communicate.external.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
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
    private 내부통신기 내부통신기;

    @Autowired
    private 엔진통신기 엔진통신기;

    @Autowired
    private 통계엔진통신기 통계엔진통신기;

    @Override
    public List<지라이슈> 제품서비스_버전목록으로_조회(Long dummy_jira_server, Long pdServiceLink, List<Long> pdServiceVersionLinks) {
        List<지라이슈> result = 엔진통신기.제품서비스_버전목록으로_조회(dummy_jira_server, pdServiceLink, pdServiceVersionLinks);
        return result;
    }

    @Override
    public 히트맵데이터 히트맵_제품서비스_버전목록으로_조회(Long dummy_jira_server, Long pdServiceLink, List<Long> pdServiceVersionLinks) {
        히트맵데이터 result = 엔진통신기.히트맵_제품서비스_버전목록으로_조회(dummy_jira_server, pdServiceLink, pdServiceVersionLinks);
        return result;
    }

    @Override
    public 검색결과_목록_메인 제품서비스_일반_버전_해결책유무_통계(EngineAggregationRequestDTO engineAggregationRequestDTO, String resolution) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_버전_해결책통계  =
                통계엔진통신기.제품서비스_일반_버전_해결책유무_통계(engineAggregationRequestDTO, resolution);

        검색결과_목록_메인 통계결과 = 요구사항_연결이슈_일반_버전_해결책통계.getBody();

        return 통계결과;
    }

    @Override
    public Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터> 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) {
        ResponseEntity<Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터>> result =
                통계엔진통신기.기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청);

        return result.getBody();
    }

    @Override
    public Map<Long, List<지라이슈>> 기준일자별_제품_및_제품버전목록_업데이트된_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) {
        ResponseEntity<List<지라이슈>> 검색일자_범위_데이터 = 통계엔진통신기.기준일자별_제품_및_제품버전목록_업데이트된_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청);

        Map<Long, List<지라이슈>> 버전별_그룹화_결과 = Optional.ofNullable(검색일자_범위_데이터.getBody())
                .orElseGet(Collections::emptyList)
                .stream()
                .flatMap(issue->issue.지라버전별로_분해가져오기().stream())
                .collect(Collectors.groupingBy(지라이슈::getPdServiceVersion));

        return 버전별_그룹화_결과;
    }


    @Override
    public List<등고선데이터> 기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) {
        ResponseEntity<Map<Long, Map<String, Map<String,List<지라이슈>>>>> 결과 = 통계엔진통신기.기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청);

        Map<Long, Map<String, Map<String,List<지라이슈>>>> 검색일자_범위_데이터 = 결과.getBody();
        Long service_id = 지라이슈_일자별_제품_및_제품버전_검색요청.getPdServiceLink();

        Map<String, String>  요구사항리스트 = this.getReqIssueList(service_id);

        if(지라이슈_일자별_제품_및_제품버전_검색요청.getIsReqType() == IsReqType.REQUIREMENT){ // 요구사항 업데이트 수 검색했을 경우
            List<등고선데이터> result = this.등고선데이터_변환(검색일자_범위_데이터, 요구사항리스트);
            return result;
        } else if (지라이슈_일자별_제품_및_제품버전_검색요청.getIsReqType()  == IsReqType.ISSUE) { // 연관된 이슈들만 검색했을 경우
            List<등고선데이터> result = this.등고선데이터_변환(검색일자_범위_데이터, 요구사항리스트);
            return result;
        }

        return null;
    }

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

    public List<등고선데이터> 등고선데이터_변환(Map<Long, Map<String, Map<String,List<지라이슈>>>> 검색일자_범위_데이터, Map<String, String> 요구사항목록){
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
