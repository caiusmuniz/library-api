package com.example.demo.api.resource;

import com.example.demo.api.dto.BookDTO;
import com.example.demo.api.dto.LoanDTO;
import com.example.demo.api.dto.LoanFilterDTO;
import com.example.demo.api.dto.ReturnedLoanDTO;
import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.Loan;
import com.example.demo.service.BookService;
import com.example.demo.service.LoanService;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @PatchMapping("{id}")
    public void returnLoan(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());

        service.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
//        Loan filter = mapper.map(dto, Loan.class);

        Page<Loan> result = service.find(dto, pageRequest);
        List<LoanDTO> loans = result.getContent()
                .stream()
                .map(entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = mapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = mapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                })
                .collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }
}
