package com.arms.util.dynamicdbmaker.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DynamicDBMakerDTO extends TreeBaseDTO {

    private String dummy;
}
