package com.arms.api.globaltreemap.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTreeMapDTO extends TreeBaseDTO {



    private Long map_key;

    private Long filerepository_link;


    private Long pdservice_link;

    private Long pdserviceversion_link;


    private Long jiraserver_link;

    private Long jiraproject_link;

    private Long jiraissuepriority_link;

    private Long jiraissueresolution_link;

    private Long jiraissuestatus_link;

    private Long jiraissuetype_link;

}
