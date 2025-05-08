package kr.hhplus.be.server.scheduler;

import java.util.List;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final ScheduleQueryService scheduleQueryService;
    private final OrderFacade orderFacade;


    // 매 5분마다 실행
//    @Scheduled(fixedRate = 300_000)
    public void checkAndExpireOrders() {
        log.info("만료 주문 조회 시작 ");
        List<OrderEntity> expiredOrders = scheduleQueryService.findExpiredOrders();
        for (OrderEntity order : expiredOrders) {
            log.info("만료 주문 단건 " +order.getId());
            List<OrderProductEntity> orderProduct = scheduleQueryService.findByOrder(order.getId());
            orderFacade.expireSingleOrder(order, orderProduct);
        }
    }
}