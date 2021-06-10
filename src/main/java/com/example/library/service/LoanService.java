package com.example.library.service;

import com.example.library.api.dto.LoanFilterDTO;
import com.example.library.model.entity.Book;
import com.example.library.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filter, Pageable pageRequest);

    Page<Loan> getLoansByBook(Book book, Pageable pageRequest);

    List<Loan> getAllLateLoans();
}
