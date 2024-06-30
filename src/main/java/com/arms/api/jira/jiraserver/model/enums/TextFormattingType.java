package com.arms.api.jira.jiraserver.model.enums;

public enum TextFormattingType {

    TEXT("text"),
    HTML("html"),
    MARKDOWN("markdown");

    private final String type;

    TextFormattingType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static TextFormattingType fromString(String type) {
        if (type == null) {
            return TextFormattingType.TEXT;
        }

        for (TextFormattingType textFormattingType : TextFormattingType.values()) {
            if (textFormattingType.getType().equals(type)) {
                return textFormattingType;
            }
        }

        return TextFormattingType.TEXT;
    }
}
