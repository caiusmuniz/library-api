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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(Boolean.FALSE);

        when(repository.save(book)).thenReturn(
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
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(Boolean.TRUE);

        // execucao
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // verificacao
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por Id.")
    public void getByIdTest() {
        Long id = 1L;
        Book book = createNewBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id, quando ele não existe na base.")
    public void bookNotFoundByIdTest() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve excluir um livro.")
    public void deleteBookTest() {
        Book book = Book.builder().id(1L).build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar excluir um livro inexistente.")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest() {
        Long id = 1L;
        Book updatingBook = Book.builder().id(id).build();

        Book updateBook = createNewBook();
        updateBook.setId(id);

        when(repository.save(updatingBook)).thenReturn(updateBook);

        // execução
        Book book = service.update(updatingBook);

        // verificação
        assertThat(book.getId()).isEqualTo(updateBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updateBook.getAuthor());
        assertThat(book.getTitle()).isEqualTo(updateBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updateBook.getIsbn());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
    public void updateInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades.")
    public void findBookTest() {
        Book book = createNewBook();
        List<Book> lista = Arrays.asList(book);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Book> page = new PageImpl<Book>(lista, pageRequest, lista.size());

        when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro por Isbn.")
    public void getBookByIsbnTest() {
        String isbn = "123";
        when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, Mockito.times(1)).findByIsbn(isbn);
    }

    private Book createNewBook() {
        return Book.builder().author("Author").title("Title").isbn("123").build();
    }
}
