package es.urjc.code.daw.library.controller.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.util.TestUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@DisplayName("REST Assured BookRestControllerTests")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookRestControllerTest {

  @LocalServerPort
  int port;

  String baseUrl;

  @BeforeEach
  public void setUp() {
    RestAssured.port = port;
    baseUrl = "https://localhost:" + port;
  }

  @Test
  @DisplayName("Given NO logged user when gets books then should return all books")
  public void givenNoLoggedUserWhenGetsAllBooksThenShouldReturnBooksList() {
    given()
        .relaxedHTTPSValidation()
    .when()
        .get(baseUrl + "/api/books/")
    .then()
        .statusCode(HttpStatus.OK.value())
        .body("size()", equalTo(5))
        .body("title", hasItems("SUEÑOS DE ACERO Y NEON", "LA LEGIÓN PERDIDA"))
        .body("description", hasItems(containsStringIgnoringCase("Los personajes que protagonizan"),
            containsStringIgnoringCase("Éufrates para conquistar Oriente")));
  }

  @Test
  @DisplayName("Given logged user as role: USER, when creates new book, then should return ok")
  public void givenLoggedUserWhenSaveNewBookThenShouldReturnOk() {
    // Creo el libro
    String title = "Book 1";
    String description = "book 1 description";
    Book book = new Book(title, description);

    Response responseCreate =
        given()
          .relaxedHTTPSValidation()
          .auth()
          .basic("user", "pass")
          .contentType(ContentType.JSON)
          .body(TestUtils.asJsonString(book))
        .when()
          .post(baseUrl + "/api/books/")
        .andReturn();

    Integer id = from(responseCreate.getBody().asString()).get("id");

    responseCreate
        .then()
          .statusCode(HttpStatus.CREATED.value())
          .body("id", notNullValue(),
              "title", equalTo(title),
              "description", containsStringIgnoringCase(description))
    .log().body();

    // Compruebo recuperar libro es correcto
    given()
        .relaxedHTTPSValidation()
      .when()
        .get(baseUrl + "/api/books/"+ id)
      .then()
        .body("id", notNullValue(),
            "title", equalTo(title),
            "description", containsStringIgnoringCase(description));

    // Borro el libro creado para no interferir en los demás test
    given()
        .relaxedHTTPSValidation()
        .auth()
        .basic("admin", "pass")
    .when()
        .delete(baseUrl + "/api/books/" + id)
    .then()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("Given logged user as role: ADMIN, when deletes book, then should return ok")
  public void givenLoggedUserAsAdminWhenDeletesBookThenShouldReturnOk() {

    // Creo un libro
    String title = "Book 1";
    String description = "book 1 description";
    Book book = new Book(title, description);

    Response responseCreate =
        given()
            .relaxedHTTPSValidation()
            .auth()
            .basic("user", "pass")
            .contentType(ContentType.JSON)
            .body(TestUtils.asJsonString(book))
        .when()
            .post(baseUrl + "/api/books/")
        .andReturn();

    Integer id = from(responseCreate.getBody().asString()).get("id");

    // Borro el libro
    given()
        .relaxedHTTPSValidation()
        .auth()
        .basic("admin", "pass")
    .when()
        .delete(baseUrl + "/api/books/" + (id))
    .then()
        .statusCode(HttpStatus.OK.value());

    // Compruebo recuperar libro NO escorrecto
    given()
        .relaxedHTTPSValidation()
    .when()
        .get(baseUrl + "/api/books/"+ id)
    .then()
        .statusCode(HttpStatus.NOT_FOUND.value());

  }

}