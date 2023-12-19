package com.arms.analysis.scope.controller;

import com.arms.analysis.scope.dto.요구사항_별_상태_및_유일_작업자_수;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.arms.util.external_communicate.통계엔진통신기;
import com.arms.util.external_communicate.dto.search.검색결과;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_단순_검색_요청;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping(value = "/arms/analysis/scope")
public class 스코프분석_컨트롤러 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 통계엔진통신기 통계엔진통신기;

    static final long dummy_jira_server = 0L;

    @GetMapping("/getReqPerVersion/{pdServiceId}")
    public ResponseEntity<검색결과_목록_메인> 버전들_하위_요구사항_연결이슈_집계(@PathVariable("pdServiceId") Long pdServiceId,
                                                     @RequestParam List<Long> pdServiceVersionLinks,
                                                     지라이슈_단순_검색_요청 검색요청_데이터) {
        log.info("스코프분석_컨트롤러 :: 버전들_하위_요구사항_연결이슈_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceId.toString(), pdServiceVersionLinks.toString());
        ResponseEntity<검색결과_목록_메인> 집계결과 = 통계엔진통신기.일반_버전필터_검색(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        return ResponseEntity.ok(집계결과.getBody());

    }

    @GetMapping("/req-status-and-reqInvolved-unique-assignees")
    public  ResponseEntity<List<요구사항_별_상태_및_유일_작업자_수>> 요구사항_별_상태_및_관여_작업자_수(
            지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {

        log.info("[ 스코프분석_컨트롤러 :: 요구사항_별_상태_및_관여_작업자_수 ] :: 지라이슈_제품_및_제품버전_검색요청.pdServiceLink ==> {}, pdServiceVersionLinks ==> {}",
                지라이슈_제품_및_제품버전_검색요청.getPdServiceLink(), 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks().toString());

        ResponseEntity<List<요구사항_별_상태_및_유일_작업자_수>> 통신결과 = 통계엔진통신기.요구사항_별_상태_및_관여_작업자_수(지라이슈_제품_및_제품버전_검색요청);

        return ResponseEntity.ok(통신결과.getBody());
    }

}
