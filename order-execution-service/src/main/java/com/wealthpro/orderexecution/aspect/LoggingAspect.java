package com.wealthpro.orderexecution.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Centralized AOP Logging Aspect for the Order-Execution-Service.
 * <p>
 * Intercepts all public method calls in the controller and service layers
 * to log method entry (with arguments), exit (with return value), and
 * execution time — eliminating repetitive logging from business logic.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut matching all public methods in the controller package.
     */
    @Pointcut("execution(* com.wealthpro.orderexecution.controller..*(..))")
    public void controllerPointcut() {
    }

    /**
     * Pointcut matching all public methods in the service package.
     */
    @Pointcut("execution(* com.wealthpro.orderexecution.service..*(..))")
    public void servicePointcut() {
    }

    /**
     * Around advice that logs method name, arguments, return value, and execution time.
     *
     * @param joinPoint the proceeding join point
     * @return the result of the target method
     * @throws Throwable if the target method throws
     */
    @Around("controllerPointcut() || servicePointcut()")
    public Object logAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // Build a readable method signature: ClassName.methodName
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Log method entry with arguments
        logger.info(">> ENTER: {}.{}() with arguments = {}", className, methodName,
                Arrays.toString(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();

        try {
            // Proceed with the actual method execution
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Log method exit with return value and timing
            logger.info("<< EXIT:  {}.{}() returned = {} | executed in {} ms",
                    className, methodName, result, executionTime);

            return result;
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;

            // Log exception details
            logger.error("!! ERROR: {}.{}() threw {} after {} ms | message: {}",
                    className, methodName, ex.getClass().getSimpleName(),
                    executionTime, ex.getMessage());

            throw ex;
        }
    }
}
