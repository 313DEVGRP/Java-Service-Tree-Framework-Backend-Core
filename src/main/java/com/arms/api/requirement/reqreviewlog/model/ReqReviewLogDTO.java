package com.arms.api.requirement.reqreviewlog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class ReqReviewLogDTO extends TreeBaseDTO {


    private Long c_pdservice_link;

    private Long c_version_link;

    private Long c_req_link;

    private String c_review_sender;

    private String c_review_responder;

    private String c_review_creat_date;

    private String c_review_result_state;

    private String c_review_result_date;

}
