package com.arms.api.product_service.pdserviceversion.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class PdServiceVersionDTO extends TreeBaseDTO {

    private String c_pds_version_start_date;

    private String c_pds_version_end_date;

    //내용
    private String c_pds_version_contents;

    //설명
    private String c_pds_version_desc;

    //비고
    private String c_pds_version_etc;
}
