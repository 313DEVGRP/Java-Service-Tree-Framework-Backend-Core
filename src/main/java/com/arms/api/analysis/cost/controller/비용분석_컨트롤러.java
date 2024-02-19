package com.arms.api.analysis.cost.controller;

import com.arms.api.analysis.cost.dto.버전별_요구사항별_연결된지_지라이슈데이터;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.analysis.cost.dto.인력별_연봉데이터;
import com.arms.api.analysis.cost.service.비용서비스;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.util.external_communicate.dto.지라이슈;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.external_communicate.통계엔진통신기;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelUtilsBase;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelUtilsFactory;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/arms/analysis/cost")
public class 비용분석_컨트롤러 {

    private final Logger 로그 = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 비용서비스 비용서비스;

    @Autowired
    통계엔진통신기 통계엔진통신기;

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

    /**
     * 버전별 요구사항별 연결된 지라 지라 이슈 아이디 조회 API (버블 차트)
     */
    @ResponseBody
    @RequestMapping(
            value = {"/req-linked-issue"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 버전별_요구사항에_연결된지_지라이슈(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {
        로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항에_연결된지_지라이슈 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        로그.info(지라이슈_제품_및_제품버전_검색요청.toString());
        버전별_요구사항별_연결된지_지라이슈데이터 검색결과 = 비용서비스.버전별_요구사항에_연결된지_지라이슈(지라이슈_제품_및_제품버전_검색요청);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 검색결과);
        return modelAndView;
    }

    /**
     * 연봉 입력을 위한 엑셀 템플릿 다운로드 API
     */
    @ApiOperation(value = "연봉 입력 엑셀 템플릿 다운로드")
    @ResponseBody
    @RequestMapping(value = "/excel-download.do", method = RequestMethod.POST)
    public void 연봉입력_엑셀템플릿_다운로드(@RequestBody List<인력별_연봉데이터> 인력별_연봉데이터_리스트,
                                @RequestParam("excelFileName") String excelFileName,
                                HttpServletResponse httpServletResponse) throws Exception {

        httpServletResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFileName);
        httpServletResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpServletResponse.addHeader("Pragma", "no-cache");
        httpServletResponse.addHeader("Expires", "0");
        httpServletResponse.setContentType("application/octet-stream");

        ExcelUtilsBase excelUtilsBase = ExcelUtilsFactory.getInstance(httpServletResponse.getOutputStream());
        excelUtilsBase.create(List.of(인력별_연봉데이터_리스트));

    }


    /*
    *  해당 요구사항 지라키로 업데이트 이력 조회 API
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/req-updated-list"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 요구사항_지라이슈키별_업데이트_목록(@RequestParam List<String> 요구사항_지라키_목록) throws Exception {
        로그.info(" [ " + this.getClass().getName() + " :: 요구사항_지라이슈키별_업데이트_목록 ] :: 요구사항_지라이슈키_목록 -> ");
        로그.info(요구사항_지라키_목록.toString());

        ResponseEntity<Map<String,List<지라이슈>>> 검색결과= 통계엔진통신기.요구사항_지라이슈키별_업데이트_목록(요구사항_지라키_목록);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 검색결과);
        return modelAndView;
    }

}
