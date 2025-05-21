package kr.hhplus.be.server.domain.point;

public class PointEvent {
    public record PointUsedCompleted(PointEventInfo pointEventInfo) {}
}