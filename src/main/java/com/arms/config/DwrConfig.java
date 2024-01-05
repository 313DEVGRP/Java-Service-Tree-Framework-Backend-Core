package com.arms.config;

import org.directwebremoting.servlet.DwrServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

@Configuration
public class DwrConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/dwr/**").addResourceLocations("/dwr/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/dwr").setViewName("forward:/dwr/index.html");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setRemoveSemicolonContent(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Bean
    public ServletRegistrationBean<DwrServlet> dwrServletRegistration() {
        DwrServlet servlet = new DwrServlet();
        ServletRegistrationBean<DwrServlet> registration = new ServletRegistrationBean<>(servlet, "/dwr/*");
        registration.setName("dwr");
        registration.addInitParameter("debug", "true");

        //pollAndCometEnabled set to true to increase the loadability of the server, although DWR has a mechanism to protect the server from overload.
        registration.addInitParameter("pollAndCometEnabled", "true");
        registration.addInitParameter("activeReverseAjaxEnabled", "true");

        registration.addInitParameter("initApplicationScopeCreatorsAtStartup", "false");
        registration.addInitParameter("jsonpEnabled", "false");
        registration.addInitParameter("org.directwebremoting.extend.ScriptSessionManager", "com.arms.egovframework.javaservice.treeframework.remote.ScriptSessionManager");
        registration.addInitParameter("allowScriptTagRemoting", "true");

        registration.addInitParameter("maxWaitAfterWrite", "60");
        return registration;
    }

}
