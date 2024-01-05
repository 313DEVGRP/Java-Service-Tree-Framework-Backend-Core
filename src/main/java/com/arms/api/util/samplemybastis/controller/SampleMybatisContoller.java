package com.arms.api.util.samplemybastis.controller;

import com.arms.api.util.samplemybastis.service.MyBatisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping(value = {"/arms/sample/mybatis"})
public class SampleMybatisContoller {

    @Autowired
    @Qualifier("myBatisService")
    private MyBatisService myBatisService;

    @ResponseBody
    @GetMapping("/getList")
    public ModelAndView getList() throws Exception {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", myBatisService.getList());

        return modelAndView;
    }

}
