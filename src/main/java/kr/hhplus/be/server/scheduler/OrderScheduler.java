package kr.hhplus.be.server.scheduler;

import kr.hhplus.be.server.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;

    // 매 5분마다 실행
    @Scheduled(fixedRate = 300_000)
    public void checkAndExpireOrders() {
        orderService.expireOldUnpaidOrders();
    }
}