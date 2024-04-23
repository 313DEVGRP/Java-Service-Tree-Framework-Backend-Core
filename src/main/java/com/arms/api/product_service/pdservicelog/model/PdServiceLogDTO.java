package com.arms.api.product_service.pdservicelog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class PdServiceLogDTO extends TreeBaseDTO {


    private String c_pdservice_contents;

    private String c_pdservice_etc;

    private String c_pdservice_owner;

    private String c_pdservice_reviewer01;

    private String c_pdservice_reviewer02;

    private String c_pdservice_reviewer03;

    private String c_pdservice_reviewer04;

    private String c_pdservice_reviewer05;

    private String c_pdservice_writer;

}
