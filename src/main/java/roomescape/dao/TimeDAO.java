package roomescape.dao;

import java.util.List;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dto.RequestTime;
import roomescape.exception.BadRequestException;
import roomescape.model.Time;

@Repository
public class TimeDAO {

    private static final String TIME_PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9])$";
    private static final Pattern pattern = Pattern.compile(TIME_PATTERN);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public TimeDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("time")
                .usingColumns("time")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Time> timeRowMapper = (resultSet, rowNum) -> {
        Time time = new Time(
                resultSet.getLong("id"),
                resultSet.getString("time")
        );
        return time;
    };

    public Time findTimeById(Long id) {
        String sql = "SELECT * FROM time WHERE id = ? ";

        return jdbcTemplate.queryForObject(sql, timeRowMapper, id);
    }

    public List<Time> findAllTimes() {
        String sql = "SELECT * FROM time";

        List<Time> times = jdbcTemplate.query(sql, timeRowMapper);

        return times;
    }

    private boolean isTimePattern(RequestTime requestTime) {
        return pattern.matcher(requestTime.time()).matches();
    }

    private void validateRequestTime(RequestTime requestTime) {
        if (requestTime.time() == null || requestTime.time().isEmpty()) {
            throw new BadRequestException("시간을 입력해주세요");
        }
        if (!isTimePattern(requestTime)) {
            throw new BadRequestException("시간 형식에 맞게 입력해주세요.");
        }
    }

    public Time insert(RequestTime requestTime) {
        validateRequestTime(requestTime);

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("time", requestTime.time());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Time(id, requestTime.time());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM time WHERE id = ?";

        if (findTimeById(id) == null) {
            throw new BadRequestException("존재하지 않는 시간입니다.");
        }
        jdbcTemplate.update(sql, Long.valueOf(id));
    }

}
