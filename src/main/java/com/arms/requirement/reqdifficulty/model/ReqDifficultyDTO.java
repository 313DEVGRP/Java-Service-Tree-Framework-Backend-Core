/*
 * @author Dongmin.lee
 * @since 2023-10-22
 * @version 23.10.22
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.requirement.reqdifficulty.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqDifficultyDTO extends TreeBaseDTO {

  
}
