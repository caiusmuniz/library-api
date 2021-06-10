package com.example.library.service.impl;

import com.example.library.api.dto.LoanFilterDTO;
import com.example.library.exception.BusinessException;
import com.example.library.model.entity.Book;
import com.example.library.model.entity.Loan;
import com.example.library.model.repository.LoanRepository;
import com.example.library.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {
    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) throws BusinessException {
        if (repository.existsByBookAndNotReturned(loan.getBook())) {
            throw new BusinessException("Livro já emprestado.");
        }
        return this.repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable pageRequest) {
        return repository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageRequest);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageRequest) {
        return repository.findByBook(book, pageRequest);
    }

    @Override
    public List<Loan> getAllLateLoans() {
        final Integer loanDays = 5;
        LocalDate daysAgo = LocalDate.now().minusDays(loanDays);
        return repository.findByLoanDateLessThanAndNotReturned(daysAgo);
    }
}
