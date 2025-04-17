package kr.hhplus.be.server.api.point;

import kr.hhplus.be.server.application.point.ChargePointCommand;
import lombok.Getter;

public record ChargePointRequest(Long userId, Long amount) {

    public ChargePointCommand toCommand() {
        return new ChargePointCommand(userId, amount);
    }
}


