package com.example.demo.api.resource;

import com.example.demo.api.dto.LoanDTO;
import com.example.demo.api.dto.LoanFilterDTO;
import com.example.demo.api.dto.ReturnedLoanDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.entity.Book;
import com.example.demo.model.entity.Loan;
import com.example.demo.service.BookService;
import com.example.demo.service.LoanService;
import com.example.demo.service.LoanServiceTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("fulano").email("fulano@email.com").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn(dto.getIsbn()).build();
        given(bookService.getBookByIsbn(dto.getIsbn()))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1L).customer("fulano").book(book).loanDate(LocalDate.now()).build();
        given(service.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = post(LOAN_API)
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

        given(bookService.getBookByIsbn(dto.getIsbn())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro não encontrado para o Isbn informado."));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de um livro emprestado.")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn(dto.getIsbn()).build();
        given(bookService.getBookByIsbn(dto.getIsbn()))
                .willReturn(Optional.of(book));

        given(service.save(Mockito.any(Loan.class))).willThrow(new BusinessException("Livro já emprestado."));

        MockHttpServletRequestBuilder request = post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Livro já emprestado."));
    }

    @Test
    @DisplayName("Deve retornar um livro.")
    public void returnBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Loan loan = Loan.builder().id(1L).build();
        given(service.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        MockHttpServletRequestBuilder request = patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk());

        verify(service, times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente.")
    public void returnInexistentBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar empréstimos.")
    public void findLoansTest() throws Exception {
        Long id = 1L;
        Loan loan = LoanServiceTest.createNewLoan();
        loan.setId(id);
        loan.getBook().setIsbn("123");

        given(service.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                loan.getBook().getIsbn(), loan.getCustomer());

        MockHttpServletRequestBuilder request = get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }
}
