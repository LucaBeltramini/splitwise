package com.splitwise.repository;

import com.splitwise.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e JOIN FETCH e.paidBy WHERE e.trip.id = :tripId ORDER BY e.id DESC")
    List<Expense> findByTripIdWithPayer(Long tripId);

    void deleteByPaidById(Long paidById);
}
