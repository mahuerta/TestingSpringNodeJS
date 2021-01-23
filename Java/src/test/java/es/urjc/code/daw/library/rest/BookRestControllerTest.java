package es.urjc.code.daw.library.rest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
  @DisplayName("Given NO logged user, when gets /books, then should return all books")
  public void givenNoLoggedUserWhenGetsAllBooksThenShouldReturnBooksList() {
    given().
        relaxedHTTPSValidation().
        when().
        get(baseUrl + "/api/books/").
        then().
        statusCode(200).
        body("size()", greaterThanOrEqualTo(5)).
        body("get(0).id", equalTo(1)).
        body("get(0).title", equalTo("SUEÑOS DE ACERO Y NEON")).
        body("get(0).description", containsStringIgnoringCase("Los personajes que protagonizan")).
        body("get(4).id", equalTo(5)).
        body("get(4).title", equalTo("LA LEGIÓN PERDIDA")).
        body("get(4).description", containsStringIgnoringCase("Éufrates para conquistar Oriente"));
  }

  @Test
  @DisplayName("Given logged user as role: USER, when creates new book, then should return ok")
  public void givenLoggedUserWhenSaveNewBookThenShouldReturnOk() {
    int numberOfBooksBeforeOperation = numberOfBooks();

    given().
        relaxedHTTPSValidation().
        auth().
        basic("user", "pass").
        contentType("application/json").
        body("{\"description\":\"description 1\",\"title\":\"New Book 1\" }").
        when().
        post(baseUrl + "/api/books/").
        then().
        statusCode(201).
        body("title", equalTo("New Book 1"),
            "description", containsStringIgnoringCase("description 1"));

    int newNumberOfBooks = numberOfBooks();
    Assertions.assertEquals(numberOfBooksBeforeOperation + 1, newNumberOfBooks);
  }

  @Test
  @DisplayName("Given logged user as role: ADMIN, when deletes book, then should return ok")
  public void givenLoggedUserAsAdminWhenDeletesBookThenShouldReturnOk() {
    int numberOfBooksBeforeOperation = numberOfBooks();

    int idCreatedBook= createBookToDelete();

    given().
        relaxedHTTPSValidation().
        auth().
        basic("admin", "pass").
        when().
        delete(baseUrl + "/api/books/" + (idCreatedBook)).
        then().
        statusCode(200);

    int newNumberOfBooks = numberOfBooks();
    Assertions.assertEquals(numberOfBooksBeforeOperation, newNumberOfBooks);
  }

  private int numberOfBooks() {
    Response response = given().
        relaxedHTTPSValidation().
        when().
        get(baseUrl + "/api/books/");
    return response.getBody().jsonPath().getList("$").size();
  }

  private int createBookToDelete() {
    Response response = given().
        relaxedHTTPSValidation().
        auth().
        basic("user", "pass").
        contentType("application/json").
        body("{\"description\":\"description book to delete\",\"title\":\"Book to delete\" }").
        when().
        post(baseUrl + "/api/books/");
    return response.getBody().jsonPath().getInt("id");
  }
}