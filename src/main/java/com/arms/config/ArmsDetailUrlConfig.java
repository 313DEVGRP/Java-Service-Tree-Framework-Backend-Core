package com.arms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArmsDetailUrlConfig {

    @Value("${arms.detail.connect.url}")
    public String address;


    public String getAddress() {
        return address;
    }
}