package com.arms.api.requirement.reqreviewcommentlog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class ReqReviewCommentLogDTO extends TreeBaseDTO {

    private Long c_pdservice_link;

    private Long c_version_link;

    private Long c_req_link;

    private Long c_req_review_link;

    private String c_req_review_sender;

    private String c_req_review_comment_date;

    private String c_req_review_comment_contents;

    private String c_req_review_etc;

}
