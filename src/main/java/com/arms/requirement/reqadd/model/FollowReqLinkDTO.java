package com.arms.requirement.reqadd.model;

import lombok.Data;

@Data
public class FollowReqLinkDTO {

	private Long pdService;
	private Long pdServiceVersion;
	private Long reqAdd;
	private Long jiraServer;
	private Long jiraProject;
}
