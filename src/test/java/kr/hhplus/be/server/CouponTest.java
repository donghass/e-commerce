package kr.hhplus.be.server;

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
import kr.hhplus.be.server.domain.coupon.CouponDiscountResult;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.CouponEntity.DiscountType;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.coupon.UserCouponEntity;
import kr.hhplus.be.server.domain.coupon.UserCouponRepository;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import kr.hhplus.be.server.domain.point.PointEntity;
import kr.hhplus.be.server.domain.point.PointHistoryEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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

    @Test
    void useCoupon() {
        // Arrange
        Long userCouponId = 1L;
        Long couponId = 123L;
        Long userId = 1L;
        String name = "가입축하 쿠폰";
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
        // Mock UserCouponEntity
        UserCouponEntity mockUserCoupon = new UserCouponEntity(userId, userId, couponId, false, name,null,expiredAt,LocalDateTime.now(),LocalDateTime.now());  // 만료일을 7일 뒤로 설정

        // Mock CouponEntity
        CouponEntity mockCoupon = new CouponEntity(1L,name,20L,DiscountType.RATE,null,null,5L,null,null);


        // Mocking repository calls
        when(userCouponRepository.findById(userCouponId)).thenReturn(Optional.of(mockUserCoupon));
        when(couponRepository.findByCouponId(couponId)).thenReturn(Optional.of(mockCoupon));

        // Act
        CouponDiscountResult couponDiscountResult = couponService.useCoupon(userCouponId);

        // Assert with AssertJ
        assertThat(couponDiscountResult)
            .isNotNull()
            .extracting(CouponDiscountResult::discountValue, CouponDiscountResult::discountType)
            .containsExactly(20L, DiscountType.RATE);  // 20% 할인, 정률 할인

        // Verifying repository interactions
        verify(userCouponRepository).findById(userCouponId);
        verify(couponRepository).findByCouponId(couponId);
    }

    @Test
    void createCoupon() {
        // Arrange
        Long userId = 1L;
        Long couponId = 123L;

        // Mock CouponEntity
        CouponEntity mockCoupon = new CouponEntity(couponId,"할인쿠폰",20L,DiscountType.RATE,null,null,10L,null,null);

        CouponIssueCommand command = new CouponIssueCommand(userId, couponId);

        // Mocking repository calls
        when(couponRepository.findByCouponId(couponId)).thenReturn(Optional.of(mockCoupon));
        when(userCouponRepository.findByCouponId(couponId)).thenReturn(Optional.empty());
        doNothing().when(couponRepository).updateCouponStock(couponId, 9L);  // 쿠폰 수량 차감
        doNothing().when(userCouponRepository).saveCoupon(any(UserCouponEntity.class));  // void 메서드에는 doNothing() 사용

        // Act
        couponService.createCoupon(command);

        // Assert
        verify(couponRepository).findByCouponId(couponId);  // coupon 조회
        verify(userCouponRepository).findByCouponId(couponId);  // 이미 발급된 쿠폰이 있는지 확인
        verify(couponRepository).updateCouponStock(couponId, 9L);  // 쿠폰 수량 차감
        verify(userCouponRepository).saveCoupon(any(UserCouponEntity.class));  // 사용자 쿠폰 저장
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
