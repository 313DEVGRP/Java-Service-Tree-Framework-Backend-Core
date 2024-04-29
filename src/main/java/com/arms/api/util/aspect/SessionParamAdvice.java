package com.arms.api.util.aspect;

import com.arms.notification.slack.SlackNotificationService;
import com.arms.notification.slack.SlackProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
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
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String methodName
                = Arrays.stream(joinPoint.getSignature().toLongString().split(" ")).skip(1).collect(Collectors.joining(" "));
        try{
            return joinPoint.proceed();
        }catch (Exception e){
            slackNotificationService.sendMessageToChannel(SlackProperty.Channel.backend, e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            if(args.length>0){
                for (Object arg : args) {
                    log.error("{} Error 발생\tmethodName : {}\tsession    : {}\tparameter   : {}\terrorMsg    : {}",appName,methodName,request.getSession().getId(),arg,errors);
                }
            }else{
                log.error("{} Error 발생\tmethodName : {}\tsession    : {}\terrorMsg    : {}",appName,methodName,request.getSession().getId(),errors);
            }

            throw e;
        }

    }
}
