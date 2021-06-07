package com.example.demo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Loan {
    @Id
    private Long id;

    @Column
    private String customer;

    @Column
    private Book book;

    @Column
    private LocalDate loanDate;

    @Column
    private Boolean returned;
}
