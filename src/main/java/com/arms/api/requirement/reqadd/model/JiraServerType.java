package com.arms.api.requirement.reqadd.model;

public enum JiraServerType {
    CLOUD("클라우드"),
    ON_PREMISE("온프레미스"),
    REDMINE_ON_PREMISE("레드마인_온프레미스");

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
        throw new IllegalArgumentException("유효하지 않은 지라 서버 타입: " + type);
    }
}
