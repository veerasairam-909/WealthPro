package com.wealthpro.analytics.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Centralized AOP Logging Aspect for the Analytics-Service.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.wealthpro.analytics.controller..*(..))") public void controllerPointcut() {}
    @Pointcut("execution(* com.wealthpro.analytics.service..*(..))") public void servicePointcut() {}

    @Around("controllerPointcut() || servicePointcut()")
    public Object logAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        logger.info(">> ENTER: {}.{}() with arguments = {}", className, methodName, Arrays.toString(joinPoint.getArgs()));
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("<< EXIT:  {}.{}() returned = {} | executed in {} ms", className, methodName, result, executionTime);
            return result;
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("!! ERROR: {}.{}() threw {} after {} ms | message: {}", className, methodName, ex.getClass().getSimpleName(), executionTime, ex.getMessage());
            throw ex;
        }
    }
}
