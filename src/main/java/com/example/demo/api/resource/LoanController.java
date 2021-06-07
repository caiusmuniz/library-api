package com.example.demo.api.resource;

import com.example.demo.api.dto.LoanDTO;
import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.Loan;
import com.example.demo.service.BookService;
import com.example.demo.service.LoanService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
@NoArgsConstructor
public class LoanController {
    @Autowired
    private LoanService service;

    @Autowired
    private BookService bookService;

    @Autowired
    private ModelMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createLoan(@RequestBody LoanDTO dto) {
        Book book = bookService.getBookByIsbn(dto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Livro não encontrado para o Isbn informado."));
        Loan entity = Loan.builder().book(book).customer("").loanDate(LocalDate.now()).build();
        entity = service.save(entity);
        return entity.getId();
    }
}
