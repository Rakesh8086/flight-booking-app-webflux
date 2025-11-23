package com.flight.app.service;

import com.flight.app.entity.Flight;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface FlightService {
    Mono<String> addFlight(Flight flight);

}
