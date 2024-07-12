package roomescape.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import roomescape.dao.ReservationDAO;
import roomescape.dto.RequestReservation;
import roomescape.model.Reservation;

@Controller
public class ReservationController {

    private final ReservationDAO reservationDAO;

    public ReservationController(ReservationDAO reservationDAO) {
        this.reservationDAO = reservationDAO;
    }

    @GetMapping("/reservation")
    public String reserve() {
        return "new-reservation";
    }

    @GetMapping("/reservations/{id}")
    @ResponseBody
    public ResponseEntity<Reservation> reserveOne(@PathVariable Long id) {
        return ResponseEntity.ok().body(reservationDAO.findReservationById(id));
    }

    @GetMapping("/reservations")
    @ResponseBody
    public ResponseEntity<List<Reservation>> reserveList() {
        return ResponseEntity.ok().body(reservationDAO.findAllReservations());
    }

    @PostMapping("/reservations")
    public ResponseEntity<Reservation> addReservation(@RequestBody RequestReservation requestReservation) {
        Reservation newReservation = reservationDAO.insert(requestReservation);

        return ResponseEntity.created(URI.create("/reservations/" + newReservation.getId())).body(newReservation);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationDAO.delete(id);

        return ResponseEntity.noContent().build();
    }

}
