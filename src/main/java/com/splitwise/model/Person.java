package com.splitwise.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persona que participa en un viaje. Puede pagar gastos y ser beneficiaria.
 */
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShare> expenseShares = new ArrayList<>();

    @OneToMany(mappedBy = "paidBy", cascade = CascadeType.ALL)
    private List<Expense> expensesPaid = new ArrayList<>();

    public Person() {
    }

    public Person(String name, Trip trip) {
        this.name = name;
        this.trip = trip;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public List<ExpenseShare> getExpenseShares() {
        return expenseShares;
    }

    public void setExpenseShares(List<ExpenseShare> expenseShares) {
        this.expenseShares = expenseShares;
    }

    public List<Expense> getExpensesPaid() {
        return expensesPaid;
    }

    public void setExpensesPaid(List<Expense> expensesPaid) {
        this.expensesPaid = expensesPaid;
    }
}
