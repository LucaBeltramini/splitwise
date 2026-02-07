package com.splitwise.dto;

import java.math.BigDecimal;

/**
 * Sugerencia de pago para simplificar deudas: "fromName" debe pagar "amount" a "toName".
 */
public class TransferSuggestion {

    private final String fromName;
    private final String toName;
    private final BigDecimal amount;

    public TransferSuggestion(String fromName, String toName, BigDecimal amount) {
        this.fromName = fromName;
        this.toName = toName;
        this.amount = amount;
    }

    public String getFromName() {
        return fromName;
    }

    public String getToName() {
        return toName;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
