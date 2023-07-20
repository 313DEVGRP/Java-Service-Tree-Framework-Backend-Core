package com.egovframework.javaservice.treemap.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTreeMapDTO {


    private Long map_key;

    private Long filerepository_link;


    private Long jiraserver_link;

    private Long jiraconnectinfo_link;


    private Long pdservice_link;

    private Long pdserviceversion_link;


    private Long jiraproject_link;

    private Long jiraprojectversion_link;


    private Long jiraissue_link;

    private Long jiraissuepriority_link;

    private Long jiraissueresolution_link;

    private Long jiraissuestatus_link;

    private Long jiraissuetype_link;

}
