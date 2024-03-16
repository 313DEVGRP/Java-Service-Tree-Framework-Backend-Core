package com.arms.api.analysis.scope.dto;

import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreeBarTempDTO {
    private long reqLink;
    private String issueKey;
    private String issueName;
    private String versionSets;
    private int totalAssignee;
    private long totalContribution;


    public TreeBarTempDTO(ReqStatusEntity reqStatusEntity) {
        this.reqLink = reqStatusEntity.getC_req_link();
        this.issueKey = reqStatusEntity.getC_issue_key();
        this.issueName = reqStatusEntity.getC_title();
        this.versionSets = reqStatusEntity.getC_req_pdservice_versionset_link();
        this.totalAssignee = 0;
        this.totalContribution = 0;
    }
}
