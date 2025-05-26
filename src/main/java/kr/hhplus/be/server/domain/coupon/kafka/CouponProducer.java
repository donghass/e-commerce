package kr.hhplus.be.server.domain.coupon.kafka;

import kr.hhplus.be.server.application.coupon.CouponIssueCommand;

public interface CouponProducer {
    void publishCompleted(CouponIssueCommand command);
}