package com.arms.api.product_service.pdservice_detail.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PdServiceDetailDTO extends TreeBaseDTO {

    private String c_contents;

    private String c_drawio_contents;

    private String c_drawdb_contents;

    private Instant c_created_at;

    private Instant c_updated_at;

}
