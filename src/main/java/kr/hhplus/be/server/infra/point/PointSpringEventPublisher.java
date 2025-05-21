package kr.hhplus.be.server.infra.point;

import kr.hhplus.be.server.domain.point.PointEvent;
import kr.hhplus.be.server.domain.point.PointEventInfo;
import kr.hhplus.be.server.domain.point.PointEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointSpringEventPublisher implements PointEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishPointUsedEvent(PointEventInfo pointEventInfo) {
        applicationEventPublisher.publishEvent(new PointEvent.PointUsedCompleted(pointEventInfo));
    }
}
