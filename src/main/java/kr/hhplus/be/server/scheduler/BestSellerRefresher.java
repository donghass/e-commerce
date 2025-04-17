package kr.hhplus.be.server.scheduler;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BestSellerRefresher {

    private final EntityManager em;

    @Scheduled(cron = "0 0 * * * *") // 매시간 정각
    @Transactional
    public void refreshBestSeller() {
        em.createNativeQuery("TRUNCATE TABLE bestSeller").executeUpdate();
        em.createNativeQuery("""
            INSERT INTO bestSeller (productId, name, price, stock, sales)
            SELECT
                p.id AS productId,
                p.name,
                p.price,
                p.stock,
                SUM(op.quantity) AS sales
            FROM orderProduct op
            JOIN product p ON op.productId = p.id
            GROUP BY p.id
            ORDER BY sales DESC
            LIMIT 5
        """).executeUpdate();
    }
}