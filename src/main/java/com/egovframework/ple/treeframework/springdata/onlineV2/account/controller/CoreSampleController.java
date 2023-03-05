package com.egovframework.ple.treeframework.springdata.onlineV2.account.controller;

import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.AccountEntity;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.model.CoreSampleEntity;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.service.AccountServiceV2;
import com.egovframework.ple.treeframework.springdata.onlineV2.account.service.CoreSampleService;
import com.egovframework.ple.treeframework.springdata.onlineV2.common.CommonResponse;
import com.egovframework.ple.treeframework.springdata.onlineV2.common.validate.InsertGroup;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v2")
@RestController
@AllArgsConstructor
public class CoreSampleController {

    private final CoreSampleService coreSampleService;

    @PostMapping("/account")
    public ResponseEntity<?> join(@RequestBody @Validated(value = InsertGroup.class) CoreSampleEntity coreSampleEntity){
        return ResponseEntity
                .ok(CommonResponse.success(coreSampleService.join(coreSampleEntity)));
    }

    @GetMapping("/account/")
    public ResponseEntity<?> findAccount(CoreSampleEntity coreSampleEntity){
        return ResponseEntity
                .ok(CommonResponse.success(coreSampleService.findAllBy(coreSampleEntity)));
    }

    @GetMapping("/account/paging")
    public ResponseEntity<?> findPagingAccount(CoreSampleEntity coreSampleEntity, Pageable pageable){
        return ResponseEntity
                .ok(CommonResponse.success(coreSampleService.findAllBy(coreSampleEntity,pageable)));
    }

}
