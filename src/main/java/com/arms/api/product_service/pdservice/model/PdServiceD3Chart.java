package com.arms.api.product_service.pdservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdServiceD3Chart {

    public String name;
    public String type;
    public List<PdServiceD3Chart> children;

}
