package com.wealthpro.productcatalog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);


    @Pointcut("execution(* com.wealthpro.productcatalog.controller.*.*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.wealthpro.productcatalog.service.*.*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.wealthpro.productcatalog.repository.*.*(..))")
    public void repositoryLayer() {}

    @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer()")
    public void allLayers() {}

    @Before("controllerLayer()")
    public void logBeforeController(JoinPoint joinPoint) {
        logger.info(
                "[BEFORE] [CONTROLLER] {}.{}() | Args: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    @Before("serviceLayer()")
    public void logBeforeService(JoinPoint joinPoint) {
        logger.info(
                "[BEFORE] [SERVICE] {}.{}() | Args: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    @Before("repositoryLayer()")
    public void logBeforeRepository(JoinPoint joinPoint) {
        logger.debug(
                "[BEFORE] [REPOSITORY] {}.{}() | Args: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    @After("controllerLayer()")
    public void logAfterController(JoinPoint joinPoint) {
        logger.info(
                "[AFTER] [CONTROLLER] {}.{}() | Execution finished",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName()
        );
    }

    @After("serviceLayer()")
    public void logAfterService(JoinPoint joinPoint) {
        logger.info(
                "[AFTER] [SERVICE] {}.{}() | Execution finished",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName()
        );
    }

    @After("repositoryLayer()")
    public void logAfterRepository(JoinPoint joinPoint) {
        logger.debug(
                "[AFTER] [REPOSITORY] {}.{}() | Execution finished",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName()
        );
    }

    @AfterReturning(pointcut = "controllerLayer()", returning = "result")
    public void logAfterReturningController(JoinPoint joinPoint, Object result) {
        logger.info(
                "[AFTER_RETURNING] [CONTROLLER] {}.{}() | Return: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                result
        );
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturningService(JoinPoint joinPoint, Object result) {
        logger.info(
                "[AFTER_RETURNING] [SERVICE] {}.{}() | Return: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                result
        );
    }

    @AfterReturning(pointcut = "repositoryLayer()", returning = "result")
    public void logAfterReturningRepository(JoinPoint joinPoint, Object result) {
        logger.debug(
                "[AFTER_RETURNING] [REPOSITORY] {}.{}() | Return: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                result
        );
    }

    @AfterThrowing(pointcut = "controllerLayer()", throwing = "exception")
    public void logAfterThrowingController(JoinPoint joinPoint, Throwable exception) {
        logger.error(
                "[AFTER_THROWING] [CONTROLLER] {}.{}() | Exception: {} | Message: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    public void logAfterThrowingService(JoinPoint joinPoint, Throwable exception) {
        logger.error(
                "[AFTER_THROWING] [SERVICE] {}.{}() | Exception: {} | Message: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
    }

    @AfterThrowing(pointcut = "repositoryLayer()", throwing = "exception")
    public void logAfterThrowingRepository(JoinPoint joinPoint, Throwable exception) {
        logger.error(
                "[AFTER_THROWING] [REPOSITORY] {}.{}() | Exception: {} | Message: {}",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );
    }

    @Around("controllerLayer()")
    public Object logAroundController(ProceedingJoinPoint pjp) throws Throwable {
        return executeWithLogging(pjp, "CONTROLLER");
    }

    @Around("serviceLayer()")
    public Object logAroundService(ProceedingJoinPoint pjp) throws Throwable {
        return executeWithLogging(pjp, "SERVICE");
    }

    @Around("repositoryLayer()")
    public Object logAroundRepository(ProceedingJoinPoint pjp) throws Throwable {
        return executeWithLogging(pjp, "REPOSITORY");
    }

    private Object executeWithLogging(ProceedingJoinPoint pjp, String layer) throws Throwable {
        String className  = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        logger.info(
                "[AROUND] [{}] --> Entering {}.{}() | Args: {}",
                layer, className, methodName, Arrays.toString(pjp.getArgs())
        );

        long start = System.currentTimeMillis();

        try {
            Object result  = pjp.proceed();
            long timeTaken = System.currentTimeMillis() - start;

            logger.info(
                    "[AROUND] [{}] <-- Exiting {}.{}() | Time: {} ms | Status: SUCCESS",
                    layer, className, methodName, timeTaken
            );

            return result;

        } catch (Throwable ex) {
            long timeTaken = System.currentTimeMillis() - start;

            logger.error(
                    "[AROUND] [{}] <-- Exiting {}.{}() | Time: {} ms | Status: FAILED | Exception: {} | Message: {}",
                    layer, className, methodName, timeTaken,
                    ex.getClass().getSimpleName(), ex.getMessage()
            );

            throw ex;
        }
    }
}