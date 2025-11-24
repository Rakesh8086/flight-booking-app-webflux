package com.flight.app.service;

import com.flight.app.entity.Booking;
import reactor.core.publisher.Mono;

public interface BookingService {

    Mono<String> bookTicket(String flightId, Booking booking); 
    
    Mono<Booking> getTicketByPnr(String pnr);
}