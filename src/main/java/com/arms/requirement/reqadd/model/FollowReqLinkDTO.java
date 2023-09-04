package com.arms.requirement.reqadd.model;

import lombok.Data;

@Data
public class FollowReqLinkDTO {

	private Long pdServiceId;
	private Long pdServiceVersionId;
	private Long reqAddId;
	private Long jiraServerId;
	private Long jiraProjectId;
}
