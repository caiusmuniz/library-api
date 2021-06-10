package com.example.library.api.resource;

import com.example.library.api.dto.BookDTO;
import com.example.library.api.dto.LoanDTO;
import com.example.library.model.entity.Book;
import com.example.library.model.entity.Loan;
import com.example.library.service.BookService;
import com.example.library.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {
    private final BookService service;
    private final LoanService loanService;
    private final ModelMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a book")
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info("Creating a book for a isbn: {}", dto.getIsbn());
        Book entity = mapper.map(dto, Book.class);
        entity = service.save(entity);
        return mapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obtains a book details by id")
    public BookDTO get(@PathVariable Long id) {
        log.info("Obtaining details for a book id: {}", id);
        return service
                .getById(id)
                .map(book -> mapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation("Find books by params")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        log.info("Find books by params");
        Book filter = mapper.map(dto, Book.class);

        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> lista = result.getContent()
                .stream()
                .map(entity -> mapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(lista, pageRequest, result.getTotalElements());
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete a book by id")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book succesfully deleted")
    })
    public void delete(@PathVariable Long id) {
        log.info("Deleting book of id: {}", id);
        Book book = service
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Update a book")
    public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO dto) {
        log.info("Updating book of id: {}", id);
        return service
                .getById(id)
                .map(book -> {
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());
                    book = service.update(book);
                    return mapper.map(book, BookDTO.class);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageRequest) {
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageRequest);
        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = mapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = mapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                })
                .collect(Collectors.toList());
        return new PageImpl<LoanDTO>(list, pageRequest, result.getTotalElements());
    }
}
