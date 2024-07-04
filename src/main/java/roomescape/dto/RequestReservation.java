package roomescape.dto;

public record RequestReservation(String name, String date, String time) {

    @Override
    public String name() {
        return name;
    }

    @Override
    public String date() {
        return date;
    }

    @Override
    public String time() {
        return time;
    }

}