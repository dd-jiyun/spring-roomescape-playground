package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.RequestReservation;
import roomescape.exception.BadRequestException;
import roomescape.model.Reservation;
import roomescape.model.Time;
import roomescape.repository.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationList() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id);
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

    @Transactional
    public Reservation addReservation(RequestReservation requestReservation) {
        validateRequestReservation(requestReservation);

        Reservation reservation = convertToReservation(requestReservation);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long id) {

        if (reservationRepository.findById(id) == null) {
            throw new BadRequestException("존재하지 않는 아이디입니다.");
        }

        reservationRepository.delete(id);
    }

    private Reservation convertToReservation(RequestReservation request) {
        Time time = reservationRepository.findTimeById(request.time());
        return Reservation.of(request, time);
    }

}
