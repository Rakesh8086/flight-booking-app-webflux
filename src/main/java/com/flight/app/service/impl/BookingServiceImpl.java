package com.flight.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flight.app.entity.Booking;
import com.flight.app.exception.FlightUnavailableException;
import com.flight.app.exception.ResourceNotFoundException;
import com.flight.app.repository.BookingRepository;
import com.flight.app.service.BookingService;
import com.flight.app.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {
    
    private final FlightService flightService;
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingServiceImpl(FlightService flightService, BookingRepository bookingRepository) {
        this.flightService = flightService;
        this.bookingRepository = bookingRepository;
    }

    private String generateUniquePNR() {
        return "CHUBBFLIGHT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public Mono<String> bookTicket(String flightId, Booking requestBooking) {
        return flightService.getFlightById(flightId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Flight with ID " + flightId + " not found.")))
            // check if seats available 
            .flatMap(flight -> {
                int seatsToBook = requestBooking.getPassengers().size();
                if(seatsToBook <= 0) {
                    return Mono.error(new IllegalArgumentException("Number of seats must be at least one."));
                }
                Integer currentAvailableSeats = flight.getAvailableSeats();              
                if(currentAvailableSeats < seatsToBook) {
                    return Mono.error(new FlightUnavailableException(
                           "Insufficient seats. Only " + currentAvailableSeats + " seats Available"));
                }

                flight.setAvailableSeats(currentAvailableSeats - seatsToBook);
                return Mono.just(flight);
            })
            // Update Flight Inventory
            .flatMap(updatedFlight -> {
                Mono<String> updateMono = flightService.updateFlightInventory(updatedFlight);
                return updateMono.thenReturn(updatedFlight); 
            })
            // save booking details in repo
            .flatMap(updatedFlight -> {
                String pnr = generateUniquePNR();
                requestBooking.setPnr(pnr);
                
                requestBooking.setFlightId(flightId);
                requestBooking.setBookingDate(LocalDateTime.now());
                requestBooking.setNumberOfSeats(requestBooking.getPassengers().size());
                requestBooking.setTotalCost(updatedFlight.getPrice() * requestBooking.getNumberOfSeats());
                requestBooking.setJourneyDate(updatedFlight.getScheduleDate());
                
                return bookingRepository.save(requestBooking).map(Booking::getPnr);
            });
    }
    
    @Override
    public Mono<Booking> getTicketByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
            			"Ticket with PNR " + pnr + " not found."
            		)));
    }
    
    @Override
    public Flux<Booking> getBookingHistoryByEmail(String emailId) {
        return bookingRepository.findByUserEmailOrderByBookingDateDesc(emailId);
    }
}
