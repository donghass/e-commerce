package kr.hhplus.be.server.domain.redis;

import java.util.Optional;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.point.ChargePointCommand;
import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderEntity;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.ProductService;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceWithRedisson {

    private final RedissonClient redissonClient;
    private final OrderService orderService;
//  주문시 재고 락 걸기
    public Long createOrder(OrderCommand command) {
        String lockKey = "lockPoint:" + command.userId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        Long orderId;
        try {
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            // 최대 5초간 기다리고, 락은 3초동안 유지

            if (!locked) {
                throw new RuntimeException("쿠폰 락 획득 실패");
            }

            // 락 잡은 후 쿠폰 발급 로직 실행
            orderId = orderService.createOrder(command);

        } catch (InterruptedException e) {
            throw new RuntimeException("락 대기 중 인터럽트", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return orderId;
    }

//  주문 취소시 재고 락
    public void expireSingleOrder(OrderEntity order, OrderProductEntity orderProduct) {
        String lockKey = "lockProduct:" + orderProduct.getProductId();
        RLock lock = redissonClient.getLock(lockKey);

        ProductEntity product;
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            // 최대 5초간 기다리고, 락은 3초동안 유지

            if (!locked) {
                throw new RuntimeException("쿠폰 락 획득 실패");
            }

            // 락 잡은 후 쿠폰 발급 로직 실행
            orderService.expireSingleOrder(order, orderProduct);

        } catch (InterruptedException e) {
            throw new RuntimeException("락 대기 중 인터럽트", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
