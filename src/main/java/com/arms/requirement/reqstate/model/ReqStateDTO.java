package com.arms.requirement.reqstate.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqStateDTO extends TreeBaseDTO {


    private String c_contents;

    private String c_etc;
}
