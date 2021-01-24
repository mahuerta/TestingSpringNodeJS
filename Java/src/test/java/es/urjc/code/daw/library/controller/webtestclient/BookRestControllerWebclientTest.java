package es.urjc.code.daw.library.controller.webtestclient;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class BookRestControllerWebclientTest {

  @Autowired
  private WebTestClient webTestClient;

  @LocalServerPort
  private int port;

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
  public void REST_getAllBooksTest() throws InterruptedException {
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
  @DisplayName("Given logged user as role: USER, when creates new book, then should return ok")
  public void REST_saveNewBookTest() throws JsonProcessingException {
    // Creo el libro
    String title = "Book 1";
    String description = "book 1 description";
    Book book = new Book(title, description);

    byte[] result = this.webTestClient
        .mutate().filter(basicAuthentication("user", "pass")).build()
        .post()
        .uri("/api/books/")
        .body(Mono.just(book), Book.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody().jsonPath("id").isNotEmpty()
        .returnResult().getResponseBody();

    Book bookCreated = new ObjectMapper().readValue(new String(result), Book.class);

    // Compruebo recuperar libro es correcto
    this.webTestClient
        .mutate().filter(basicAuthentication("user", "pass")).build()
        .get()
        .uri("/api/books/" + bookCreated.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody().jsonPath("id").isNotEmpty()
        .jsonPath("title")
        .value(Matchers.containsStringIgnoringCase(title))
        .jsonPath("description")
        .value(Matchers.containsStringIgnoringCase(description));

    // Borro el libro creado para no interferir en los demás test
    this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .delete()
        .uri("/api/books/" + bookCreated.getId())
        .exchange()
        .expectStatus()
        .isOk();

  }

  @Test
  @DisplayName("Given logged user as role: ADMIN, when deletes book, then should return ok")
  public void REST_deleteBookTest()
      throws JsonProcessingException {
    // Creo un libro
    String title = "Book 1";
    String description = "book 1 description";
    Book book = new Book(title, description);

    byte[] result = this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .post()
        .uri("/api/books/")
        .body(Mono.just(book), Book.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody().jsonPath("id").isNotEmpty()
        .returnResult().getResponseBody();

    Book bookCreated = new ObjectMapper().readValue(new String(result), Book.class);

    // Borro el libro
    this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .delete()
        .uri("/api/books/" + bookCreated.getId())
        .exchange()
        .expectStatus()
        .isOk();

    // Compruebo recuperar libro NO es correcto
    this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .get()
        .uri("/api/books/" + bookCreated.getId())
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

}
