package com.arms.api.jira.jiraserver.model.enums;

public enum EntityType {
    프로젝트("almProject"),
    이슈유형("issueType"),
    이슈상태("issueStatus"),
    이슈우선순위("issuePriority"),
    이슈해결책("issueResolution");

    private final String type;

    EntityType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static EntityType fromString(String type) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getType().equals(type)) {
                return entityType;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 엔티티 타입: " + type);
    }
}
