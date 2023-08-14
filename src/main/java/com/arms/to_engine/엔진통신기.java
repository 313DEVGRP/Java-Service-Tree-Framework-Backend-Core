package com.arms.to_engine;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "engine", url = "${arms.engine.url}")
public interface 엔진통신기 {

    @PostMapping("/jira/connect/info")
    JiraInfoEntity 지라서버_등록(@RequestBody JiraInfoDTO jiraInfoDTO);
}
