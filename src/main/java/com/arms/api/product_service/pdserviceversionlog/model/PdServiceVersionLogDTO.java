package com.arms.api.product_service.pdserviceversionlog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class PdServiceVersionLogDTO extends TreeBaseDTO {


    private Long c_pdservice_link;

    private String c_pds_version_start_date;

    private String c_pds_version_end_date;

    private String c_pds_version_contents;

    private String c_pds_version_etc;
}
