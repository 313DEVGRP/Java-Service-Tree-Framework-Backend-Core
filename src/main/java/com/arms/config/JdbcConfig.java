package com.arms.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcConfig {

    @Bean
    public JdbcTemplate logJdbcTemplate(@Qualifier("onlyJdbcDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}