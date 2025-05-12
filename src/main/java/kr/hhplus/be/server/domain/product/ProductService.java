package kr.hhplus.be.server.domain.product;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.application.product.BestSellerResult;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.concurrency.ConcurrencyService;
import kr.hhplus.be.server.domain.order.OrderProductEntity;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import kr.hhplus.be.server.suportAop.redissonLock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final BestSellerRepository bestSellerRepository;
    private final ConcurrencyService concurrencyService;
    private final RedisRepository redisRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 상품 리스트 조회
    public Page<ProductResult> readProductList(Pageable pageable) {
        Page<ProductEntity> product = productRepository.findPagedProducts(pageable);

        return product.map(p -> new ProductResult(
            p.getId(),
            p.getName(),
            p.getPrice(),
            p.getStock()
        ));
    }

    //주문 총액 조회  // 사용 안함
    public Long readOrderProduct(List<OrderProduct> orderProduct) {
        Long totalAmount = 0L;
        for (int i = 0; i < orderProduct.size(); i++) {
            Long productId = orderProduct.get(i).productId();
            Long quantity = orderProduct.get(i).quantity();
//            Long updateQuantity;

            // 재고 조회 로직 (예시)
            ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

            totalAmount += product.getPrice() * quantity;
        }
        // 주문 총액
        return totalAmount;
    }

    public List<BestSellerResult> bestSellerList(BestSellerReadType bestSellerReadType) {
        List<BestSellerEntity> bestSeller = List.of();
        //  캐시갱신 스케줄러가 아닌 일반 인기상품 조회시 캐시에서 가져오고 캐시가 비어있을 경우 DB 조회
        if (bestSellerReadType == BestSellerReadType.MANUAL) {
            bestSeller = redisRepository.getCachedBestSellerList();

            if (bestSeller.isEmpty()) {
                bestSeller = bestSellerRepository.findAll();
                redisRepository.saveBestSellerList(bestSeller);
            }
        } else if (bestSellerReadType.equals(BestSellerReadType.SCHEDULED)) {
            bestSeller = bestSellerRepository.findAll();
        }
        return bestSeller.stream()
            .map(b -> new BestSellerResult(
                b.getProductId(),
                b.getName(),
                b.getPrice(),
                b.getStock(),
                b.getSales()
            ))
            .collect(Collectors.toList());
    }

    @DistributedLock(key = "'product:' + #orderProduct.productId")
    public void expireOrder(OrderProductEntity orderProduct) {
        ProductEntity product = concurrencyService.productDecreaseStock(
            orderProduct.getProductId());

        product.plusStock(orderProduct.getQuantity());
        productRepository.save(product);
    }

    @DistributedLock(key = "'product:' + #orderProduct.productId")
    public void expireFailOrder(OrderProductEntity orderProduct) {
        ProductEntity product = concurrencyService.productDecreaseStock(
            orderProduct.getProductId());

        product.updateStock(orderProduct.getQuantity());
        productRepository.save(product);
    }

    @DistributedLock(key = "'product:' + #op.productId")
    public ProductEntity decreaseSingleStock(OrderProduct op) {

        try {
            ProductEntity product = concurrencyService.productDecreaseStock(op.productId());
            log.info("락상품 재고 = " + product.getStock());
            product.updateStock(op.quantity());
            return productRepository.save(product);
        } catch (Exception e) {
            log.error("락 획득 실패 또는 재고 차감 실패: ", e);
            throw new RuntimeException("락 획득 실패", e);
        }
    }

    public void rollbackStock(List<OrderProduct> items, List<ProductEntity> products) {
        for (int i = 0; i < items.size(); i++) {
            OrderProduct op = items.get(i);
            try {
                productSingleRollback(op);
            } catch (Exception e) {
                log.error("재고 증가 실패 (productId={})", op.productId(), e);
            }
        }
    }

    @DistributedLock(key = "'product:' + #op.productId")
    private void productSingleRollback(OrderProduct op) {
        ProductEntity product = concurrencyService.productDecreaseStock(op.productId());
        product.plusStock(op.quantity());
        productRepository.save(product);
    }

    public List<ProductResult> getTopRankedProducts(int size) {
        String key = "ranking:daily:" + LocalDate.now();

        // Redis에서 TOP 10 상품 ID를 스코어 순서대로 조회
        Set<Long> productIds = redisRepository.getTopProducts(key, size);

        // 상품 정보를 DB에서 조회하여 RANKING 순서대로 담기
        return productIds.stream()
            .map(this::toResult)
            .collect(Collectors.toList());
    }

    private ProductResult toResult(Long productId) {
        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

        return new ProductResult(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock()
        );
    }

    public void increaseProductScore(Long productId, Long quantity) {
        String key = "ranking:daily:" + LocalDate.now();
        redisRepository.increaseScore(key, productId, quantity);
    }
}