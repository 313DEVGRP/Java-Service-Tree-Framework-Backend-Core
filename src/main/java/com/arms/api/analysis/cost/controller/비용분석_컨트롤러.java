package com.arms.api.analysis.cost.controller;

import com.arms.api.analysis.cost.service.비용서비스;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/arms/analysis/cost")
public class 비용분석_컨트롤러 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    private final 비용서비스 비용서비스;

    /**
     * 버전별 요구사항별 담당자 조회 API
     */
    @GetMapping("/version-req-assignees")
    public ResponseEntity<CommonResponse.ApiResult<List<검색결과>>> 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청)  {
        로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        로그.info(지라이슈_제품_및_제품버전_검색요청.toString());

        List<검색결과> 결과 = 비용서비스.버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청);

        return ResponseEntity.ok(CommonResponse.success(결과));
    }
}
