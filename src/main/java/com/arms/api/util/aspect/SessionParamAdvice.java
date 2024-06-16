package com.arms.api.util.aspect;

import com.arms.api.util.slack.SlackNotificationService;
import com.arms.api.util.slack.SlackProperty;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.connector.RequestFacade;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class SessionParamAdvice {

    private final SlackNotificationService slackNotificationService;
    private final String appName;

    public SessionParamAdvice(SlackNotificationService slackNotificationService
            , @Value("${spring.application.name}") String appName) {
        this.slackNotificationService = slackNotificationService;
        this.appName = appName;
    }

    @Around("execution(* com.arms..controller.*.*(..))")
    public Object sessionParam(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();

        String methodName
                = Arrays.stream(joinPoint.getSignature().toLongString().split(" ")).skip(1).collect(Collectors.joining(" "));
        try{
            return joinPoint.proceed();
        }catch (Exception e){
            slackNotificationService.sendMessageToChannel(SlackProperty.Channel.backend, e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            List<Object> argsObject = Arrays.stream(args).filter(a -> !(a instanceof RequestFacade))
                .collect(Collectors.toList());

            if(args.length>0){
                Arrays.stream(args)
                    .filter(a -> a instanceof RequestFacade).findFirst().map(a -> (RequestFacade)a).filter(a->a.getSession()!=null).ifPresentOrElse(a->{
                        for (Object arg : argsObject) {
                            log.error("{} Error 발생\tmethodName : {}\tsession    : {}\tparameter   : {}\terrorMsg    : {}",appName,methodName,a.getSession().getId(),arg,errors);
                        }
                    },()->{
                        for (Object arg : argsObject) {
                            log.error("{} Error 발생\tmethodName : {}\tparameter   : {}\terrorMsg    : {}",appName,methodName,arg,errors);
                        }
                    });
            }

            throw e;
        }

    }
}
