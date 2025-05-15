package kr.hhplus.be.server.domain.coupon;

import java.time.Duration;

public interface CouponRedisRepository {

    Long tryIssue(String issuedKey, String stockKey, String string);
//    Long tryIssueCouponWithLimit(String issuedKey, Long userId, Long stockLimit);

}
