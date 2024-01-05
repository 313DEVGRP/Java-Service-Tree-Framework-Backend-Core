package com.arms.api.analysis.scope.dto;

import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreeBarDTO {
    private String id;
    private String name;
    private String parent;
    private long value;
    private String color;
    private String type;

    public TreeBarDTO(ReqStatusEntity reqStatusEntity) {
        this.id = reqStatusEntity.getC_issue_key();
        this.name = reqStatusEntity.getC_issue_key() + " - " +reqStatusEntity.getC_title();
        this.parent = reqStatusEntity.getC_pds_version_link().toString();
        this.value = 0;
        this.color = "";
        this.type = "requirement";
    }
}
