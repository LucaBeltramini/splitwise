package com.splitwise.controller;

import com.splitwise.dto.AddExpenseForm;
import com.splitwise.dto.PersonSummaryRow;
import com.splitwise.dto.TransferSuggestion;
import com.splitwise.model.Expense;
import com.splitwise.model.Person;
import com.splitwise.model.Trip;
import com.splitwise.service.ExpenseService;
import com.splitwise.service.TripService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Vista de viaje: personas, gastos, formularios. Resumen de saldos.
 */
@Controller
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;
    private final ExpenseService expenseService;

    public TripController(TripService tripService, ExpenseService expenseService) {
        this.tripService = tripService;
        this.expenseService = expenseService;
    }

    @GetMapping("/{id}")
    public String tripView(@PathVariable Long id, @RequestParam(required = false) String tab, Model model, RedirectAttributes redirectAttributes) {
        Trip trip;
        try {
            trip = tripService.getTripById(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
        List<Person> people = tripService.getPeopleByTripId(id);
        List<Expense> expenses = expenseService.getExpensesByTripId(id);

        model.addAttribute("trip", trip);
        model.addAttribute("people", people);
        model.addAttribute("expenses", expenses);
        model.addAttribute("activeTab", "gastos".equalsIgnoreCase(tab) ? "gastos" : "personas");
        if (!model.containsAttribute("expenseForm")) {
            AddExpenseForm form = new AddExpenseForm();
            model.addAttribute("expenseForm", form);
        }
        return "trip";
    }

    @PostMapping("/{id}/people")
    public String addPerson(@PathVariable Long id, @RequestParam String name, RedirectAttributes redirectAttributes) {
        String trimmed = name != null ? name.trim() : "";
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de la persona es obligatorio.");
            return "redirect:/trips/" + id;
        }
        try {
            tripService.addPerson(id, trimmed);
            redirectAttributes.addFlashAttribute("message", "Persona agregada.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trips/" + id + "?tab=personas";
    }

    @PostMapping("/{id}/expenses")
    public String addExpense(@PathVariable Long id, @Valid @ModelAttribute("expenseForm") AddExpenseForm form,
                             BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.expenseForm", result);
            redirectAttributes.addFlashAttribute("expenseForm", form);
            return "redirect:/trips/" + id + "?tab=gastos";
        }
        if (form.getParticipantIds() == null || form.getParticipantIds().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos una persona para el gasto.");
            redirectAttributes.addFlashAttribute("expenseForm", form);
            return "redirect:/trips/" + id + "?tab=gastos";
        }
        try {
            expenseService.createExpense(id, form.getDescription(), form.getAmount(),
                    form.getPaidById(), form.getParticipantIds());
            redirectAttributes.addFlashAttribute("message", "Gasto registrado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("expenseForm", form);
            return "redirect:/trips/" + id + "?tab=gastos";
        }
        return "redirect:/trips/" + id + "?tab=gastos";
    }

    @GetMapping("/{id}/summary")
    public String summary(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Trip trip;
        try {
            trip = tripService.getTripById(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
        List<PersonSummaryRow> summary = expenseService.getSummary(id);
        List<TransferSuggestion> transfers = expenseService.computeTransfers(summary);
        model.addAttribute("trip", trip);
        model.addAttribute("summary", summary);
        model.addAttribute("transfers", transfers);
        return "summary";
    }

    @GetMapping("/{id}/edit")
    public String editTripForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Trip trip = tripService.getTripById(id);
            model.addAttribute("trip", trip);
            return "trip-edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateTrip(@PathVariable Long id, @RequestParam String name, RedirectAttributes redirectAttributes) {
        String trimmed = name != null ? name.trim() : "";
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre del viaje es obligatorio.");
            return "redirect:/trips/" + id + "/edit";
        }
        try {
            tripService.updateTrip(id, trimmed);
            redirectAttributes.addFlashAttribute("message", "Viaje actualizado.");
            return "redirect:/trips/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trips/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/confirm-delete")
    public String confirmDeleteTrip(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Trip trip = tripService.getTripById(id);
            model.addAttribute("trip", trip);
            return "trip-confirm-delete";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTrip(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tripService.deleteTrip(id);
            redirectAttributes.addFlashAttribute("message", "Viaje eliminado.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/{tripId}/people/{personId}/edit")
    public String editPersonForm(@PathVariable Long tripId, @PathVariable Long personId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Trip trip = tripService.getTripById(tripId);
            Person person = tripService.getPersonInTrip(tripId, personId);
            model.addAttribute("trip", trip);
            model.addAttribute("person", person);
            return "person-edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trips/" + tripId;
        }
    }

    @PostMapping("/{tripId}/people/{personId}/edit")
    public String updatePerson(@PathVariable Long tripId, @PathVariable Long personId, @RequestParam String name, RedirectAttributes redirectAttributes) {
        String trimmed = name != null ? name.trim() : "";
        if (trimmed.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio.");
            return "redirect:/trips/" + tripId + "/people/" + personId + "/edit";
        }
        try {
            tripService.updatePerson(tripId, personId, trimmed);
            redirectAttributes.addFlashAttribute("message", "Nombre actualizado.");
            return "redirect:/trips/" + tripId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trips/" + tripId;
        }
    }

    @GetMapping("/{tripId}/people/{personId}/confirm-delete")
    public String confirmDeletePerson(@PathVariable Long tripId, @PathVariable Long personId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Trip trip = tripService.getTripById(tripId);
            Person person = tripService.getPersonInTrip(tripId, personId);
            model.addAttribute("trip", trip);
            model.addAttribute("person", person);
            return "person-confirm-delete";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trips/" + tripId;
        }
    }

    @PostMapping("/{tripId}/people/{personId}/delete")
    public String deletePerson(@PathVariable Long tripId, @PathVariable Long personId, RedirectAttributes redirectAttributes) {
        try {
            tripService.deletePerson(tripId, personId);
            redirectAttributes.addFlashAttribute("message", "Persona eliminada.");
            return "redirect:/trips/" + tripId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/trips/" + tripId;
        }
    }
}
