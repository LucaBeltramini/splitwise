package com.splitwise.service;

import com.splitwise.model.Expense;
import com.splitwise.model.ExpenseShare;
import com.splitwise.model.Person;
import com.splitwise.model.Trip;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitwise.dto.PersonSummaryRow;
import com.splitwise.dto.TransferSuggestion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de gastos. Calcula ExpenseShare (división equitativa) y resumen de saldos.
 */
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PersonRepository personRepository;
    private final TripService tripService;

    public ExpenseService(ExpenseRepository expenseRepository, PersonRepository personRepository, TripService tripService) {
        this.expenseRepository = expenseRepository;
        this.personRepository = personRepository;
        this.tripService = tripService;
    }

    /**
     * Crea un gasto y sus ExpenseShare dividiendo el monto en partes iguales
     * entre las personas indicadas en participantIds.
     */
    @Transactional
    public Expense createExpense(Long tripId, String description, BigDecimal amount, Long paidById, List<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una persona para el gasto.");
        }
        Trip trip = tripService.getTripById(tripId);
        Person payer = personRepository.findById(paidById)
                .orElseThrow(() -> new IllegalArgumentException("Persona que pagó no encontrada."));
        if (!payer.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("La persona que pagó no pertenece a este viaje.");
        }

        List<Person> participants = new ArrayList<>();
        for (Long pid : participantIds) {
            Person p = personRepository.findById(pid)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + pid));
            if (!p.getTrip().getId().equals(tripId)) continue;
            participants.add(p);
        }
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Ninguna persona seleccionada pertenece al viaje.");
        }

        int count = participants.size();
        BigDecimal shareAmount = amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        Expense expense = new Expense();
        expense.setDescription(description.trim());
        expense.setAmount(amount);
        expense.setPaidBy(payer);
        expense.setTrip(trip);

        for (Person person : participants) {
            ExpenseShare share = new ExpenseShare(expense, person, shareAmount);
            expense.getShares().add(share);
            person.getExpenseShares().add(share);
        }

        expense = expenseRepository.save(expense);
        trip.getExpenses().add(expense);
        payer.getExpensesPaid().add(expense);
        return expense;
    }

    public List<Expense> getExpensesByTripId(Long tripId) {
        return expenseRepository.findByTripIdWithPayer(tripId);
    }

    /**
     * Calcula por persona: total pagado, total consumido (suma de sus ExpenseShare) y saldo.
     * Saldo = totalPagado - totalConsumido → positivo = le deben, negativo = debe.
     */
    @Transactional(readOnly = true)
    public List<com.splitwise.dto.PersonSummaryRow> getSummary(Long tripId) {
        List<Person> people = tripService.getPeopleByTripId(tripId);
        List<Expense> expenses = getExpensesByTripId(tripId);

        // total pagado por persona (suma de gastos donde paidBy = person)
        Set<Long> paidByPerson = expenses.stream()
                .map(e -> e.getPaidBy().getId())
                .collect(Collectors.toSet());

        return people.stream().map(person -> {
            BigDecimal totalPaid = expenses.stream()
                    .filter(e -> e.getPaidBy().getId().equals(person.getId()))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalConsumed = expenses.stream()
                    .flatMap(e -> e.getShares().stream())
                    .filter(s -> s.getPerson().getId().equals(person.getId()))
                    .map(ExpenseShare::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balance = totalPaid.subtract(totalConsumed);
            return new PersonSummaryRow(
                    person.getId(),
                    person.getName(),
                    totalPaid.setScale(2, RoundingMode.HALF_UP),
                    totalConsumed.setScale(2, RoundingMode.HALF_UP),
                    balance.setScale(2, RoundingMode.HALF_UP)
            );
        }).toList();
    }

    /**
     * Simplifica las deudas: calcula el mínimo de pagos para que todos queden a cero.
     * Cada fila es "fromName paga amount a toName".
     */
    public List<TransferSuggestion> computeTransfers(List<PersonSummaryRow> summary) {
        List<TransferSuggestion> result = new ArrayList<>();
        if (summary == null || summary.isEmpty()) return result;

        // Copias mutables: (nombre, saldo restante)
        List<PersonBalance> debtors = new ArrayList<>();
        List<PersonBalance> creditors = new ArrayList<>();
        for (PersonSummaryRow row : summary) {
            BigDecimal b = row.getBalance();
            if (b == null) continue;
            if (b.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new PersonBalance(row.getPersonName(), b.negate())); // lo que debe (positivo)
            } else if (b.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new PersonBalance(row.getPersonName(), b));
            }
        }
        debtors.sort(Comparator.comparing(PersonBalance::getBalance).reversed()); // el que más debe primero
        creditors.sort(Comparator.comparing(PersonBalance::getBalance).reversed()); // al que más le deben primero

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            PersonBalance debtor = debtors.get(i);
            PersonBalance creditor = creditors.get(j);
            if (debtor.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                i++;
                continue;
            }
            if (creditor.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                j++;
                continue;
            }
            BigDecimal amount = debtor.getBalance().min(creditor.getBalance()).setScale(2, RoundingMode.HALF_UP);
            result.add(new TransferSuggestion(debtor.getName(), creditor.getName(), amount));
            debtor.setBalance(debtor.getBalance().subtract(amount));
            creditor.setBalance(creditor.getBalance().subtract(amount));
        }
        return result;
    }

    /** Helper mutable para el algoritmo de simplificación. */
    private static class PersonBalance {
        private final String name;
        private BigDecimal balance;

        PersonBalance(String name, BigDecimal balance) {
            this.name = name;
            this.balance = balance;
        }
        String getName() { return name; }
        BigDecimal getBalance() { return balance; }
        void setBalance(BigDecimal balance) { this.balance = balance; }
    }
}
