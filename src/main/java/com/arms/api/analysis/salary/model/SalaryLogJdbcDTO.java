package com.arms.api.analysis.salary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class SalaryLogJdbcDTO implements Serializable {

    private Date c_date;

    private String formatted_date;

    private String c_method;

    private String c_name;

    private String c_state;

    private String c_key;

    private Integer c_annual_income;

}
