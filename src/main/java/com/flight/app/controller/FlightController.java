package com.flight.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flight.app.entity.Flight;
import com.flight.app.service.FlightService;

import jakarta.validation.Valid;
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

}
