package es.urjc.code.daw.library.controller.webtestclient;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.util.BookTestUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class BookRestControllerWebclientTest extends BookTestUtils {

  private WebTestClient webTestClient;

  @LocalServerPort
  private int port;

  // TODO: Utilizar una clase de configuración para inyectarla en los test que sean necesarios
  @BeforeEach
  public void setup() throws SSLException {
    SslContext sslContext = SslContextBuilder
        .forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();

    HttpClient httpClient = HttpClient.create()
        .secure(sslSpec -> sslSpec.sslContext(sslContext))
        .baseUrl("https://localhost:" + port);

    ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    this.webTestClient = WebTestClient
        .bindToServer(connector)
        .build();
  }

  @Test
  @DisplayName("Given NO logged user when gets books then should return all books")
  public void givenNoLoggedUserWhenGetsAllBooksThenShouldReturnBooksList() throws InterruptedException {
    this.webTestClient
        .get()
        .uri("/api/books/")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].title")
        .value(Matchers.containsStringIgnoringCase("SUEÑOS DE ACERO Y NEON"))
        .jsonPath("$[0].description")
        .value(Matchers.containsStringIgnoringCase("Los personajes que protagonizan"))
        .jsonPath("$.size()").isEqualTo(5);
  }

  @Test
  @DisplayName("Given logged user with role USER, when creates new book, then should return ok")
  public void givenLoggedUserWhenSaveNewBookThenShouldReturnOk() throws JsonProcessingException {
    // Create book
    WebTestClient.ResponseSpec responseCreate = this.createBook(EXAMPLE_TITLE, EXAMPLE_DESCRIPTION);

    // Otra forma de obtener el libro a partir del contenido de la petición, mucho más sencilla.
    Mono<Book> book = responseCreate.expectStatus().isCreated().returnResult(Book.class).getResponseBody().single();

    // Header validation
    responseCreate
        .expectStatus()
        .isCreated();

    // Body validation
    BodyContentSpec responseBody = responseCreate.expectBody();
    responseBody.jsonPath("id").isNotEmpty();

    Book bookCreated = new ObjectMapper().readValue(new String(responseBody.returnResult().getResponseBody()), Book.class);

    // Get book OK
    this.getBook(bookCreated.getId())
        .expectStatus()
        .isOk()
        .expectBody().jsonPath("id").isNotEmpty()
        .jsonPath("title")
        .value(Matchers.containsStringIgnoringCase(EXAMPLE_TITLE))
        .jsonPath("description")
        .value(Matchers.containsStringIgnoringCase(EXAMPLE_DESCRIPTION));

    // Delete book
    deleteBook(bookCreated.getId());
  }

  @Test
  @DisplayName("Given logged user as role: ADMIN, when deletes book, then should return ok")
  public void givenLoggedUserAsAdminWhenDeletesBookThenShouldReturnOk()
      throws JsonProcessingException {
    // Create book
    WebTestClient.ResponseSpec responseCreate = this.createBook(EXAMPLE_TITLE, EXAMPLE_DESCRIPTION);
    Book bookCreated = new ObjectMapper().readValue(new String(responseCreate.expectBody().returnResult().getResponseBody()), Book.class);

    // Delete book
    this.deleteBook(bookCreated.getId())
        .expectStatus()
        .isOk();

    // Get book NOT exists
    this.getBook(bookCreated.getId())
        .expectStatus()
        .is4xxClientError();
  }

  private WebTestClient.ResponseSpec deleteBook(Long id) {
    return this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .delete()
        .uri("/api/books/" + id)
        .exchange();
  }

  private WebTestClient.ResponseSpec getBook(Long id) {
    return this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .get()
        .uri("/api/books/" + id)
        .exchange();
  }

  private WebTestClient.ResponseSpec createBook(String title, String description){
    return this.webTestClient
        .mutate().filter(basicAuthentication("user", "pass")).build()
        .post()
        .uri("/api/books/")
        .body(Mono.just(new Book(EXAMPLE_TITLE, EXAMPLE_DESCRIPTION)), Book.class)
        .exchange();
  }

}
