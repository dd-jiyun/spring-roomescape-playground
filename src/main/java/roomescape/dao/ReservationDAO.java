package roomescape.dao;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dto.RequestReservation;
import roomescape.exception.BadRequestException;
import roomescape.model.Reservation;
import roomescape.model.Time;

@Repository
public class ReservationDAO {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingColumns("name", "date", "time_id")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        Reservation reservations = new Reservation(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("date"),
                new Time(
                        resultSet.getLong("time_id"),
                        resultSet.getString("time_value")
                )
        );
        return reservations;
    };

    public int count() {
        String sql = "SELECT COUNT(1) FROM reservation";

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Reservation findReservationById(Long id) {
        String sql = """
                         SELECT r.id as reservation_id,
                                r.name,
                                r.date,
                                t.id as time_id,
                                t.time as time_value
                         FROM reservation as r INNER JOIN time as t on r.time_id = t.id
                         WHERE r.id = ?
                """;

        Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);

        return reservation;
    }

    public List<Reservation> findAllReservations() {
        String sql = """
                            SELECT r.id as reservation_id,
                                   r.name,
                                   r.date,
                                   t.id as time_id,
                                   t.time as time_value
                            FROM reservation as r INNER JOIN time as t on r.time_id = t.id
                """;

        List<Reservation> reservations = jdbcTemplate.query(sql, reservationRowMapper);

        return reservations;
    }

    private void validateRequestReservation(RequestReservation requestReservation) {
        if (requestReservation.name() == null || requestReservation.name().isEmpty()) {
            throw new BadRequestException("이름을 작성해주세요");
        }
        if (requestReservation.date() == null || requestReservation.date().isEmpty()) {
            throw new BadRequestException("날짜를 선택해주세요");
        }
        if (requestReservation.time() == null) {
            throw new BadRequestException("시간을 선택해주세요");
        }
    }

    public Reservation insert(RequestReservation requestReservation) {
        validateRequestReservation(requestReservation);

        String sql = "SELECT * FROM time WHERE id = ?";

        Time time = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
            Time time1 = new Time(
                    resultSet.getLong("id"),
                    resultSet.getString("time")
            );
            return time1;
        }, requestReservation.time());

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", requestReservation.name())
                .addValue("date", requestReservation.date())
                .addValue("time_id", requestReservation.time());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        Reservation reservation = new Reservation(id, requestReservation.name(), requestReservation.date(), time);

        return reservation;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";

        if (findReservationById(id) == null) {
            throw new BadRequestException("예약 ID가 존재하지 않습니다.");
        }

        jdbcTemplate.update(sql, id);
    }

    public void deleteAll() {
        String sql = "DELETE FROM reservation";

        jdbcTemplate.update(sql);
    }

}
