package com.example.demo.api.resource;

import com.example.demo.api.dto.BookDTO;
import com.example.demo.api.exception.ApiErrors;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.entity.Book;
import com.example.demo.service.BookService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@NoArgsConstructor
public class BookController {
    @Autowired
    private BookService service;

    @Autowired
    private ModelMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        Book entity = mapper.map(dto, Book.class);
        entity = service.save(entity);
        return mapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id) {
        return service
                .getById(id)
                .map(book -> mapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
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
    public void delete(@PathVariable Long id) {
        Book book = service
                .getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    public BookDTO update(@PathVariable Long id, BookDTO dto) {
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
}
