package kr.hhplus.be.server.config.jpa.request;

import lombok.Getter;

// 인기 상품 조회 요청 DTO
@Getter
public class PopularProductRequest {
    private String startDate;  // 조회 시작일 (yyyy-MM-dd 형식)
    private String endDate;    // 조회 종료일 (yyyy-MM-dd 형식)

    private int id;
    private String name;
    private long price;
    private int sales;
    private int stock;

}

