package com.arms.api.analysis.scope.dto;

import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
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

    public TreeBarDTO(LoadReqAddDTO loadReqAddDTO) {
        this.id = "requirement-" + loadReqAddDTO.getC_id().toString();
        this.name = loadReqAddDTO.getC_title();
        this.parent = loadReqAddDTO.getC_req_pdservice_versionset_link().toString();
        this.value = 0;
        this.color = "";
        this.type = "requirement";
    }
}
