package com.example.demo.service;

import com.example.demo.exception.BusinessException;
import com.example.demo.model.entity.Book;
import com.example.demo.model.repository.BookRepository;
import com.example.demo.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {
    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setup() {
        service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        // cenario
        Book book = createNewBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(Boolean.FALSE);

        Mockito.when(repository.save(book)).thenReturn(
                Book.builder()
                        .id(1L)
                        .author("Author")
                        .title("Title")
                        .isbn("123")
                        .build()
        );

        // execucao
        Book savedBook = service.save(book);

        // verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo("Author");
        assertThat(savedBook.getTitle()).isEqualTo("Title");
        assertThat(savedBook.getIsbn()).isEqualTo("123");
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar livro com ISBN existente.")
    public void shouldNotSaveABookWithDuplicatedIsbn() {
        // cenario
        Book book = createNewBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(Boolean.TRUE);

        // execucao
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // verificacao
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    private Book createNewBook() {
        return Book.builder().author("Author").title("Title").isbn("123").build();
    }
}
