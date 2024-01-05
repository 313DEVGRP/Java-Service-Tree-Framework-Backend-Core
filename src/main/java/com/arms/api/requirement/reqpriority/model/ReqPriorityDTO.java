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
package com.arms.api.requirement.reqpriority.model;

import lombok.*;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqPriorityDTO extends TreeBaseDTO {

    //내용
    private String c_contents;

    //설명
    private String c_desc;

    //비고
    private String c_etc;

}
