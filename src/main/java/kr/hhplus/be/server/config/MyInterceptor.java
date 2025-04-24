package kr.hhplus.be.server.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class MyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("요청 전 처리 - preHandle");
        // GET 요청에서 userId 파라미터 추출
        String userIdParam = request.getParameter("userId");

        if (userIdParam != null) {
            Long userId = Long.parseLong(userIdParam);
            System.out.println("검증용 userId: " + userId);

            // 검증 로직 예시
            if (userId <= 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 userId입니다.");
                return false;
            }
            // true면 계속 진행, false면 중단
            return true;
        }
        // userId가 없는 요청은 거부할 수도 있음
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "userId가 필요합니다.");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("요청 후 처리 - afterCompletion");
    }
}