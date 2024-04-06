package com.arms.api.jira.jiraserver.model.enums;

public enum ServerType {
    JIRA_CLOUD("클라우드"),
    JIRA_ON_PREMISE("온프레미스"),
    REDMINE_ON_PREMISE("레드마인_온프레미스");

    private final String type;

    ServerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ServerType fromString(String type) {
        for (ServerType serverType : ServerType.values()) {
            if (serverType.getType().equals(type)) {
                return serverType;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 지라 서버 타입: " + type);
    }
}
