package kr.hhplus.be.server.domain.point;

public interface PointEventPublisher {
    void publishPointUsedEvent(PointEventInfo event);
}