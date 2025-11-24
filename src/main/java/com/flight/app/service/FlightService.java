package com.flight.app.service;

import com.flight.app.entity.Flight;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface FlightService {
    Mono<String> addFlight(Flight flight);
    
    Flux<Flight> searchFlights(String fromPlace, String toPlace, LocalDate scheduleDate);
    
    Mono<Flight> getFlightById(String flightId); 
    
    Mono<String> updateFlightInventory(Flight flight);
}
