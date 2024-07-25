package roomescape.model;

import java.util.regex.Pattern;
import roomescape.dto.RequestTime;
import roomescape.exception.BadRequestException;

public class Time {
    private static final String TIME_PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9])$";
    private static final Pattern pattern = Pattern.compile(TIME_PATTERN);

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
        Time time = new Time(null, requestTime.time());
        time.validate();
        return time;
    }

    private void validate() {
        if (this.time == null || this.time.isEmpty()) {
            throw new BadRequestException("시간을 입력해주세요");
        }
        if (!pattern.matcher(this.time).matches()) {
            throw new BadRequestException("시간 형식에 맞게 입력해주세요.");
        }
    }
}
