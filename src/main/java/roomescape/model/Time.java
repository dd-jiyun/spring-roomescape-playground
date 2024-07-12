package roomescape.model;

import roomescape.dto.RequestTime;

public class Time {

    private Long id;
    private String time;

    public Time(Long id, String time) {
        this.id = id;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public static Time of(RequestTime requestTime) {
        return new Time(null, requestTime.time());
    }

}
