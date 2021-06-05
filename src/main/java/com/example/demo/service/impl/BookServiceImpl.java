package com.example.demo.service.impl;

import com.example.demo.exception.BusinessException;
import com.example.demo.model.entity.Book;
import com.example.demo.model.repository.BookRepository;
import com.example.demo.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já cadastrado.");
        }

        return repository.save(book);
    }
}
