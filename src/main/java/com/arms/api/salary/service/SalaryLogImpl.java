/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.salary.service;

import com.arms.api.salary.model.SalaryLogJdbcDTO;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@RequiredArgsConstructor
@Service("salaryLog")
@Slf4j
public class SalaryLogImpl extends TreeServiceImpl implements SalaryLog {


    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SalaryLogJdbcDTO> findSalaryLogsBetweenDates(String startDate, String endDate) {
//        String method = "create";
        String sql = "SELECT " +
                "    c_date, " +
                "    DATE_FORMAT(c_date, '%Y-%m-%d') AS formatted_date, " +
                "    c_method, " +
                "    c_name, " +
                "    c_state, " +
                "    c_key, " +
                "    c_annual_income " +
                "FROM " +
                "    T_ARMS_ANNUAL_INCOME_LOG " +
                "WHERE " +
                "    c_date BETWEEN ? AND ? " +
                "AND c_type = 'default' " +
//                "AND c_method = ? " +
                "ORDER BY c_date DESC";

        List<SalaryLogJdbcDTO> results = jdbcTemplate.query(
                sql,
                new Object[]{startDate, endDate},
                new RowMapper<SalaryLogJdbcDTO>() {
                    public SalaryLogJdbcDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                        SalaryLogJdbcDTO entry = new SalaryLogJdbcDTO();
                        entry.setC_date(rs.getTimestamp("c_date"));
                        entry.setFormatted_date(rs.getString("formatted_date"));
                        entry.setC_method(rs.getString("c_method"));
                        entry.setC_name(rs.getString("c_name"));
                        entry.setC_key(rs.getString("c_key"));
                        entry.setC_state(rs.getString("c_state"));
                        entry.setC_annual_income(rs.getInt("c_annual_income"));
                        return entry;
                    }
                });
        return results;

    }
}