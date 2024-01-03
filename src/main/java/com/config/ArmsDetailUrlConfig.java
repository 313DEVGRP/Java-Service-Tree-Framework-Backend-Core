package com.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArmsDetailUrlConfig {

    public static String address;

    @Value("${arms.detail.connect.url}")
    public static void setAddress(String address) {
        ArmsDetailUrlConfig.address = address;
    }
}
