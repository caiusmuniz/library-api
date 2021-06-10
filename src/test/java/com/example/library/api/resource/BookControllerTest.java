package com.example.library.api.resource;

import com.example.library.api.dto.BookDTO;
import com.example.library.exception.BusinessException;
import com.example.library.model.entity.Book;
import com.example.library.service.BookService;
import com.example.library.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {
    static String BOOK_API = "/api/books";
    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {
        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(1L).author("Arthur").title("As Aventuras").isbn("001").build();

        given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()))
        ;
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro.")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)))
        ;
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar livro com ISBN existente.")
    public void createBookWithDuplicatedIsbn() throws Exception {
        BookDTO dto = createNewBook();
        String msgErro = "Isbn já cadastrado.";

        given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(msgErro));

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(msgErro))
        ;
    }

    @Test
    @DisplayName("Deve obter informações de um livro.")
    public void getBookDetailsTest() throws Exception {
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .build();

        given(service.getById(id)).willReturn(Optional.of(book));

        // execução
        MockHttpServletRequestBuilder request = get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir.")
    public void bookNotFoundTest() throws Exception {
        given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {
        given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

        // execução
        MockHttpServletRequestBuilder request = delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
    public void deleteInexistentBookTest() throws Exception {
        given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução
        MockHttpServletRequestBuilder request = delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest() throws Exception {
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder().id(1L).author("some author").title("some title").isbn("321").build();
        given(service.getById(id)).willReturn(Optional.of(updatingBook));

        Book updatedBook = Book.builder().id(1L).author("Arthur").title("As Aventuras").isbn("321").build();
        given(service.update(updatingBook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = put(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("isbn").value("321"));
    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar o livro para atualizar")
    public void updateInexistentBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = put(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros.")
    public void findBooksTest() throws Exception {
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .build();

        given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 10), 1));

        String queryString = String.format("?author=%s&title=%s&page=0&size=10",
                book.getAuthor(), book.getTitle());

        MockHttpServletRequestBuilder request = get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    public static BookDTO createNewBook() {
        return BookDTO.builder().author("Arthur").title("As Aventuras").isbn("001").build();
    }
}
