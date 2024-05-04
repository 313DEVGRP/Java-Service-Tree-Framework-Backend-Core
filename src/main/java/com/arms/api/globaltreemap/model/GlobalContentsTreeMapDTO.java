package com.arms.api.globaltreemap.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GlobalContentsTreeMapDTO extends TreeBaseDTO {


    private Long map_key;

    private Long pdservice_link;

    private Long pdservicedetail_link;

    private Long filerepository_link;


}
