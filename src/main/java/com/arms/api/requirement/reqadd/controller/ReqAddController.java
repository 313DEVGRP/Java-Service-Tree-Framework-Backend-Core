/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqadd.controller;

import com.arms.api.product_service.pdservice.model.PdServiceDTO;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.excelupload.ExcelGantUpload;
import com.arms.api.requirement.reqadd.excelupload.WbsSchedule;
import com.arms.api.requirement.reqadd.model.*;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqdifficulty.service.ReqDifficulty;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqpriority.service.ReqPriority;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.util.TreeServiceUtils;
import com.arms.api.util.filerepository.model.FileRepositoryDTO;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.api.util.버전유틸;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.DateUtils;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.arms.egovframework.javaservice.treeframework.validation.group.AddNode;
import com.arms.egovframework.javaservice.treeframework.validation.group.MoveNode;
import com.arms.egovframework.javaservice.treeframework.validation.group.UpdateNode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqAdd"})
public class ReqAddController extends TreeAbstractController<ReqAdd, ReqAddDTO, ReqAddEntity> {

    @Autowired
    @Qualifier("reqAdd")
    private ReqAdd reqAdd;

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @Autowired
    @Qualifier("pdServiceVersion")
    private PdServiceVersion pdServiceVersion;

    @Autowired
    @Qualifier("reqPriority")
    private ReqPriority reqPriority;

    @Autowired
    @Qualifier("reqDifficulty")
    private ReqDifficulty reqDifficulty;

    @Autowired
    @Qualifier("reqState")
    private ReqState reqState;

    @PostConstruct
    public void initialize() {
        setTreeService(reqAdd);
        setTreeEntity(ReqAddEntity.class);
    }


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getMonitor(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            ReqAddDTO reqAddDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getMonitor");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("getMonitor", changeReqTableName);

        reqAddEntity.setOrder(Order.asc("c_position"));
        List<ReqAddEntity> list = reqAdd.getChildNodeWithoutPaging(reqAddEntity);

        SessionUtil.removeAttribute("getMonitor");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getNodesWithoutRoot.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getNodesWithoutRoot(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            ReqAddDTO reqAddDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getNodesWithoutRoot");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("getNodesWithoutRoot", changeReqTableName);

        Criterion criterion = Restrictions.not(
                // replace "id" below with property name, depending on what you're filtering against
                Restrictions.in("c_id", new Object[]{TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
        );
        reqAddEntity.getCriterions().add(criterion);
        reqAddEntity.setOrder(Order.asc("c_position"));
        List<ReqAddEntity> list = reqAdd.getChildNodeWithoutPaging(reqAddEntity);

        SessionUtil.removeAttribute("getNodesWithoutRoot");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getChildNode.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBChildNode(@PathVariable(value = "changeReqTableName") String changeReqTableName,
                                             ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getSwitchDBChildNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNode", changeReqTableName);

            reqAddEntity.setWhere("c_parentid", new Long(parser.get("c_id")));
            reqAddEntity.setOrder(Order.asc("c_position"));
            List<ReqAddEntity> list = reqAdd.getChildNode(reqAddEntity);

            SessionUtil.removeAttribute("getChildNode");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", list);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getChildNodeWithParent.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBChildNodeWithParent(@PathVariable(value = "changeReqTableName") String changeReqTableName,
                                                       ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getSwitchDBChildNodeWithParent");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNodeWithParent", changeReqTableName);

            Long targetId = new Long(parser.get("c_id"));
            Criterion criterion1 = Restrictions.eq("c_parentid", targetId);
            Criterion criterion2 = Restrictions.eq("c_id", targetId);
            Criterion criterion3 = Restrictions.or(criterion1, criterion2);
            reqAddEntity.getCriterions().add(criterion3);
            reqAddEntity.setOrder(Order.asc("c_position"));

            List<ReqAddEntity> list = reqAdd.getChildNode(reqAddEntity);

            SessionUtil.removeAttribute("getChildNodeWithParent");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", list);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getNode.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBNode(
            @PathVariable(value = "changeReqTableName") String changeReqTableName
            , ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getSwitchDBNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getNode", changeReqTableName);

            ReqAddEntity returnVO = reqAdd.getNode(reqAddEntity);

            SessionUtil.removeAttribute("getNode");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", returnVO);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getReqAddListByFilter.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getReqAddListByFilter(
            @PathVariable(value = "changeReqTableName") String changeReqTableName
            , ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("[ ReqAddController :: getSwitchDBNode ]");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("getReqAddListByFilter", changeReqTableName);

        String[] versionStrArr = StringUtils.split(reqAddEntity.getC_req_pdservice_versionset_link(), ",");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        List<ReqAddEntity> savedList = new ArrayList<>();
        if (versionStrArr == null || versionStrArr.length == 0) {
            reqAddEntity.setOrder(Order.asc("c_position"));
            savedList = reqAdd.getChildNodeWithoutPaging(reqAddEntity);
            SessionUtil.removeAttribute("getReqAddListByFilter");
        } else {
            Disjunction orCondition = Restrictions.disjunction();
            for (String versionStr : versionStrArr) {
                versionStr = "\\\"" + versionStr + "\\\"";
                orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
            }

            if (reqAddEntity.getC_type() != null) {
                reqAddEntity.getCriterions().add(orCondition);
                reqAddEntity.getCriterions().add(Restrictions.eq("c_type", reqAddEntity.getC_type()));
            } else {
                orCondition.add(Restrictions.eq("c_type","folder"));
                reqAddEntity.getCriterions().add(orCondition);
            }

            reqAddEntity.setOrder(Order.asc("c_position"));

            savedList = reqAdd.getChildNodeWithoutPaging(reqAddEntity);
            SessionUtil.removeAttribute("getReqAddListByFilter");
        }
        modelAndView.addObject("result", savedList);
        return modelAndView;

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/addNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addReqNode(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @Validated({AddNode.class}) ReqAddDTO reqAddDTO,
            BindingResult bindingResult, ModelMap model
    ) throws Exception {

        log.info("ReqAddController :: addReqNode");

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        reqAddEntity.setPdServiceEntity(TreeServiceUtils.getNode(pdService, reqAddDTO.getC_req_pdservice_link(), PdServiceEntity.class));

        reqAddEntity.setReqPriorityEntity(TreeServiceUtils.getNode(reqPriority, reqAddDTO.getC_req_priority_link(), ReqPriorityEntity.class));

        reqAddEntity.setReqDifficultyEntity(TreeServiceUtils.getNode(reqDifficulty, reqAddDTO.getC_req_difficulty_link(), ReqDifficultyEntity.class));

        reqAddEntity.setReqStateEntity(TreeServiceUtils.getNode(reqState, reqAddDTO.getC_req_state_link(), ReqStateEntity.class));

        Date date = new Date();
        reqAddEntity.setC_req_create_date(date);
        reqAddEntity.setC_req_update_date(date);

        List<Long> versionList = Optional.ofNullable(reqAddEntity.getC_req_pdservice_versionset_link())
                .map(버전유틸::convertToLongArray)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());

        Date 버전시작일 = null;
        Date 버전종료일 = null;
        if (!versionList.isEmpty()) {
            Map<String, String> 시작일과_종료일 = pdServiceVersion.versionPeriod(versionList);

            버전시작일 = DateUtils.getDate(시작일과_종료일.get("earliestDate"), "yyyy/MM/dd HH:mm");
            버전종료일 = DateUtils.getDate(시작일과_종료일.get("latestDate"), "yyyy/MM/dd HH:mm");

            if (reqAddEntity.getC_req_start_date() == null) {
                reqAddEntity.setC_req_start_date(버전시작일);
            }

            if (reqAddEntity.getC_req_end_date() == null) {
                reqAddEntity.setC_req_end_date(버전종료일);
            }
        }

        long 총계획기간일수 = 0;
        if (reqAddEntity.getC_req_start_date() != null && reqAddEntity.getC_req_end_date() != null) {
            총계획기간일수 = DateUtils.getDiffDay(reqAddEntity.getC_req_start_date(), reqAddEntity.getC_req_end_date());
        }
        reqAddEntity.setC_req_plan_time(총계획기간일수);

        long 총기간일수 = 0;
        if (버전시작일 != null && 버전종료일 != null) {
            총기간일수 = DateUtils.getDiffDay(버전시작일, 버전종료일);
        }
        reqAddEntity.setC_req_total_time(총기간일수);

        long 총작업MM = DateUtils.convertDaysToManMonth(총기간일수);
        long 총계획MM = DateUtils.convertDaysToManMonth(총계획기간일수);

        reqAddEntity.setC_req_total_resource(총작업MM);
        reqAddEntity.setC_req_plan_resource(총계획MM);
        ReqAddEntity savedNode = reqAdd.addReqNode(reqAddEntity, changeReqTableName);

        log.info("ReqAddController :: addReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedNode));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/updateNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> updateReqNode(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @Validated({UpdateNode.class}) ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model
    ) throws Exception {

        log.info("ReqAddController :: updateReqNode");

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        if (reqAddDTO.getC_req_priority_link() != null) {
            reqAddEntity.setReqPriorityEntity(TreeServiceUtils.getNode(reqPriority, reqAddDTO.getC_req_priority_link(), ReqPriorityEntity.class));
        }

        if (reqAddDTO.getC_req_difficulty_link() != null) {
            reqAddEntity.setReqDifficultyEntity(TreeServiceUtils.getNode(reqDifficulty, reqAddDTO.getC_req_difficulty_link(), ReqDifficultyEntity.class));
        }

        if (reqAddDTO.getC_req_state_link() != null) {
            reqAddEntity.setReqStateEntity(TreeServiceUtils.getNode(reqState, reqAddDTO.getC_req_state_link(), ReqStateEntity.class));
        }

        Date date = new Date();
        reqAddEntity.setC_req_update_date(date);

        List<Long> versionList = Optional.ofNullable(reqAddEntity.getC_req_pdservice_versionset_link())
                .map(버전유틸::convertToLongArray)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());

        Date 버전시작일 = null;
        Date 버전종료일 = null;
        if (!versionList.isEmpty()) {
            Map<String, String> 시작일과_종료일 = pdServiceVersion.versionPeriod(versionList);

            버전시작일 = DateUtils.getDate(시작일과_종료일.get("earliestDate"), "yyyy/MM/dd HH:mm");
            버전종료일 = DateUtils.getDate(시작일과_종료일.get("latestDate"), "yyyy/MM/dd HH:mm");

            if (reqAddEntity.getC_req_start_date() == null) {
                reqAddEntity.setC_req_start_date(버전시작일);
            }

            if (reqAddEntity.getC_req_end_date() == null) {
                reqAddEntity.setC_req_end_date(버전종료일);
            }
        }

        long 총계획기간일수 = 0;
        if (reqAddEntity.getC_req_start_date() != null && reqAddEntity.getC_req_end_date() != null) {
            총계획기간일수 = DateUtils.getDiffDay(reqAddEntity.getC_req_start_date(), reqAddEntity.getC_req_end_date());
        }
        reqAddEntity.setC_req_plan_time(총계획기간일수);

        long 총기간일수 = 0;
        if (버전시작일 != null && 버전종료일 != null) {
            총기간일수 = DateUtils.getDiffDay(버전시작일, 버전종료일);
        }
        reqAddEntity.setC_req_total_time(총기간일수);

        long 총작업MM = DateUtils.convertDaysToManMonth(총기간일수);
        long 총계획MM = DateUtils.convertDaysToManMonth(총계획기간일수);

        reqAddEntity.setC_req_total_resource(총작업MM);
        reqAddEntity.setC_req_plan_resource(총계획MM);

        Integer result = reqAdd.updateReqNode(reqAddEntity, changeReqTableName);

        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/updateDate.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> updateReqDate(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @Validated({UpdateNode.class}) ReqAddDateDTO reqAddDateDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model
    ) throws Exception {

        log.info("ReqAddController :: updateDate");

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDateDTO, ReqAddEntity.class);

        long 총계획기간일수 = 0;
        if (reqAddEntity.getC_req_start_date() != null && reqAddEntity.getC_req_end_date() != null) {
            총계획기간일수 = DateUtils.getDiffDay(reqAddEntity.getC_req_start_date(), reqAddEntity.getC_req_end_date());
        }
        reqAddEntity.setC_req_plan_time(총계획기간일수);

        long 총계획MM = DateUtils.convertDaysToManMonth(총계획기간일수);
        reqAddEntity.setC_req_total_resource(총계획MM);

        SessionUtil.setAttribute("updateDate", changeReqTableName);

        int result = reqAdd.updateNode(reqAddEntity);

        SessionUtil.removeAttribute("updateDate");

        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/updateDataBase.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> updateDataBase(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @Validated({UpdateNode.class}) ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model
    ) throws Exception {

        log.info("ReqAddController :: updateDataBase"); // 요구사항 상태 우선순위 난이도 시작일 종료일 데이터 베이스 값 변경

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);
        if(reqAddDTO.getC_req_state_link() != null){
            reqAddEntity.setReqStateEntity(TreeServiceUtils.getNode(reqState, reqAddDTO.getC_req_state_link(), ReqStateEntity.class)); // 상태
        }
        if(reqAddDTO.getC_req_priority_link() != null){
            reqAddEntity.setReqPriorityEntity(TreeServiceUtils.getNode(reqPriority, reqAddDTO.getC_req_priority_link(), ReqPriorityEntity.class)); // 우선순위
        }
        if(reqAddDTO.getC_req_difficulty_link() != null){
            reqAddEntity.setReqDifficultyEntity(TreeServiceUtils.getNode(reqDifficulty, reqAddDTO.getC_req_difficulty_link(), ReqDifficultyEntity.class)); // 난이도
        }

        int result = reqAdd.updateDataBase(reqAddEntity,changeReqTableName);

        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/removeNode.do"},
            method = {RequestMethod.DELETE}
    )
    public ResponseEntity<?> removeReqNode(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @Validated({UpdateNode.class}) ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddController :: removeReqNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        int removedReqAddEntity = reqAdd.removeReqNode(reqAddEntity, changeReqTableName, request);

        log.info("ReqAddController :: removeReqNode");
        return ResponseEntity.ok(CommonResponse.success(removedReqAddEntity));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/moveNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> moveReqNode(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @Validated({MoveNode.class}) ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddController :: moveReqNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("moveNode", changeReqTableName);

        ReqAddEntity savedReqAddEntity = reqAdd.moveNode(reqAddEntity, request);

        SessionUtil.removeAttribute("moveNode");

        log.info("ReqAddController :: moveReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedReqAddEntity));

    }

    @ResponseBody
    @RequestMapping(value = "/uploadFileToNode.do", method = RequestMethod.POST)
    public ModelAndView uploadFileToNode(final MultipartHttpServletRequest multiRequest,
                                         HttpServletRequest request, Model model) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        long pdservice_link = parser.getLong("pdservice_link");

        HashMap<String, Set<FileRepositoryEntity>> map = new HashMap();

        Set<FileRepositoryEntity> entitySet = Collections.emptySet();
        map.put("files", entitySet);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", map);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getFilesByNode.do", method = RequestMethod.GET)
    public ModelAndView getFilesByNode(FileRepositoryDTO fileRepositoryDTO, HttpServletRequest request) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        HashMap<String, Set<FileRepositoryEntity>> returnMap = new HashMap();

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", returnMap);

        return modelAndView;
    }

    @ResponseBody
    @PostMapping(value = "/sample/excel-to-list")
    public ResponseEntity excelUpload(@RequestPart("excelFile") MultipartFile excelFile, HttpServletRequest request) throws Exception {
        //확인후에 저장을 하기 위한 샘플입니다.
        return ResponseEntity.ok(CommonResponse.success(
                        new ExcelGantUpload(excelFile.getInputStream())
                                .getGetWebScheduleList()
                                .stream()
                                .sorted(comparing(WbsSchedule::getDepth).reversed())
                                .collect(toList())
                )
        );
    }

    @ResponseBody
    @GetMapping(value = "/{changeReqTableName}/getDetail.do")
    public ResponseEntity<ReqAddDetailDTO> followReqLink(FollowReqLinkDTO followReqLinkDTO, @PathVariable(value = "changeReqTableName") String changeReqTableName
    ) throws Exception {

        return ResponseEntity.ok(reqAdd.getDetail(followReqLinkDTO, changeReqTableName));
    }


    @GetMapping(value = "/{changeReqTableName}/getNodeDetail.do")
    public ResponseEntity<LoadReqAddDTO> loadReqNode(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestParam(value = "c_id") Long c_id, HttpServletRequest request
    ) throws Exception {

        log.info("ReqAddController :: getNodeDetail.do :: changeReqTableName {} :: c_id {}", changeReqTableName, c_id);

        SessionUtil.setAttribute("getNodeDetail", changeReqTableName);

        ReqAddEntity reqAddEntity = new ReqAddEntity();

        reqAddEntity.setC_id(c_id);

        ReqAddEntity response = reqAdd.getNode(reqAddEntity);

        log.info("ReqAddController :: getNodeDetail.do :: response :: " + response);

        ModelMapper customModelMapper = new ModelMapper();
        customModelMapper.addMappings(new PropertyMap<ReqAddEntity, LoadReqAddDTO>() {
            @Override
            protected void configure() {
                map().setC_req_pdservice_link(source.getPdServiceEntity().getC_id());
                map().setC_req_priority_link(source.getReqPriorityEntity().getC_id());
                map().setC_req_state_link(source.getReqStateEntity().getC_id());
                map().setC_req_difficulty_link(source.getReqDifficultyEntity().getC_id());
            }
        });

        LoadReqAddDTO reqAddDto = customModelMapper.map(response, LoadReqAddDTO.class);

        log.info("ReqAddController :: getNodeDetail.do :: reqAddDto :: " + reqAddDto);

        SessionUtil.removeAttribute("getNodeDetail");

        return ResponseEntity.ok(reqAddDto);
    }


    @GetMapping(value = "/{changeReqTableName}/getNodesWhereInIds.do")
    public ResponseEntity<List<LoadReqAddDTO>> getNodesWhereInIds(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestParam List<Long> ids, HttpServletRequest request
    ) throws Exception {

        log.info("ReqAddController :: getNodesWhereInIds :: changeReqTableName :: {} :: ids {} ", changeReqTableName, ids);

        SessionUtil.setAttribute("getNodesWhereInIds", changeReqTableName);

        ReqAddEntity reqAddEntity = new ReqAddEntity();

        Criterion criterion = Restrictions.in("c_id", ids);

        reqAddEntity.getCriterions().add(criterion);

        reqAddEntity.setOrder(Order.asc("c_position"));

        List<ReqAddEntity> list = reqAdd.getChildNodeWithoutPaging(reqAddEntity);

        SessionUtil.removeAttribute("getNodesWhereInIds");

        ModelMapper customModelMapper = new ModelMapper();
        customModelMapper.addMappings(new PropertyMap<ReqAddEntity, LoadReqAddDTO>() {
            @Override
            protected void configure() {
                map().setC_req_pdservice_link(source.getPdServiceEntity().getC_id());
                map().setC_req_priority_link(source.getReqPriorityEntity().getC_id());
                map().setC_req_state_link(source.getReqStateEntity().getC_id());
                map().setC_req_difficulty_link(source.getReqDifficultyEntity().getC_id());
            }
        });

        List<LoadReqAddDTO> loadReqAddDTOList = list.stream().map(entity -> customModelMapper.map(entity, LoadReqAddDTO.class)).collect(toList());

        return ResponseEntity.ok(loadReqAddDTOList);
    }

    @GetMapping(value = "/getRequirementAssignee.do")
    public ResponseEntity<?> getRequirementAssignee(PdServiceDTO pdServiceDTO, HttpServletRequest request) throws Exception {
        log.info("ReqAddController :: getRequirementAssignee");

        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        return ResponseEntity.ok(CommonResponse.success( reqAdd.getRequirementAssignee(pdServiceEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/updateReqAddOnly.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> updateReqAddOnly(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestBody ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model
    ) throws Exception {

        log.info("ReqAddController :: updateReqAddOnly"); // 요구사항 상태 우선순위 난이도 시작일 종료일 데이터 베이스 값 변경

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);
        if(reqAddDTO.getC_req_state_link() != null){
            reqAddEntity.setReqStateEntity(TreeServiceUtils.getNode(reqState, reqAddDTO.getC_req_state_link(), ReqStateEntity.class)); // 상태
        }
        if(reqAddDTO.getC_req_priority_link() != null){
            reqAddEntity.setReqPriorityEntity(TreeServiceUtils.getNode(reqPriority, reqAddDTO.getC_req_priority_link(), ReqPriorityEntity.class)); // 우선순위
        }
        if(reqAddDTO.getC_req_difficulty_link() != null){
            reqAddEntity.setReqDifficultyEntity(TreeServiceUtils.getNode(reqDifficulty, reqAddDTO.getC_req_difficulty_link(), ReqDifficultyEntity.class)); // 난이도
        }

        SessionUtil.setAttribute("updateReqAddOnly", changeReqTableName);
        int result = reqAdd.updateNode(reqAddEntity);
        SessionUtil.removeAttribute("updateReqAddOnly");

        return ResponseEntity.ok(CommonResponse.success(result));
    }
}
