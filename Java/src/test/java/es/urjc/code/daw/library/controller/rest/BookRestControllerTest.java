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

  private final String exampleTitle = "Book 1";
  private final String exampleDescription = "Book 1 description";
  private String baseUrl;

  @LocalServerPort
  private int port;

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
  @DisplayName("Given logged user with role USER, when creates new book, then should return ok")
  public void givenLoggedUserWhenSaveNewBookThenShouldReturnOk() {
    // Create Book
    Response responseCreate = createBook(exampleTitle, exampleDescription);

    // Validations
    responseCreate
        .then()
          .statusCode(HttpStatus.CREATED.value())
          .body("id", notNullValue(),
              "title", equalTo(exampleTitle),
              "description", containsStringIgnoringCase(exampleDescription));

    Integer id = from(responseCreate.getBody().asString()).get("id");

    // Get book is OK
    getBook(id)
        .then()
          .statusCode(HttpStatus.OK.value())
          .body("id", notNullValue(),
              "title", equalTo(exampleTitle),
              "description", containsStringIgnoringCase(exampleDescription));

    // Delete book
    deleteBook(id);
  }

  @Test
  @DisplayName("Given logged user as role: ADMIN, when deletes book, then should return ok")
  public void givenLoggedUserAsAdminWhenDeletesBookThenShouldReturnOk() {
    // Create Book
    Response responseCreate = createBook(exampleTitle, exampleDescription);
    Integer id = from(responseCreate.getBody().asString()).get("id");

    // Delete book
    Response responseDelete = deleteBook(id);
    responseDelete
        .then()
          .statusCode(HttpStatus.OK.value());

    // Validations
    getBook(id)
        .then()
          .statusCode(HttpStatus.NOT_FOUND.value());

  }

  private Response createBook(String title, String description) {
    return given()
        .relaxedHTTPSValidation()
        .auth()
        .basic("user", "pass")
        .contentType(ContentType.JSON)
        .body(TestUtils.asJsonString(new Book(title, description)))
        .when()
        .post(baseUrl + "/api/books/")
        .andReturn();
  }

  private Response getBook(Integer id) {
    return given()
        .relaxedHTTPSValidation()
        .when()
        .get(baseUrl + "/api/books/"+ id)
        .andReturn();
  }

  private Response deleteBook(Integer id){
    return given()
        .relaxedHTTPSValidation()
        .auth()
        .basic("admin", "pass")
        .when()
        .delete(baseUrl + "/api/books/" + (id))
        .andReturn();
  }

}