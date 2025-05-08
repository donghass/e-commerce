package kr.hhplus.be.server.config.aop;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)  // AOP 활성화, CGLIB 기반 클래스 프록시 사용
public class AopConfig {
    // 별도의 로직 없이 AOP 활성화를 위한 설정만 해줍니다.
}
