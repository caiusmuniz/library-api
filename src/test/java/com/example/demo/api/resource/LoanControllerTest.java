package com.example.demo.api.resource;

import com.example.demo.api.dto.LoanDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.Loan;
import com.example.demo.service.BookService;
import com.example.demo.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {
    static String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    LoanService service;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Deve realizar um empréstimo.")
    public void createLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn(dto.getIsbn()).build();
        BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn()))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1L).customer("fulano").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(service.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo com Isbn inexistente.")
    public void invalidIsbnCreateLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro não encontrado para o Isbn informado."));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de um livro emprestado.")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn(dto.getIsbn()).build();
        BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn()))
                .willReturn(Optional.of(book));

        BDDMockito.given(service.save(Mockito.any(Loan.class))).willThrow(new BusinessException("Livro já emprestado."));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro já emprestado."));
    }
}
