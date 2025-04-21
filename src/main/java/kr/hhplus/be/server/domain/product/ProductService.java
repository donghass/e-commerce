package kr.hhplus.be.server.domain.product;


import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.application.order.OrderCommand.OrderProduct;
import kr.hhplus.be.server.application.product.BestSellerResult;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.product.execption.ProductErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BestSellerRepository bestSellerRepository;
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
    public Long readOrderProduct(List<OrderProduct> orderProduct){
        Long totalAmount = 0L;
        for(int i = 0; i < orderProduct.size(); i++){
            Long productId = orderProduct.get(i).productId();
            Long quantity = orderProduct.get(i).quantity();
//            Long updateQuantity;

            // 재고 조회 로직 (예시)
            ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.INVALID_PRODUCT_ID));

//            product.updateStock(quantity);
//            Long currentStock = product.getStock();
//            if (quantity > currentStock) {
//                throw new BusinessException(ProductErrorCode.INVALID_QUANTITY);
//            }
//            // 재고 차감
//            updateQuantity = currentStock - quantity;
//            productRepository.save(product);

            totalAmount += product.getPrice() * quantity;
        }
        // 주문 총액
        return totalAmount;
    }

    public List<BestSellerResult> bestSellerList() {

        List<BestSellerEntity> bestSeller = bestSellerRepository.findAll();

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
}
