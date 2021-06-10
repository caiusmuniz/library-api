package com.example.library.mode.repository;

import com.example.library.model.entity.Book;
import com.example.library.model.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {
    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o ISBN informado.")
    public void returnTrueWhenIsbnExists() {
        // cenario
        String isbn = "123";

        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando existir um livro na base com o ISBN informado.")
    public void returnFalseWhenIsbnExists() {
        // cenario
        String isbn = "123";

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por Id.")
    public void findByIdTest() {
        // cenário
        Book book = createNewBook("123");
        entityManager.persist(book);

        // execução
        Optional<Book> foundBook = repository.findById(book.getId());

        // verificação
        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        Book book = createNewBook("123");

        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve excluir um livro.")
    public void deleteBookTest() {
        Book book = createNewBook("123");
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }

    public static Book createNewBook(String isbn) {
        return Book.builder().author("Author").title("Title").isbn(isbn).build();
    }
}
