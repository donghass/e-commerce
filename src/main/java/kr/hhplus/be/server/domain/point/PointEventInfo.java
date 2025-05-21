package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.order.OrderEntity;
import lombok.Getter;

@Getter
public class PointEventInfo {
    private OrderEntity order;
    private Long pointHisId;

    public static PointEventInfo save(OrderEntity order, Long pointHisId) {
        PointEventInfo pointEventInfo = new PointEventInfo();
        pointEventInfo.order = order;
        pointEventInfo.pointHisId = pointHisId;
        return pointEventInfo;
    };

}
