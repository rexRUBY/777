package org.example.common.common.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(org.example.common.common.log.LogExecution)")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();

        // 요청 로그
        logger.info("[AOP_LOG] Request - Method: {}, Arguments: {}", methodName, methodArgs);

        Object result;
        try {
            // 메서드 실행
            result = joinPoint.proceed();
        } catch (Exception e) {
            // 에러 로그
            logger.error("[AOP_LOG] Error in Method: {}, Message: {}", methodName, e.getMessage(), e);
            throw e;
        }

        // 응답 로그
        logger.info("[AOP_LOG] Response - Method: {}, Result: {}", methodName, result);
        return result;
    }
}
