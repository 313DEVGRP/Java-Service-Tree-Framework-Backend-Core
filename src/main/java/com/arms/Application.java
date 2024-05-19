/*
 * @author Dongmin.lee
 * @since 2023-03-13
 * @version 23.03.13
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableSwagger2
@SpringBootApplication
@ImportResource(value= {
        "classpath:com/arms/egovframework/spring/context-common.xml",
        "classpath:com/arms/egovframework/spring/context-hibernate.xml",
        "classpath:com/arms/egovframework/spring/context-jdbc.xml"
})
@EnableAsync
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


}
