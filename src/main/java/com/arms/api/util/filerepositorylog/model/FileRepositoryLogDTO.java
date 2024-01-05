package com.arms.api.util.filerepositorylog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileRepositoryLogDTO extends TreeBaseDTO {

    private String fileName;
    private String contentType;
    private String serverSubPath;
    private String physicalName;
    private Long size;
    private String name;
    private String url;
    private String thumbnailUrl;
    private String delete_url;
    private String delete_type;

}
