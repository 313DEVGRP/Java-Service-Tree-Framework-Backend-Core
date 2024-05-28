package com.arms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArmsDetailUrlConfig {

    @Value("${arms.detail.connect.url}")
    public String address;

    @Value("${requirement.state.complete.keyword}")
    private String completeKeyword;

    @Value("${requirement.delete.type}")
    public String deleteTYpe;

    public String getAddress() {
        return this.address;
    }

    public String getDeleteTYpe() {
        return this.deleteTYpe;
    }

    public String getCompleteKeyword() {
        return this.completeKeyword;
    }

}
