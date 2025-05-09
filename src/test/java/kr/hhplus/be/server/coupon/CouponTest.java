package kr.hhplus.be.server.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyService;
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // 필수
public class CouponTest {
    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;
    @Mock
    private ConcurrencyService concurrencyService;


    @Test
    void createCoupon() {
        // Arrange
        Long userId = 1L;
        Long couponId = 123L;

        // Mock CouponEntity
        CouponEntity mockCoupon = new CouponEntity(couponId,"할인쿠폰",20L,DiscountType.RATE,null,null,10L,null,null);

        CouponIssueCommand command = new CouponIssueCommand(userId, couponId);

        // Mocking repository calls
//        when(couponRepository.findById(couponId)).thenReturn(Optional.of(mockCoupon));
        when(userCouponRepository.findByCouponId(couponId)).thenReturn(Optional.empty());
        doNothing().when(userCouponRepository).save(any(UserCouponEntity.class));  // void 메서드에는 doNothing() 사용
        //      ConcurrencyService mock 세팅
        when(concurrencyService.couponDecreaseStock(couponId)).thenReturn(mockCoupon);

        // Act
        couponService.createCoupon(command);

        // Assert
//        verify(couponRepository).findById(couponId);  // coupon 조회
        verify(concurrencyService).couponDecreaseStock(couponId);  // coupon 조회
        verify(userCouponRepository).findByCouponId(couponId);  // 이미 발급된 쿠폰이 있는지 확인
        verify(userCouponRepository).save(any(UserCouponEntity.class));  // 사용자 쿠폰 저장
    }


    @Test
    void validateCoupon_fail() {
        // Arrange
        // Mock UserCouponEntity
        UserCouponEntity mockUserCoupon = new UserCouponEntity(1L, 1L, 1L, true, "할인쿠폰",null,LocalDateTime.now().plusDays(7),LocalDateTime.now(),LocalDateTime.now());  // 만료일을 7일 뒤로 설정


        // Act & Assert
        assertThrows(BusinessException.class, () -> mockUserCoupon.validateCoupon(mockUserCoupon));
    }
    @Test
    void validateCoupon_success() {
        // Arrange
        // Mock UserCouponEntity
        UserCouponEntity mockUserCoupon = new UserCouponEntity(1L, 1L, 1L, false, "할인쿠폰",null,LocalDateTime.now().plusDays(7),LocalDateTime.now(),LocalDateTime.now());  // 만료일을 7일 뒤로 설정


        // Act & Assert
        assertDoesNotThrow(() -> mockUserCoupon.validateCoupon(mockUserCoupon));
    }
}
