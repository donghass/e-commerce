package kr.hhplus.be.server.application.point;
//controller  에서 application 로 받아오는 dto
public record ChargePointCommand(Long userId, Long amount) { }
