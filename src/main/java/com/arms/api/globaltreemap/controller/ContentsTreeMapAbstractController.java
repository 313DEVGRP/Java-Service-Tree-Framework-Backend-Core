package com.arms.api.globaltreemap.controller;

import com.arms.api.globaltreemap.service.GlobalContentsTreeMapService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@Api("ContentsTreeMapFramework")
public abstract class ContentsTreeMapAbstractController {

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected GlobalContentsTreeMapService globalContentsTreeMapService;


}
