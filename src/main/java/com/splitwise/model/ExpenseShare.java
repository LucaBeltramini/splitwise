package com.splitwise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Parte de un gasto que corresponde a una persona.
 * amount = gasto total / número de personas seleccionadas.
 */
@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /**
     * Monto que corresponde a esta persona (total / N participantes).
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public ExpenseShare() {
    }

    public ExpenseShare(Expense expense, Person person, BigDecimal amount) {
        this.expense = expense;
        this.person = person;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
