package com.example.library.service;

import com.example.library.api.dto.LoanFilterDTO;
import com.example.library.exception.BusinessException;
import com.example.library.model.entity.Book;
import com.example.library.model.entity.Loan;
import com.example.library.model.repository.LoanRepository;
import com.example.library.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {
    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setup() {
        service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo.")
    public void saveLoanTest() {
        Loan savingLoan = createNewLoan();

        Loan savedLoan = createNewLoan();
        savedLoan.setId(1L);
        savedLoan.setLoanDate(savingLoan.getLoanDate());

        when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(false);
        when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado.")
    public void loanedBookErrorOnCreateLoanTest() {
        Loan savingLoan = createNewLoan();

        when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Livro já emprestado.");

        verify(repository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informações de empréstimo por Id.")
    public void getLoanDetailsTest() {
        Long id = 1L;
        Loan loan = createNewLoan();
        loan.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um emprestimo.")
    public void updateLoanTest() {
        Loan loan = createNewLoan();
        loan.setId(1L);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();

        verify(repository).save(loan);
    }

    public static Loan createNewLoan() {
        Book book = Book.builder().id(1L).build();
        String customer = "fulano";

        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades.")
    public void findLoanTest() {
        LoanFilterDTO filter = LoanFilterDTO.builder().isbn("123").customer("fulano").build();

        Loan loan = createNewLoan();
        loan.setId(1L);
        List<Loan> lista = Arrays.asList(loan);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());

        when(repository.findByBookIsbnOrCustomer(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class))
        )
                .thenReturn(page);

        Page<Loan> result = service.find(filter, pageRequest);

        Assertions.assertThat(result.getContent()).isEqualTo(lista);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
