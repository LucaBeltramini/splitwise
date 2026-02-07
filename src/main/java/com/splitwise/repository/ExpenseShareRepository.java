package com.splitwise.repository;

import com.splitwise.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    List<ExpenseShare> findByPersonId(Long personId);

    List<ExpenseShare> findByExpenseId(Long expenseId);

    void deleteByPersonId(Long personId);
}
