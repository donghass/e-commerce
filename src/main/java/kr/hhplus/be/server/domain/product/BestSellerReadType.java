package kr.hhplus.be.server.domain.product;

// 조회 방식에 따른 Enum 정의
public enum BestSellerReadType {
    SCHEDULED, // 스케줄러에서 조회
    MANUAL;    // 수동으로 조회
}