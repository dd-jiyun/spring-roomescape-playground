package roomescape.model;

import roomescape.dto.RequestReservation;
import roomescape.exception.BadRequestException;

public class Reservation {

    private Long id;
    private String name;
    private String date;
    private Time time;

    public Reservation(Long id, String name, String date, Time time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public static Reservation of(RequestReservation request, Time time) {
        Reservation reservation = new Reservation(null, request.name(), request.date(), time);
        reservation.validate();
        return reservation;
    }

    private void validate() {
        if (this.name == null || this.name.isEmpty()) {
            throw new BadRequestException("이름을 작성해주세요");
        }
        if (this.date == null || this.date.isEmpty()) {
            throw new BadRequestException("날짜를 선택해주세요");
        }
        if (this.time == null) {
            throw new BadRequestException("시간을 선택해주세요");
        }
    }

}
