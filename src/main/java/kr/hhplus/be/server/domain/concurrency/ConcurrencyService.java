package kr.hhplus.be.server.domain.concurrency;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponEntity;
import kr.hhplus.be.server.domain.coupon.execption.CouponErrorCode;
import kr.hhplus.be.server.domain.product.ProductEntity;
import kr.hhplus.be.server.domain.product.ProductRepository;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcurrencyService {
    private final ConcurrencyProductRepository concurrencyProductRepository;
    private final ConcurrencyCouponRepository concurrencyCouponRepository;

    public ProductEntity productDecreaseStock(Long productId) {
        return concurrencyProductRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));
    }

    public CouponEntity couponDecreaseStock(Long couponId) {
        return concurrencyCouponRepository.findById(couponId)
            .orElseThrow(() -> new BusinessException(CouponErrorCode.INVALID_COUPON_ID));
    }
}