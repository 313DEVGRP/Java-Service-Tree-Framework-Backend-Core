package com.arms.globaltreemap.controller;

import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.CommonResponse.ApiResult;
import com.arms.globaltreemap.model.GlobalTreeMapDTO;
import com.arms.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.globaltreemap.service.GlobalTreeMapService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Api("TreeMapFramework")
public abstract class TreeMapAbstractController {

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected GlobalTreeMapService globalTreeMapService;

    @GetMapping("/find")
    public ResponseEntity<?> findMapList(GlobalTreeMapDTO globalTreeMapDTO){

        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);
        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.findAllBy(globalTreeMapEntity)));
    }

    @GetMapping("/findBy")
    public ResponseEntity<ApiResult<GlobalTreeMapEntity>> findMap(@PathVariable Long mapKey){
        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.findById(mapKey)));
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResult<List<GlobalTreeMapEntity>>> saveMapList(@RequestBody List<GlobalTreeMapDTO> globalTreeMapDTOs){

        return ResponseEntity.ok(CommonResponse.success(
                globalTreeMapService.saveAll(
                    globalTreeMapDTOs
                    .stream()
                    .map(dto->modelMapper.map(dto, GlobalTreeMapEntity.class))
                    .collect(Collectors.toList())
                )));
    }

    @PostMapping("/saveOne")
    public ResponseEntity<ApiResult<GlobalTreeMapEntity>> saveMapOne(@RequestBody GlobalTreeMapDTO globalTreeMapDTO){
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);
        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.saveOne(globalTreeMapEntity)));
    }

    @DeleteMapping("/delete/{mapKey}")
    public ResponseEntity<ApiResult<String>> deleteMapList(@PathVariable Long mapKey){
        globalTreeMapService.delete(mapKey);
        return ResponseEntity.ok(CommonResponse.success("OK"));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getAllGlobalTreeMap.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getAllGlobalTreeMap(GlobalTreeMapDTO globalTreeMapDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("GlobalTreeMapController :: getAllGlobalTreeMap");
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);

        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.findAllBy(globalTreeMapEntity)));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/addGlobalTreeMap.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addGlobalTreeMap(GlobalTreeMapDTO globalTreeMapDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("GlobalTreeMapController :: addGlobalTreeMap");
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);

        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.saveOne(globalTreeMapEntity)));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/alterGlobalTreeMap.do"},
            method = {RequestMethod.PUT}
    )
    public ResponseEntity<?> alterGlobalTreeMap(GlobalTreeMapDTO globalTreeMapDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("GlobalTreeMapController :: alterGlobalTreeMap");
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);

        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.update(globalTreeMapEntity)));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/removeGlobalTreeMap.do"},
            method = {RequestMethod.DELETE}
    )
    public ResponseEntity<?> removeGlobalTreeMap(GlobalTreeMapDTO globalTreeMapDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("GlobalTreeMapController :: removeGlobalTreeMap");
        GlobalTreeMapEntity globalTreeMapEntity = modelMapper.map(globalTreeMapDTO, GlobalTreeMapEntity.class);

        List<GlobalTreeMapEntity> globalTreeMapEntityList = globalTreeMapService.findAllBy(globalTreeMapEntity);

        if(globalTreeMapEntityList == null || globalTreeMapEntityList.isEmpty()){
            return ResponseEntity.ok(CommonResponse.error("not found, thus not delete", HttpStatus.INTERNAL_SERVER_ERROR));
        }else if (globalTreeMapEntityList.size() > 1){
            return ResponseEntity.ok(CommonResponse.error("not found, thus not delete", HttpStatus.INTERNAL_SERVER_ERROR));
        }

        return ResponseEntity.ok(CommonResponse.success(globalTreeMapService.delete(globalTreeMapEntityList.get(0))));

    }


}
