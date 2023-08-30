package com.arms.util.external_communicate;

import com.arms.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.util.external_communicate.dto.*;
import com.arms.util.external_communicate.dto.cloud.*;
import com.arms.util.external_communicate.dto.onpremise.*;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(name = "loopback", url = "http://127.0.0.1:31313")
public interface 내부통신기 {

    @PostMapping("/arms/reqStatus/{changeReqTableName}/addStatusNode.do")
    ResponseEntity<?> 요구사항_이슈_저장하기(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                   @RequestBody ReqStatusDTO reqStatusDTO);

    @PostMapping("/arms/reqStatus/{changeReqTableName}/getStatusMonitor.do")
    ResponseEntity<?> 제품별_요구사항_이슈_조회(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                   @RequestBody ReqStatusDTO reqStatusDTO);


}
