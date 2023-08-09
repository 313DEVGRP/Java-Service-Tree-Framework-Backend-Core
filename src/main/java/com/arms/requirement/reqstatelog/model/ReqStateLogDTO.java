package com.arms.requirement.reqstatelog.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqStateLogDTO extends TreeBaseDTO {

    private String c_contents;

    private String c_etc;

}
