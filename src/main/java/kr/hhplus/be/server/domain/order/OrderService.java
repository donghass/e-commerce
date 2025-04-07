package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.application.order.OrderDto;
import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.execption.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository OrderRepository;


    // 포인트 충전
    @Transactional  // JPA 영속성 때문에 save 하지 않아도 자동으로 충전금액 update 된다 // 신규 데이터 추가일 경우엔 안됨
    public OrderDto createOrder(ChargePointCommand command) {
        OrderEntity order = orderRepository.findByUserId(command.userId())
            .orElseThrow(() -> new BusinessException(OrderErrorCode.INVALID_USER_ID));

        return new OrderDto();
    }

}
