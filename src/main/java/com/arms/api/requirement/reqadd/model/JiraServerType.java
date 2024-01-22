package com.arms.api.requirement.reqadd.model;

public enum JiraServerType {
    CLOUD("클라우드"),
    ON_PREMISE("온프레미스");

    private final String type;

    JiraServerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static JiraServerType fromString(String type) {
        for (JiraServerType jiraServerType : JiraServerType.values()) {
            if (jiraServerType.getType().equals(type)) {
                return jiraServerType;
            }
        }
        throw new IllegalArgumentException("Invalid Jira Server Type: " + type);
    }
}
