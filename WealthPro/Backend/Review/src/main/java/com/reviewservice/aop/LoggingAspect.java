package com.reviewservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // -------------------------------------------------------
    // Pointcuts
    // -------------------------------------------------------

    @Pointcut("execution(* com.reviewservice.controller..*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.reviewservice.service..*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* com.reviewservice.repository..*(..))")
    public void repositoryLayer() {}

    @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer()")
    public void allLayers() {}

    // -------------------------------------------------------
    // Around Advice — Controller Layer
    // Logs HTTP method, URI, args, return value, execution time
    // -------------------------------------------------------

    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        // Try to get HTTP request details
        String httpMethod = "N/A";
        String requestURI = "N/A";
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                httpMethod  = request.getMethod();
                requestURI  = request.getRequestURI();
            }
        } catch (Exception ignored) {}

        logger.info(">>> REQUEST  [{} {}] {}.{}() | Args: {}",
                httpMethod, requestURI, className, methodName,
                Arrays.toString(joinPoint.getArgs()));

        long start  = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        logger.info("<<< RESPONSE [{} {}] {}.{}() | Completed in {} ms",
                httpMethod, requestURI, className, methodName, elapsed);

        return result;
    }

    // -------------------------------------------------------
    // Around Advice — Service Layer
    // Logs method entry, exit, and execution time
    // -------------------------------------------------------

    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        logger.debug("--> SERVICE {}.{}() | Args: {}",
                className, methodName, Arrays.toString(joinPoint.getArgs()));

        long start   = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed  = System.currentTimeMillis() - start;

        logger.debug("<-- SERVICE {}.{}() | Completed in {} ms",
                className, methodName, elapsed);

        return result;
    }

    // -------------------------------------------------------
    // Around Advice — Repository Layer
    // Logs query method name and execution time
    // -------------------------------------------------------

    @Around("repositoryLayer()")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        logger.debug("--> REPOSITORY {}.{}()",className, methodName);

        long start   = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed  = System.currentTimeMillis() - start;

        logger.debug("<-- REPOSITORY {}.{}() | Completed in {} ms",
                className, methodName, elapsed);

        return result;
    }

    // -------------------------------------------------------
    // AfterThrowing Advice — All Layers
    // Logs any exception thrown across controller/service/repository
    // -------------------------------------------------------

    @AfterThrowing(pointcut = "allLayers()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        logger.error("!!! EXCEPTION in {}.{}() | Type: {} | Message: {}",
                className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }
}