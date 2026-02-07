package com.splitwise.repository;

import com.splitwise.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByTripIdOrderByName(Long tripId);
}
