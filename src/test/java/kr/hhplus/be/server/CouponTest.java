package kr.hhplus.be.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        // Mock UserCouponEntity
        UserCouponEntity mockUserCoupon = new UserCouponEntity();
        mockUserCoupon.setCouponId(couponId);
        mockUserCoupon.setExpiredAt(LocalDateTime.now().plusDays(7));  // 만료일을 7일 뒤로 설정

        // Mock CouponEntity
        CouponEntity mockCoupon = new CouponEntity();
        mockCoupon.setId(couponId);
        mockCoupon.setDiscountValue(20L); // 20% 할인
        mockCoupon.setDiscountType(DiscountType.RATE); // 정률 할인

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

        CouponEntity mockCoupon = new CouponEntity();
        mockCoupon.setId(couponId);
        mockCoupon.setStock(10L);
        mockCoupon.setName("Discount Coupon");

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
    void validateCoupon_success() {
        // Arrange
        UserCouponEntity userCouponEntity = new UserCouponEntity();
        userCouponEntity.setExpiredAt(LocalDateTime.now());
        userCouponEntity.setUsed(true);


        // Act & Assert
        assertThrows(BusinessException.class, () -> userCouponEntity.validateCoupon(userCouponEntity));
    }
}
