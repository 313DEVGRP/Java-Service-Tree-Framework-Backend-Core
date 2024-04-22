package com.arms.api.util.communicate.internal;

import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "loopback", url = "http://127.0.0.1:31313")
public interface 내부통신기 {

    @PostMapping("/arms/reqStatus/{changeReqTableName}/addStatusNode.do")
    ResponseEntity<?> 요구사항_이슈_저장하기(@PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestBody ReqStatusDTO reqStatusDTO);

    @GetMapping("/arms/reqStatus/{changeReqTableName}/getStatusMonitor.do")
    List<ReqStatusEntity> 제품별_요구사항_이슈_조회(@PathVariable(value = "changeReqTableName") String changeReqTableName,
            @SpringQueryMap ReqStatusDTO reqStatusDTO);

    @PutMapping("/arms/reqStatus/{changeReqTableName}/updateStatusNode.do")
    ResponseEntity<?> 요구사항_이슈_수정하기(@PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestBody ReqStatusDTO reqStatusDTO);

    @GetMapping("/arms/reqAdd/{changeReqTableName}/getNodeDetail.do")
    ResponseEntity<LoadReqAddDTO> 요구사항조회(@PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestParam(value = "c_id") Long c_id);

    @GetMapping("/arms/reqAdd/{changeReqTableName}/getNodesWhereInIds.do")
    ResponseEntity<List<LoadReqAddDTO>> 요구사항목록조회(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            @RequestParam List<Long> ids
    );

}
