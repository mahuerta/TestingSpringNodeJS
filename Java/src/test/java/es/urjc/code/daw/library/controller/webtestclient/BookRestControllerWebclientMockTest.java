package es.urjc.code.daw.library.controller.webtestclient;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import es.urjc.code.daw.library.util.BookTestUtils;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
public class BookRestControllerWebclientMockTest extends BookTestUtils {

  private WebTestClient webTestClient;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookService bookService;

  @BeforeEach
  public void setup() {
    this.webTestClient = MockMvcWebTestClient
        .bindTo(mockMvc)
        .build();
  }

  @Test
  @DisplayName("Given NO logged user when gets books then should return all books")
  public void givenNoLoggedUserWhenGetsAllBooksThenShouldReturnBooksList() {

    List<Book> books = Arrays.asList(new Book(EXAMPLE_TITLE, EXAMPLE_DESCRIPTION),
        new Book(EXAMPLE_TITLE + BOOK_SEQUEL, EXAMPLE_DESCRIPTION + BOOK_SEQUEL));
    Mockito.when(bookService.findAll()).thenReturn(books);

    this.webTestClient
        .get()
        .uri("/api/books/")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].title")
        .value(Matchers.equalTo(EXAMPLE_TITLE))
        .jsonPath("$[0].description")
        .value(Matchers.equalTo(EXAMPLE_DESCRIPTION))
        .jsonPath("$.size()").isEqualTo(2);
  }

  @Test
  @DisplayName("Given logged user with role USER, when creates new book, then should return ok")
  public void givenLoggedUserWhenSaveNewBookThenShouldReturnOk() {
    Book book = new Book(EXAMPLE_TITLE, EXAMPLE_DESCRIPTION);
    Mockito.when(bookService.save(Mockito.any(Book.class))).thenReturn(book);

    // Create book
    this.webTestClient
        .mutate().filter(basicAuthentication("user", "pass")).build()
        .post()
        .uri("/api/books/")
        .body(Mono.just(book), Book.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.title")
        .value(Matchers.equalTo(EXAMPLE_TITLE))
        .jsonPath("$.description")
        .value(Matchers.equalTo(EXAMPLE_DESCRIPTION));

  }

  @Test
  @DisplayName("Given logged user as role: ADMIN, when deletes book, then should return ok")
  public void givenLoggedUserAsAdminWhenDeletesBookThenShouldReturnOk() {
    Long exampleId = 1L;
    Mockito.doNothing().when(bookService).delete(Mockito.anyLong());

    this.webTestClient
        .mutate().filter(basicAuthentication("admin", "pass")).build()
        .delete()
        .uri("/api/books/" + exampleId)
        .exchange()
        .expectStatus()
        .isOk();

  }

}
