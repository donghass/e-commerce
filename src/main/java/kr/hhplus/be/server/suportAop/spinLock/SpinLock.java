package kr.hhplus.be.server.suportAop.spinLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpinLock {
    String key();         // 락 키 (필수)
    int maxRetry() default 5; // 최대 재시도 횟수
    long sleepMillis() default 50; // 재시도 간 sleep 시간(ms)
}
