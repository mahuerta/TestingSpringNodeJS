package es.urjc.code.daw.library.controller.unitary;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import es.urjc.code.daw.library.util.TestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("MockMVC BookRestControllerTests")
class BookRestControllerTest {

  private final String exampleTitle = "Book";
  private final String exampleDescription = "Book description";
  private final String bookSequel = " 2";

  @Autowired
  private MockMvc mvc;

  @MockBean
  private BookService bookService;

  @Nested
  @DisplayName("Given NO logged user")
  class givenNoLoggedUser {

    @Nested
    @DisplayName("when the user gets all books ")
    class whenGetAllBooks {

      @Test
      @DisplayName("then the user should get all books")
      public void thenShouldGetAllBooks() throws Exception {
        List<Book> books = Arrays.asList(new Book(exampleTitle, exampleDescription),
                                         new Book(exampleTitle + bookSequel, exampleDescription + bookSequel));
        Mockito.when(bookService.findAll()).thenReturn(books);

        mvc.perform(get("/api/books/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", equalTo(exampleTitle)))
            .andExpect(jsonPath("$[0].description", equalTo(exampleDescription)));
      }

      @Test
      @DisplayName("then should return no books")
      public void thenShouldGetNoBooks() throws Exception {
        Mockito.when(bookService.findAll()).thenReturn(new ArrayList<>());

        mvc.perform(get("/api/books/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
      }
    }
  }

  @Nested
  @DisplayName("Given user logged")
  class givenLoggedUser {

    @Nested
    @DisplayName("when the user creates new book")
    class whenCreateNewBook {

      @Test
      @DisplayName("then should return book created")
      @WithMockUser(username = "username", roles = "USER")
      public void thenShouldReturnBook() throws Exception {
        Book book = new Book(exampleTitle, exampleDescription);
        Mockito.when(bookService.save(Mockito.any())).thenReturn(book);

        mvc.perform(post("/api/books/")
            .content(TestUtils.asJsonString(book))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", equalTo(exampleTitle)))
            .andExpect(jsonPath("$.description", equalTo(exampleDescription)));
      }
    }
  }

  @Nested
  @DisplayName("Given user admin logged")
  class givenLoggedAsAdminUser {

    @Nested
    @DisplayName("when the user deletes a book")
    class whenDeleteBook {
      private final Integer exampleId = 1;

      @Test
      @DisplayName("then should return ok")
      @WithMockUser(username = "username", roles = "ADMIN")
      public void thenShouldReturnBook() throws Exception {
        Mockito.doNothing().when(bookService).delete(exampleId);

        mvc.perform(delete("/api/books/"+exampleId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
      }

      @Test
      @DisplayName("then should return throw exception")
      @WithMockUser(username = "username", roles = "ADMIN")
      public void thenShouldThrowException() throws Exception {
        Mockito.doThrow(EmptyResultDataAccessException.class).when(bookService).delete(exampleId);

        mvc.perform(delete("/api/books/"+exampleId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
      }
    }
  }
}