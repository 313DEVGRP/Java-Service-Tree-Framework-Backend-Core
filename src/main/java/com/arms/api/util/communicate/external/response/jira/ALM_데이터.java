package com.arms.api.util.communicate.external.response.jira;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class ALM_데이터 implements Serializable {
    private String self;
    private String id;
}
