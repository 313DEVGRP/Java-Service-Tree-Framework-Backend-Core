package com.arms.globaltreemap.controller;

import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import com.arms.globaltreemap.model.GlobalTreeMapDTO;
import com.arms.globaltreemap.model.GlobalTreeMapEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.egovframework.javaservice.treeframework.remote.Global.chat;

@Slf4j
@RequestMapping(value = {"/arms/globaltreemap"})
@RestController
public class GlobalTreeMapController extends TreeMapAbstractController {

    @ResponseBody
    @RequestMapping(
            value = {"/getConnectInfo/pdService/pdServiceVersion/jiraProject.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getConnectInfo_pdService_pdServiceVersion_jiraProject(GlobalTreeMapDTO globalTreeMapDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("GlobalTreeMapController :: getConnectInfo_pdService_pdServiceVersion_jiraProject");
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);

        List<GlobalTreeMapEntity> savedList = globalTreeMapService.findAllBy(globalTreeMapEntity);

        List<GlobalTreeMapEntity> filteredList = savedList.stream().filter(savedData ->
                        savedData.getPdserviceversion_link() != null &&
                        savedData.getJiraproject_link() != null
        ).collect(Collectors.toList());
        return ResponseEntity.ok(CommonResponse.success(filteredList));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/setConnectInfo/pdService/pdServiceVersion/jiraProject.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> setConnectInfo_pdService_pdServiceVersion_jiraProject(GlobalTreeMapDTO globalTreeMapDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("GlobalTreeMapController :: setConnectInfo_pdService_pdServiceVersion_jiraProject");
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);

        ParameterParser parser = new ParameterParser(request);
        String[] paramList = StringUtils.jsonStringifyConvert(parser.get("c_pdservice_jira_ids"));

        List<String> jiraProjectList = Arrays.stream(paramList).collect(Collectors.toList()); //지라프로젝트들

        // 1. pdService 와 pdServiceVersion 으로 리스트를 찾고
        // 2. 찾은 리스트를 중심으로 루프를 돌면서
        // 매치 리스트는 save 하고
        // non 매치 리스트는 delete 를 하자.

        GlobalTreeMapEntity searchObj = new GlobalTreeMapEntity();
        searchObj.setPdserviceversion_link(globalTreeMapEntity.getPdserviceversion_link());
        List<GlobalTreeMapEntity> searchedObjList = globalTreeMapService.findAllBy(searchObj); // pdservice와 pdserviceVersion으로 트리맵엔티티들 가져옴

        // 1. pdService 와 pdServiceVersion 으로 리스트를 찾고
        List<GlobalTreeMapEntity> 트리맵_제품_지라프로젝트_연결정보 = searchedObjList.stream().filter(data ->
                        data.getPdserviceversion_link() != null &&
                        data.getJiraproject_link() != null
        ).collect(Collectors.toList()); // 가져온 트리맵엔티티중에서 지라프로젝트도 있는 녀석들만 가져옴

        // 2. 찾은 리스트를 중심으로 루프를 돌면서 삭제할거 삭제하고
        for(GlobalTreeMapEntity data : 트리맵_제품_지라프로젝트_연결정보){
            Long jiraProjectLink = data.getJiraproject_link();
            boolean alreadyRegist = jiraProjectList.stream().anyMatch(dataObj -> jiraProjectLink == NumberUtils.toLong(dataObj) );

            if(alreadyRegist){
                //이미 등록된 데이터.
                log.info("already registered : getPdserviceversion_link" + data.getPdserviceversion_link());
                log.info("already registered : getJiraproject_link" + data.getJiraproject_link());

                //jiraProjectList.remove(data);

            }else {
                //등록은 되 있으나, 매치되지 않은 데이터 : 삭제 대상.
                globalTreeMapService.delete(data);
            }
        }

        for( String jiraProject : jiraProjectList ){

            GlobalTreeMapEntity saveTarget = new GlobalTreeMapEntity();
            saveTarget.setPdserviceversion_link(globalTreeMapEntity.getPdserviceversion_link());
            saveTarget.setJiraproject_link(Long.parseLong(jiraProject));


            List<GlobalTreeMapEntity> checkList = globalTreeMapService.findAllBy(saveTarget);
            List<GlobalTreeMapEntity> checkDuplicate = checkList.stream().filter(data ->
                            data.getPdserviceversion_link().equals(saveTarget.getPdserviceversion_link()) &&
                            data.getJiraproject_link().equals(saveTarget.getJiraproject_link())
            ).collect(Collectors.toList());

            if( checkDuplicate == null || checkDuplicate.isEmpty() == true) {
                globalTreeMapService.saveOne(saveTarget);
            }else{
                log.info(globalTreeMapEntity.toString());
            }
        }
        chat.sendMessageByEngine("지라 프로젝트가 추가되었습니다.");

        return ResponseEntity.ok(CommonResponse.success("test"));

    }

}
