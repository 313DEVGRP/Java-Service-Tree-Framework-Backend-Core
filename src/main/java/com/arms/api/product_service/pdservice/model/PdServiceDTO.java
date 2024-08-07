package com.arms.api.product_service.pdservice.model;

import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class PdServiceDTO extends TreeBaseDTO {

    //@Getter @Setter
    private String c_pdservice_owner;

    private String c_pdservice_reviewer01;

    private String c_pdservice_reviewer02;

    private String c_pdservice_reviewer03;

    private String c_pdservice_reviewer04;

    private String c_pdservice_reviewer05;

    private String c_pdservice_writer;

    //내용
    private String c_pdservice_contents;

    //설명
    private String c_pdservice_desc;

    //비고
    private String c_pdservice_etc;
    private Set<PdServiceVersionEntity> pdServiceVersionEntities;

}
