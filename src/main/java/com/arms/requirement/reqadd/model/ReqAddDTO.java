package com.arms.requirement.reqadd.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqAddDTO extends TreeBaseDTO {

    private Long c_req_pdservice_link;
    
    private String c_req_pdservice_versionset_link;

    private String c_req_reviewer01;

    private String c_req_reviewer02;

    private String c_req_reviewer03;

    private String c_req_reviewer04;

    private String c_req_reviewer05;

    private String c_req_reviewer01_status;

    private String c_req_reviewer02_status;

    private String c_req_reviewer03_status;

    private String c_req_reviewer04_status;

    private String c_req_reviewer05_status;

    private String c_req_writer;

    private String c_req_create_date;

    private String c_req_update_date;

    private String c_req_contents;

    private String c_req_desc;

    private String c_req_etc;
}