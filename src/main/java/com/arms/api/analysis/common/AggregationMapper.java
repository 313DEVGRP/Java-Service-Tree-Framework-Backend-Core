package com.arms.api.analysis.common;

import com.arms.api.util.communicate.external.request.EngineAggregationRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AggregationMapper {
    EngineAggregationRequestDTO toEngineAggregationRequestDTO(AggregationRequestDTO aggregationRequestDTO);
}