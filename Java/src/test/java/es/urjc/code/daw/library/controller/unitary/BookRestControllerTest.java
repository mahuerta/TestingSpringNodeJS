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

  @Autowired
  private MockMvc mvc;

  @MockBean
  private BookService bookService;

  @Nested
  @DisplayName("Given No Logged user")
  class givenNoLoggedUser {

    @Nested
    @DisplayName("when the user gets all books ")
    class whenGetAllBooks {

      @Test
      @DisplayName("then the user should get all books")
      public void thenShouldGetAllBooks() throws Exception {
        String title = "Book 1";
        String description = "book 1 description";
        List<Book> books = Arrays.asList(new Book(title, description), new Book());
        Mockito.when(bookService.findAll()).thenReturn(books);

        mvc.perform(get("/api/books/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", equalTo(title)))
            .andExpect(jsonPath("$[0].description", equalTo(description)));
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
    class whenGetAllBooks {

      @Test
      @DisplayName("then should return book created")
      @WithMockUser(username = "username", roles = "USER")
      public void thenShouldReturnBook() throws Exception {
        String title = "Book 1";
        String description = "book 1 description";
        Book book = new Book(title, description);
        Mockito.when(bookService.save(Mockito.any())).thenReturn(book);

        mvc.perform(post("/api/books/")
            .content(TestUtils.asJsonString(book))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", equalTo(title)))
            .andExpect(jsonPath("$.description", equalTo(description)));
      }
    }
  }

  @Nested
  @DisplayName("Given user admin logged")
  class givenLoggedAsAdminUser {

    @Nested
    @DisplayName("when the user deletes a book")
    class whenGetAllBooks {

      @Test
      @DisplayName("then should return ok")
      @WithMockUser(username = "username", roles = "ADMIN")
      public void thenShouldReturnBook() throws Exception {
        long id = 1;
        Mockito.doNothing().when(bookService).delete(id);

        mvc.perform(delete("/api/books/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
      }

      @Test
      @DisplayName("then should return throw exception")
      @WithMockUser(username = "username", roles = "ADMIN")
      public void thenShouldThrowException() throws Exception {
        long id = 2;
        Mockito.doThrow(EmptyResultDataAccessException.class).when(bookService).delete(id);

        mvc.perform(delete("/api/books/2")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
      }
    }

  }

}