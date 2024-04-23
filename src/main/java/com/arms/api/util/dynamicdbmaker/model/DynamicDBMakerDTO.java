package com.arms.api.util.dynamicdbmaker.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class DynamicDBMakerDTO extends TreeBaseDTO {

    private String dummy;
}
