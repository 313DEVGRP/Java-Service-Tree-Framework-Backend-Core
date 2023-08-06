package com.egovframework.javaservice.treemap.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Entity
@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Table(name = "GLOBAL_TREE_MAP")
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(of={"map_key"})
public class GlobalTreeMapEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "map_key")
    private Long map_key;

    @Column(name = "filerepository_link")
    private Long filerepository_link;


    @Column(name = "jiraserver_link")
    private Long jiraserver_link;


    @Column(name = "pdservice_link")
    private Long pdservice_link;

    @Column(name = "pdserviceversion_link")
    private Long pdserviceversion_link;


    @Column(name = "jiraproject_link")
    private Long jiraproject_link;

    @Column(name = "jiraprojectversion_link")
    private Long jiraprojectversion_link;


    @Column(name = "jiraissue_link")
    private Long jiraissue_link;

    @Column(name = "jiraissuepriority_link")
    private Long jiraissuepriority_link;

    @Column(name = "jiraissueresolution_link")
    private Long jiraissueresolution_link;

    @Column(name = "jiraissuestatus_link")
    private Long jiraissuestatus_link;

    @Column(name = "jiraissuetype_link")
    private Long jiraissuetype_link;

}
