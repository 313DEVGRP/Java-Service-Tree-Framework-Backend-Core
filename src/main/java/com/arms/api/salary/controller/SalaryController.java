package com.arms.api.salary.controller;

import com.arms.api.salary.model.SalaryDTO;
import com.arms.api.salary.model.SalaryEntity;
import com.arms.api.salary.model.SampleDTO;
import com.arms.api.salary.service.SalaryService;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.errors.response.ErrorCode;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelUtilsBase;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelUtilsFactory;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/arms/salaries")
@RequiredArgsConstructor
public class SalaryController extends TreeAbstractController<SalaryService, SalaryDTO, SalaryEntity> {

    private final SalaryService salaryService;

    @PostConstruct
    public void initialize() {
        setTreeService(salaryService);
        setTreeEntity(SalaryEntity.class);
    }

    /**
     * 연봉 입력을 위한 엑셀 템플릿 다운로드 API
     */
    @ApiOperation(value = "연봉 입력 엑셀 템플릿 다운로드")
    @PostMapping("/excel-download.do")
    public void 연봉입력_엑셀템플릿_다운로드(@RequestBody List<SalaryDTO> 연봉데이터리스트,
                                @RequestParam("excelFileName") String excelFileName,
                                HttpServletResponse httpServletResponse) throws Exception {

        log.info(" [ " + this.getClass().getName() + " :: 연봉입력_엑셀템플릿_다운로드 ]");

        httpServletResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + excelFileName);
        httpServletResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpServletResponse.addHeader("Pragma", "no-cache");
        httpServletResponse.addHeader("Expires", "0");
        httpServletResponse.setContentType("application/octet-stream");

        ExcelUtilsBase excelUtilsBase = ExcelUtilsFactory.getInstance(httpServletResponse.getOutputStream());
        List<SampleDTO> 샘플데이터 = salaryService.샘플연봉정보();
        List<SalaryEntity> 비교한_연봉리스트 = salaryService.연봉정보비교(연봉데이터리스트);
        excelUtilsBase.create(List.of(샘플데이터, 비교한_연봉리스트));

    }

    /**
     * 연봉 입력을 위한 엑셀 템플릿 업로드 API
     */
    @ApiOperation(value = "연봉 입력 엑셀 템플릿 업로드")
    @PostMapping("/excel-upload.do")
    public ModelAndView 연봉입력_엑셀템플릿_업로드(@RequestPart("excelFile") MultipartFile excelFile, HttpServletRequest httpServletRequest) throws Exception {

        log.info(" [ " + this.getClass().getName() + " :: 연봉입력_엑셀템플릿_업로드 ]");

        ExcelUtilsBase excelUtilsBase = ExcelUtilsFactory.getInstance(excelFile.getInputStream());
        List<SalaryEntity> 업로드한_연봉리스트 = excelUtilsBase.read(SalaryEntity.class);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", salaryService.엑셀데이터_DB저장(업로드한_연봉리스트));
        return modelAndView;

    }

    @PutMapping("/update.do")
    public ResponseEntity<CommonResponse.ApiResult<Integer>> 연봉정보수정(SalaryDTO salaryDTO) throws Exception {
        Map<String, SalaryEntity> 모든_연봉정보_맵 = salaryService.모든_연봉정보_맵();
        SalaryEntity salaryEntity = 모든_연봉정보_맵.get(salaryDTO.getC_key());
        int result = 0;
        if(salaryEntity != null) {
            salaryEntity.setC_annual_income(salaryDTO.getC_annual_income());
            result = salaryService.updateNode(salaryEntity);
        }
        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
