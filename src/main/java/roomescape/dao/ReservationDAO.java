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
import roomescape.model.Reservation;

@Repository
public class ReservationDAO {
    private JdbcTemplate jdbcTemplate;
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

    public Reservation findReservationById(Long id) {
        String sql = "select * from reservation where id = ?";

        Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);

        return reservation;
    }

    public List<Reservation> findAllReservations() {
        String sql = "select * from reservation";

        List<Reservation> reservations = jdbcTemplate.query(sql, reservationRowMapper);

        return reservations;
    }

    public Reservation insert(RequestReservation requestReservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", requestReservation.name())
                .addValue("date", requestReservation.date())
                .addValue("time", requestReservation.time());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Reservation(id, requestReservation.name(), requestReservation.date(), requestReservation.time());
    }

    public int delete(Long id) {
        String sql = "delete from reservation where id = ?";

        return jdbcTemplate.update(sql, Long.valueOf(id));
    }

}
