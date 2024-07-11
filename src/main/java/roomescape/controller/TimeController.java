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
import roomescape.dao.TimeDAO;
import roomescape.dto.RequestTime;
import roomescape.model.Time;

@Controller
public class TimeController {

    private final TimeDAO timeDAO;

    public TimeController(TimeDAO timeDAO) {
        this.timeDAO = timeDAO;
    }

    @GetMapping("/time")
    public String time() {
        return "time";
    }

    @GetMapping("/times")
    @ResponseBody
    public ResponseEntity<List<Time>> timeList() {
        return ResponseEntity.ok().body(timeDAO.findAllTimes());
    }

    @PostMapping("/times")
    public ResponseEntity<Time> addTime(@RequestBody RequestTime requestTime) {
        Time newTime = timeDAO.insert(requestTime);

        return ResponseEntity.created(URI.create("/times/" + newTime.getId())).body(newTime);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> cancelTime(@PathVariable Long id) {
        timeDAO.delete(id);

        return ResponseEntity.noContent().build();
    }

}
