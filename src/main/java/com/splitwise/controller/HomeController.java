package com.splitwise.controller;

import com.splitwise.model.Trip;
import com.splitwise.service.TripService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Pantalla principal: crear viaje y listar viajes.
 */
@Controller
@RequestMapping("/")
public class HomeController {

    private final TripService tripService;

    public HomeController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public String home(Model model) {
        List<Trip> trips = tripService.findAllTrips();
        model.addAttribute("trips", trips);
        if (!model.containsAttribute("trip")) {
            model.addAttribute("trip", new Trip());
        }
        return "home";
    }

    @PostMapping("trips")
    public String createTrip(@Valid @ModelAttribute("trip") Trip trip, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.trip", result);
            redirectAttributes.addFlashAttribute("trip", trip);
            return "redirect:/";
        }
        String name = trip.getName() != null ? trip.getName().trim() : "";
        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre del viaje es obligatorio.");
            return "redirect:/";
        }
        Trip created = tripService.createTrip(name);
        redirectAttributes.addFlashAttribute("message", "Viaje creado correctamente.");
        return "redirect:/trips/" + created.getId();
    }
}
