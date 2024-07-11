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
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dao.ReservationDAO;
import roomescape.dto.RequestReservation;
import roomescape.model.Reservation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private ReservationDAO reservationDAO;

    @Test
    @DisplayName("/로 요청 시 index.html과 200 statusCode를 반환한다.")
    void defaultUrlStatusTest() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("reservation URI 호출 시 reservation.html과 200 statusCode를 반환한다..")
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
    @DisplayName("예약 추가 시 201 statusCode를 반환하며 저장한다.")
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
    @DisplayName("예약을 추가하고 DAO를 통해 조회한 예약 수와 API를 통해 조회한 예약 수가 동일해야 한다.")
    void addReservationAndVerifyCountMatchesBetweenDatabaseAndApiTest() {
        reservationDAO.insert(new RequestReservation("브라운", "2023-08-05", "15:40"));

        List<Reservation> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", Reservation.class);

        int count = reservationDAO.count();

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    @DisplayName("예약을 추가하고 쿼리를 통해 예약 ID로 조회한 이름과 API를 통해 조회한 이름이 동일해야 한다.")
    void addReservationAndVerifyNameConsistencyBetweenDatabaseAndApiTest() {
        reservationDAO.insert(new RequestReservation("냠냠이", "2023-08-05", "15:40"));

        Reservation nameFromDB = reservationDAO.findReservationById(1L);

        String nameFromApi = RestAssured.given().log().all()
                .when().get("/reservations/1")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getString("name");

        assertThat(nameFromDB).isNotNull();
        assertThat(nameFromDB.getName()).isEqualTo(nameFromApi);
    }

    @Test
    @DisplayName("DAO를 통해 테이블에 예약을 추가한 후, 조회 쿼리를 통해 데이터가 저장되었는지 확인한다. 그 후 취소 API를 통해 테이블 예약 정보를 삭제하고 테이블에서 삭제되었는지 확인한다.")
    void reservationCreateAndDeleteFromDBTest() {
        reservationDAO.deleteAll();

        RequestReservation requestReservation = new RequestReservation("브라운", "2023-08-05", "10:00");
        reservationDAO.insert(requestReservation);

        int count = reservationDAO.count();
        assertThat(count).isEqualTo(1);

        reservationDAO.delete(1L);

        int countAfterDelete = reservationDAO.count();
        assertThat(countAfterDelete).isEqualTo(0);
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
    @DisplayName("예약 시간을 추가하면 201 상태코드가 반환된다.")
    void timeCreateAndDeleteTest() {
        Map<String, String> params = new HashMap<>();
        params.put("time", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/times/1");

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약 시간을 추가할 때 시간이 비어있으면 예외가 발생한다.")
    void NoRequiredTimeExceptionTest() {
        Map<String, String> params = new HashMap<>();
        params.put("time", "");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 시간이 형식에 맞지 않으면 예외가 발생한다")
    void NoMatchTimePatternExceptionTest() {
        Map<String, String> params = new HashMap<>();
        params.put("time", "12:--");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

}
