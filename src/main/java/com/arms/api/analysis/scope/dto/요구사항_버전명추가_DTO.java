package com.arms.api.analysis.scope.dto;

import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class 요구사항_버전명추가_DTO {

    ReqAddEntity reqAddEntity;

    String version_name;
}
