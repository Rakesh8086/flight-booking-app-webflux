package com.flight.app.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flight.app.entity.Flight; 

import reactor.core.publisher.Flux; 
import java.time.LocalDate;

@Repository
public interface FlightRepository extends ReactiveMongoRepository<Flight, String> { 

    Flux<Flight> findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
            String fromPlace,
            String toPlace,
            LocalDate scheduleDate,
            int availableSeats
    );
}