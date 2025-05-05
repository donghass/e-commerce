package kr.hhplus.be.server.suportAop.spinLock;

import kr.hhplus.be.server.suportAop.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpinLockAop {

    private static final String REDISSON_LOCK_PREFIX = "lock:";

    private final LockManager lockManager;

    @Around("@annotation(kr.hhplus.be.server.suportAop.spinLock.SpinLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SpinLock distributedLock = method.getAnnotation(SpinLock.class);

        String lockKey = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        int maxRetry = distributedLock.maxRetry();
        long sleepMillis = distributedLock.sleepMillis();

        lockManager.lock(lockKey, maxRetry, sleepMillis);

        try {
            return joinPoint.proceed();
        } finally {
            lockManager.unlock(lockKey);
        }
    }
}
