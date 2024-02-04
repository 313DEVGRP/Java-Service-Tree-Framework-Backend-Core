package com.arms.api.analysis.cost.controller;

import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.analysis.cost.service.비용서비스;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/arms/analysis/cost")
public class 비용분석_컨트롤러 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 비용서비스 비용서비스;

    /**
     * 버전별 요구사항별 담당자 조회 API
     */
    @GetMapping("/version-req-assignees")
    public ResponseEntity<CommonResponse.ApiResult<버전요구사항별_담당자데이터>> 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청)  {
        로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        로그.info(지라이슈_제품_및_제품버전_검색요청.toString());

        버전요구사항별_담당자데이터 결과 = 비용서비스.버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청);

        return ResponseEntity.ok(CommonResponse.success(결과));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/req-difficulty-priority-list"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 요구사항목록_난이도_및_우선순위통계_가져오기(
                        @PathVariable(value = "changeReqTableName") String changeReqTableName,
                        ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        로그.info("[ ReqAddController :: 요구사항목록_난이도_및_우선순위통계_가져오기 ]");

        SessionUtil.setAttribute("req-difficulty-priority-list", changeReqTableName);

        요구사항목록_난이도_및_우선순위통계데이터 조회결과 = 비용서비스.요구사항목록_난이도_및_우선순위통계_가져오기(reqAddDTO);

        SessionUtil.removeAttribute("req-difficulty-priority-list");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 조회결과);
        return modelAndView;
    }
}
