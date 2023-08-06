package com.arms.pdserviceversion.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;

@Getter
@Setter
@Builder
@ToString
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
