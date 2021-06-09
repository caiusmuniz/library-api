package com.example.demo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private Long id;

    @NotEmpty
    @Size(min = 1, max = 5, message = "ISBN must be between 1 and 5 characters")
    private String isbn;

    @NotEmpty(message = "Customer cannot be empty")
    private String customer;

    @NotEmpty
    @Email(message = "Email should be valid")
    private String email;

    private BookDTO book;
}
