package com.arms.외부연동;

import lombok.*;

import java.net.URI;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueType {

    private URI self;
    private Long id;
    private String name;
    private boolean isSubtask;
    private String description;
    private URI iconUri;

}
