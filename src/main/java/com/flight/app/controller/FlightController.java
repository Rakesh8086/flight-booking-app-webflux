package com.flight.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.flight.app.dto.FlightSearchRequest;
import com.flight.app.entity.Flight;
import com.flight.app.service.FlightService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1.0/flight")
public class FlightController {

    private final FlightService flightService;

    @Autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping("/airline/inventory/add")
    public Mono<ResponseEntity<String>> addFlightInventory(@Valid @RequestBody Flight flight) {
        
        return flightService.addFlight(flight)
                .map(flightId -> new ResponseEntity<>("Flight added successfully with ID: " 
                + flightId, HttpStatus.CREATED));
    
    }
    
    @PostMapping("/search")
    public Mono<ResponseEntity<List<Flight>>> searchFlights(@Valid @RequestBody 
    		FlightSearchRequest request) {
        
        Flux<Flight> matchingFlights = flightService.searchFlights(
                request.getFromPlace(),
                request.getToPlace(),
                request.getJourneyDate()
        );

        return matchingFlights
                .collectList()
                .flatMap(flights -> {
                    if (flights.isEmpty()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No flights found matching the criteria"
                        ));
                    }
                    return Mono.just(new ResponseEntity<>(flights, HttpStatus.OK));
                });
    }
}
