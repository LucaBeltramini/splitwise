package com.splitwise.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el formulario de alta de gasto.
 * participantIds: IDs de las personas entre las que se divide el gasto.
 */
public class AddExpenseForm {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255)
    private String description;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "Debe indicar quién pagó")
    private Long paidById;

    /**
     * IDs de las personas que participan en el gasto (división equitativa).
     * Debe haber al menos uno (validado en servicio o controlador).
     */
    private List<Long> participantIds = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getPaidById() {
        return paidById;
    }

    public void setPaidById(Long paidById) {
        this.paidById = paidById;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds != null ? participantIds : new ArrayList<>();
    }
}
