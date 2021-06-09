package com.example.demo.mode.repository;

import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.Loan;
import com.example.demo.model.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static com.example.demo.mode.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {
    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
    public void existsByBookAndNotReturned() {
        Loan loan = createAndPersistLoan(LocalDate.now());
        Book book = loan.getBook();

        Boolean exists = repository.existsByBookAndNotReturned(book);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer.")
    public void findByBookIsbnOrCustomer() {
        Loan loan = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = repository.findByBookIsbnOrCustomer(
                loan.getBook().getIsbn(),
                loan.getCustomer(),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a 5 dias atrás e não retornados")
    public void findByLoanDateLessThanAndNotReturned() {
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Não deve obter empréstimos cuja data empréstimo for menor ou igual a 5 dias atrás e não retornados")
    public void notFindByLoanDateLessThanAndNotReturned() {
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).isEmpty();
    }

    private Loan createAndPersistLoan(LocalDate localDate) {
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("fulano").loanDate(localDate).build();
        return entityManager.persist(loan);
    }
}
