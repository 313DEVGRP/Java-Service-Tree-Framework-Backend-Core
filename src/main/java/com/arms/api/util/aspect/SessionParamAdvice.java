package com.arms.api.util.aspect;

import com.arms.notification.slack.SlackNotificationService;
import com.arms.notification.slack.SlackProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
@AllArgsConstructor
public class SessionParamAdvice {

    private final SlackNotificationService slackNotificationService;

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
            for (Object arg : args) {
                log.error("※ERROR 발생\nmethodName : {}\nsession    : {}\nparameter   : {}\nerrorMsg    : {}",methodName,request.getSession().getId(),arg,errors);
            }
            throw e;
        }

    }
}
