package com.arms.dashboard.model.resource;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AssigneeData {
    private long requirements;
    private long issues;
    private String displayName;
    private Map<String, Long> issueTypes;
    private Map<String, Long> priorities;
    private Map<String, Long> statuses;
    private Map<String, Long> resolutions;
}
