package com.arms.api.globaltreemap.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Table(name = "GLOBAL_CONTENTS_TREE_MAP")
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(of={"map_key"})
public class GlobalContentsTreeMapEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "map_key")
    private Long map_key;

    @Column(name = "filerepository_link")
    private Long filerepository_link;

    @Column(name = "pdservice_link")
    private Long pdservice_link;

    @Column(name = "pdservicedetail_link")
    private Long pdservicedetail_link;


}
