package com.flight.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flight.app.entity.Booking;
import com.flight.app.entity.Flight;
import com.flight.app.exception.CancellationNotPossibleException;
import com.flight.app.exception.FlightUnavailableException;
import com.flight.app.exception.ResourceNotFoundException;
import com.flight.app.repository.BookingRepository;
import com.flight.app.service.BookingService;
import com.flight.app.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    
    @Override
    public Mono<Void> cancelTicket(String pnr) {
        return getTicketByPnr(pnr)
            .flatMap(booking -> 
                flightService.getFlightById(booking.getFlightId())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                    		"Flight not found for ID " + booking.getFlightId()
                    		)))
                    // Using tuple to carry both Booking and Flight objects forward
                    .flatMap(flight -> Mono.just(new BookingFlightTuple(booking, flight)))
            )
            .flatMap(tuple -> {
                Booking booking = tuple.booking;
                Flight flight = tuple.flight;
                
                LocalDateTime departureDateTime = booking.getJourneyDate()
                        .atTime(flight.getDepartureTime()); 
                LocalDateTime cancellationDeadline = departureDateTime.minus(24, ChronoUnit.HOURS);
                
                if(LocalDateTime.now().isAfter(cancellationDeadline)) {
                    return Mono.error(new CancellationNotPossibleException(
                        "Cancellation Not Possible due to 24 hour deadline."));
                }
                
                flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfSeats());
                return Mono.just(tuple);
            })
            // Update Inventory and Delete Booking
            .flatMap(tuple -> {
                Mono<String> updateInventory = flightService.updateFlightInventory(tuple.flight);
                Mono<Void> deleteBooking = bookingRepository.delete(tuple.booking);
                
                // Wait for both operations to complete and then return
                return Mono.when(updateInventory, deleteBooking);
            });
            
    }
    
    // helper class to carry two objects through a reactive chain
    private static class BookingFlightTuple {
        final Booking booking;
        final Flight flight;
        
        BookingFlightTuple(Booking booking, Flight flight) {
            this.booking = booking;
            this.flight = flight;
        }
    }
}
