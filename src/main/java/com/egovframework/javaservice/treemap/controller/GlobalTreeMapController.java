package com.egovframework.javaservice.treemap.controller;

import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.CommonResponse.ApiResult;
import com.egovframework.javaservice.treemap.model.GlobalTreeMapDTO;
import com.egovframework.javaservice.treemap.model.GlobalTreeMapEntity;
import com.egovframework.javaservice.treemap.service.GlobalTreeMapService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(value = {"/api/mad/global-tree-map"})
@RestController
@AllArgsConstructor
public class GlobalTreeMapController {

    @Autowired
    protected ModelMapper modelMapper;
    private final GlobalTreeMapService globalTreeMapService;

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




}
