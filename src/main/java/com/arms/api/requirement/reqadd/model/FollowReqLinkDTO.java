package com.arms.api.requirement.reqadd.model;

import lombok.Data;

import java.util.List;

@Data
public class FollowReqLinkDTO {

	private Long pdService;
	private List<Long> pdServiceVersion;
	private Long reqAdd;
	private Long jiraServer;
	private Long jiraProject;
}
