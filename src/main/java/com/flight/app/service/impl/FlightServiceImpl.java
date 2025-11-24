package com.flight.app.service.impl;

import com.flight.app.entity.Flight;
import com.flight.app.repository.FlightRepository;
import com.flight.app.service.FlightService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Autowired
    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    private Mono<Flight> validateFlight(Flight flight) {
        // fromPlace and toPlace cannot be the same
        if(flight.getFromPlace().equalsIgnoreCase(flight.getToPlace())) {
            return Mono.error(new IllegalArgumentException("Departure and arrival places cannot be the same."));
        }

        // Arrival time must be after Departure time
        LocalTime departure = flight.getDepartureTime();
        LocalTime arrival = flight.getArrivalTime();

        if(!arrival.isAfter(departure)) {
            return Mono.error(new IllegalArgumentException("Arrival time must be after the departure time."));
        }
        
        return Mono.just(flight);
    }

    @Override
    public Mono<String> addFlight(Flight flight) {
        return validateFlight(flight)
            .map(f -> {
                f.setAvailableSeats(f.getTotalSeats());
                return f;
            })
            .flatMap(flightRepository::save)
            .map(savedFlight -> savedFlight.getId());
    }
    
    @Override
    public Flux<Flight> searchFlights(String fromPlace, String toPlace, LocalDate scheduleDate) {
        return flightRepository.findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                fromPlace,
                toPlace,
                scheduleDate,
                0 // flights with 1 or more available seats
        );
    }
    
    @Override
    public Mono<Flight> getFlightById(String flightId) {
        return flightRepository.findById(flightId);
    }

}
