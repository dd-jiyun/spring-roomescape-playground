package roomescape.service;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.RequestTime;
import roomescape.exception.BadRequestException;
import roomescape.model.Time;
import roomescape.repository.TimeRepository;

@Service
public class TimeService {
    private static final String TIME_PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9])$";
    private static final Pattern pattern = Pattern.compile(TIME_PATTERN);

    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
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

    @Transactional(readOnly = true)
    public List<Time> getTimeList() {
        return timeRepository.findAll();
    }

    @Transactional
    public Time addTime(RequestTime requestTime) {
        validateRequestTime(requestTime);

        Time time = convertTime(requestTime);

        return timeRepository.save(time);
    }

    @Transactional
    public void cancelTime(Long id) {
        if (timeRepository.findById(id) == null) {
            throw new BadRequestException("존재하지 않는 아이디입니다.");
        }

        timeRepository.delete(id);
    }

    private Time convertTime(RequestTime requestTime) {
        return Time.of(requestTime);
    }

}
