package com.arms.api.product_service.pdservice_pure.model;

import com.arms.api.product_service.pdservice_detail.model.PdServiceDetailEntity;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
public class PdServiceWithDetailDTO {

    PdServicePureEntity pdServicePure;
    List<PdServiceDetailEntity> pdServiceDetails;

    @Builder
    private PdServiceWithDetailDTO(PdServicePureEntity pdServicePure, List<PdServiceDetailEntity> pdServiceDetails) {
        this.pdServicePure = pdServicePure;
        this.pdServiceDetails = pdServiceDetails;
    }
}
