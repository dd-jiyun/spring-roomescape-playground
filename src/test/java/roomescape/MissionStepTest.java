package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dao.ReservationDAO;
import roomescape.model.Reservation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationDAO reservationDAO;

    @Test
    @DisplayName("/로 요청 시 index.html과 200 statusCode 반환하는지 테스트합니다.")
    void defaultUrlStatusTest() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("reservation URI 호출 시 reservation.html과 200 statusCode 반환한다.")
    void reservationPageStatusTest() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 목록 조회 시 데이터를 반환한다.")
    void reservationJsonDataTest() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("예약 추가 시 201 statusCode를 반환하며 저장된다.")
    void createReservationTest() {
        Map<String, String> params = new HashMap<>();

        params.put("name", "브라운");
        params.put("date", "2023-08-05");
        params.put("time", "15:40");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/1")
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("필요한 데이터를 입력하지 않았을 때 400 statusCode를 반환한다.")
    void NoRequiredReservationDataExceptionTest() {
        Map<String, String> params = new HashMap<>();

        params.put("name", "브라운");
        params.put("date", "");
        params.put("time", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("삭제할 예약 id가 없는 경우 400 statusCode가 반환되며 예외가 발생한다.")
    void withoutReservationIdToDeleteExceptionTest() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약을 추가하고 쿼리를 통해 조회한 예약 수와 API를 통해 조회한 예약 수가 동일해야 한다.")
    void addReservationAndVerifyCountMatchesBetweenDatabaseAndAPI() {
        jdbcTemplate.update("INSERT INTO reservation (name, date, time) VALUES (?, ?, ?)", "브라운", "2023-08-05", "15:40");

        List<Reservation> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", Reservation.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    @DisplayName("예약을 추가하고 쿼리를 통해 예약 ID로 조회한 이름과 API를 통해 조회한 이름이 동일한지 테스트합니다.")
    void addReservationAndVerifyNameConsistencyBetweenDatabaseAndApiTest() {
        jdbcTemplate.update("INSERT INTO reservation (name, date, time) VALUES (?, ?, ?)", "냠냠이", "2023-08-05", "15:40");

        Reservation nameFormDB = reservationDAO.findReservationById(1L);

        String nameFromApi = RestAssured.given().log().all()
                .when().get("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getString("name");

        assertThat(nameFormDB).isNotNull();
        assertThat(nameFormDB.getName()).isEqualTo(nameFromApi);
    }

}
