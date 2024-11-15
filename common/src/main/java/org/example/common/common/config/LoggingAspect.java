package org.example.common.common.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(org.example.common.common.log.LogExecution)") // 대상 메서드 패턴 수정
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // AOP에서 호출된 메서드 이름과 클래스 이름 가져오기
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();

        // MDC에 필드 추가
        MDC.put("method", methodName);  // 메서드 이름
        MDC.put("module", className);  // 클래스 이름
        MDC.put("aop_type", "Request"); // AOP 요청 타입

        // 로그 메시지 출력 (Request)
        logger.info("Method [{}] in [{}] started with AOP type: Request", methodName, className);

        Object result;
        try {
            // 원래의 메서드 실행
            result = joinPoint.proceed();

            // AOP 타입을 Response로 변경
            MDC.put("aop_type", "Response");
            logger.info("Method [{}] in [{}] completed successfully with AOP type: Response", methodName, className);

        } catch (Exception e) {
            // AOP 타입을 Error로 변경
            MDC.put("aop_type", "Error");
            logger.error("Method [{}] in [{}] encountered an error: {}", methodName, className, e.getMessage(), e);
            throw e; // 예외를 다시 던져 호출 측에서 처리
        } finally {
            MDC.clear(); // MDC 값 제거
        }

        return result;
    }
}
