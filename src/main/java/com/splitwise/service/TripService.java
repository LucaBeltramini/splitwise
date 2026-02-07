package com.splitwise.service;

import com.splitwise.model.Person;
import com.splitwise.model.Trip;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.ExpenseShareRepository;
import com.splitwise.repository.PersonRepository;
import com.splitwise.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de viajes y personas.
 */
@Service
public class TripService {

    private final TripRepository tripRepository;
    private final PersonRepository personRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;

    public TripService(TripRepository tripRepository, PersonRepository personRepository,
                       ExpenseRepository expenseRepository, ExpenseShareRepository expenseShareRepository) {
        this.tripRepository = tripRepository;
        this.personRepository = personRepository;
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
    }

    @Transactional
    public Trip createTrip(String name) {
        Trip trip = new Trip(name);
        return tripRepository.save(trip);
    }

    public List<Trip> findAllTrips() {
        return tripRepository.findAllOrderByIdDesc();
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + id));
    }

    @Transactional
    public Person addPerson(Long tripId, String name) {
        Trip trip = getTripById(tripId);
        Person person = new Person(name.trim(), trip);
        person = personRepository.save(person);
        trip.getPeople().add(person);
        return person;
    }

    public List<Person> getPeopleByTripId(Long tripId) {
        return personRepository.findByTripIdOrderByName(tripId);
    }

    @Transactional
    public Trip updateTrip(Long id, String name) {
        Trip trip = getTripById(id);
        trip.setName(name != null ? name.trim() : "");
        return tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = getTripById(id);
        tripRepository.delete(trip);
    }

    public Person getPersonInTrip(Long tripId, Long personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada."));
        if (!person.getTrip().getId().equals(tripId)) {
            throw new IllegalArgumentException("La persona no pertenece a este viaje.");
        }
        return person;
    }

    @Transactional
    public Person updatePerson(Long tripId, Long personId, String name) {
        Person person = getPersonInTrip(tripId, personId);
        person.setName(name != null ? name.trim() : "");
        return personRepository.save(person);
    }

    /** Elimina la persona y sus gastos como pagador; quita su participación en otros gastos. */
    @Transactional
    public void deletePerson(Long tripId, Long personId) {
        Person person = getPersonInTrip(tripId, personId);
        expenseShareRepository.deleteByPersonId(personId);
        expenseRepository.deleteByPaidById(personId);
        personRepository.delete(person);
    }
}
