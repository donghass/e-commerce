package kr.hhplus.be.server.domain.order;

public interface DataPlatformClient {

    void sendToDataPlatform(OrderEntity order);
}
