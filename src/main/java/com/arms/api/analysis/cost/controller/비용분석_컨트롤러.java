package com.arms.api.analysis.cost.controller;

import com.arms.api.analysis.cost.dto.*;
import com.arms.api.analysis.cost.service.비용서비스;
import com.arms.api.analysis.cost.service.연봉서비스;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.util.API호출변수;
import com.arms.api.util.external_communicate.dto.지라이슈_일반_집계_요청;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.arms.api.util.communicate.external.통계엔진통신기;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private 연봉서비스 연봉서비스;

    @Autowired
    통계엔진통신기 통계엔진통신기;

    @GetMapping("/all-assignees")
    public ResponseEntity<CommonResponse.ApiResult<버전요구사항별_담당자데이터>> 전체_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청)  {

        long 시작시간 = System.currentTimeMillis();

        로그.info(" [ " + this.getClass().getName() + " :: 전체_담당자가져오기 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        로그.info(지라이슈_제품_및_제품버전_검색요청.toString());

        Long 제품아이디 = 지라이슈_제품_및_제품버전_검색요청.getPdServiceLink();
        if (제품아이디 == null) {
            return null;
        }

        List<Long> 버전아이디_목록 = 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks();
        if (버전아이디_목록 == null) {
            return null;
        }

        String 하위그룹필드 = API호출변수.담당자이름집계;
        지라이슈_일반_집계_요청 일반_집계_요청_세팅 = 지라이슈_일반_집계_요청.builder()
                .메인그룹필드(API호출변수.담당자아이디집계)
                .컨텐츠보기여부(true)
                .크기(1000)
                .하위그룹필드들(Arrays.stream(하위그룹필드.split(",")).collect(Collectors.toList()))
                .build();

        버전요구사항별_담당자데이터 결과 = 비용서비스.전체_담당자가져오기(제품아이디, 버전아이디_목록, 일반_집계_요청_세팅);

        long 종료시간 = System.currentTimeMillis();

        long 걸린시간 = 종료시간 - 시작시간;
        로그.info("API 호출이 걸린 시간: " + 걸린시간 + "밀리초");

        return ResponseEntity.ok(CommonResponse.success(결과));
    }

    /**
     * 버전별 요구사항별 담당자 조회 API
     */
    @GetMapping("/version-req-assignees")
    public ResponseEntity<CommonResponse.ApiResult<버전요구사항별_담당자데이터>> 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청)  {
        long 시작시간 = System.currentTimeMillis();

        로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        로그.info(지라이슈_제품_및_제품버전_검색요청.toString());

        버전요구사항별_담당자데이터 결과 = 비용서비스.버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청);

        long 종료시간 = System.currentTimeMillis();

        long 걸린시간 = 종료시간 - 시작시간;
        로그.info("API 호출이 걸린 시간: " + 걸린시간 + "밀리초");
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
    public ModelAndView 버전별_요구사항_연결된_지라이슈키(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {
        로그.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항_연결된_지라이슈키 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        로그.info(지라이슈_제품_및_제품버전_검색요청.toString());
        버전별_요구사항별_연결된_지라이슈데이터 검색결과 = 비용서비스.버전별_요구사항_연결된_지라이슈키(지라이슈_제품_및_제품버전_검색요청);
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
    public void 연봉입력_엑셀템플릿_다운로드(@RequestBody List<연봉데이터> 연봉데이터리스트,
                                @RequestParam("excelFileName") String excelFileName,
                                HttpServletResponse httpServletResponse) throws Exception {

        로그.info(" [ " + this.getClass().getName() + " :: 연봉입력_엑셀템플릿_다운로드 ]");

        httpServletResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFileName);
        httpServletResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpServletResponse.addHeader("Pragma", "no-cache");
        httpServletResponse.addHeader("Expires", "0");
        httpServletResponse.setContentType("application/octet-stream");

        ExcelUtilsBase excelUtilsBase = ExcelUtilsFactory.getInstance(httpServletResponse.getOutputStream());
        List<샘플연봉데이터> 샘플데이터 = 연봉서비스.샘플연봉정보();
        List<연봉엔티티> 비교한_연봉리스트 = 연봉서비스.연봉정보비교(연봉데이터리스트);
        excelUtilsBase.create(List.of(샘플데이터, 비교한_연봉리스트));

    }

    /**
     * 연봉 입력을 위한 엑셀 템플릿 업로드 API
     */
    @ApiOperation(value = "연봉 입력 엑셀 템플릿 업로드")
    @ResponseBody
    @RequestMapping(value = "/excel-upload.do", method = RequestMethod.POST)
    public ModelAndView 연봉입력_엑셀템플릿_업로드(@RequestPart("excelFile") MultipartFile excelFile, HttpServletRequest httpServletRequest) throws Exception {

        로그.info(" [ " + this.getClass().getName() + " :: 연봉입력_엑셀템플릿_업로드 ]");

        ExcelUtilsBase excelUtilsBase = ExcelUtilsFactory.getInstance(excelFile.getInputStream());
        List<연봉엔티티> 업로드한_연봉리스트 = excelUtilsBase.read(연봉엔티티.class);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 연봉서비스.엑셀데이터_DB저장(업로드한_연봉리스트));
        return modelAndView;

    }

    /*
    *  해당 요구사항 지라키로 업데이트 이력 조회 API
    * */
    @ResponseBody
    @RequestMapping(
            value = {"/req-updated-list"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 요구사항_지라이슈키별_업데이트_목록(@RequestParam List<String> issueList) throws Exception {
        로그.info(" [ " + this.getClass().getName() + " :: 요구사항_지라이슈키별_업데이트_목록 ] :: 요구사항_지라이슈키_목록 -> ");
        로그.info(issueList.toString());

        ResponseEntity<Map<String,List<요구사항_지라이슈키별_업데이트_목록_데이터>>> 검색결과= 통계엔진통신기.요구사항_지라이슈키별_업데이트_목록(issueList);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 검색결과);
        return modelAndView;
    }

}
