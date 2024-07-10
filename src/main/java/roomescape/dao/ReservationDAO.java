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

@Repository
public class ReservationDAO {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingColumns("name", "date", "time")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        Reservation reservations = new Reservation(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("date"),
                resultSet.getString("time")
        );
        return reservations;
    };

    public int count() {
        String sql = "SELECT COUNT(1) FROM reservation";

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Reservation findReservationById(Long id) {
        String sql = "SELECT * FROM reservation WHERE id = ?";

        Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);

        return reservation;
    }

    public List<Reservation> findAllReservations() {
        String sql = "SELECT * FROM reservation";

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
        if (requestReservation.time() == null || requestReservation.time().isEmpty()) {
            throw new BadRequestException("시간을 선택해주세요");
        }
    }

    public Reservation insert(RequestReservation requestReservation) {
        validateRequestReservation(requestReservation);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", requestReservation.name())
                .addValue("date", requestReservation.date())
                .addValue("time", requestReservation.time());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Reservation(id, requestReservation.name(), requestReservation.date(), requestReservation.time());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";

        if (findReservationById(id) == null) {
            throw new BadRequestException("예약 ID가 존재하지 않습니다.");
        }

        jdbcTemplate.update(sql, Long.valueOf(id));
    }

    public void deleteAll() {
        String sql = "DELETE FROM reservation";

        jdbcTemplate.update(sql);
    }

}
