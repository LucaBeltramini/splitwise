package com.splitwise.dto;

import java.math.BigDecimal;

/**
 * Fila del resumen: por persona, total pagado, total consumido y saldo.
 * Saldo positivo = le deben; negativo = debe.
 */
public class PersonSummaryRow {

    private Long personId;
    private String personName;
    private BigDecimal totalPaid;
    private BigDecimal totalConsumed;
    private BigDecimal balance;

    public PersonSummaryRow(Long personId, String personName, BigDecimal totalPaid, BigDecimal totalConsumed, BigDecimal balance) {
        this.personId = personId;
        this.personName = personName;
        this.totalPaid = totalPaid;
        this.totalConsumed = totalConsumed;
        this.balance = balance;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getPersonName() {
        return personName;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getTotalConsumed() {
        return totalConsumed;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
