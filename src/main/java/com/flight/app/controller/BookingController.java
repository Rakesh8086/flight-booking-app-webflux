package com.flight.app.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.flight.app.dto.BookingRequest;
import com.flight.app.dto.PassengerDTO;
import com.flight.app.entity.Booking;
import com.flight.app.entity.Passenger;
import com.flight.app.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1.0/flight")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking/{flightId}")
    public Mono<ResponseEntity<String>> bookTicket(
            @PathVariable String flightId, 
            @Valid @RequestBody BookingRequest request) {

        Booking bookingEntity = mapBookingRequestToEntity(request);

        // returns Mono<String> (PNR)
        return bookingService.bookTicket(flightId, bookingEntity)
                .map(pnr -> new ResponseEntity<>("Ticket booked successfully. PNR: " + pnr, HttpStatus.CREATED));
    }
    
    private Booking mapBookingRequestToEntity(BookingRequest request) {
        Booking booking = new Booking();
        booking.setUserName(request.getUserName());
        booking.setUserEmail(request.getUserEmail());
        booking.setMobileNumber(request.getMobileNumber());
        booking.setMealOpted(request.getMealOpted());
        
        List<Passenger> passengers = request.getPassengers().stream()
                .map(this::mapPassengerDtoToEntity)
                .collect(Collectors.toList());
        
        booking.setPassengers(passengers); 
        
        return booking;
    }
    
    private Passenger mapPassengerDtoToEntity(PassengerDTO dto) {
        Passenger passenger = new Passenger();
        passenger.setName(dto.getName());
        passenger.setGender(dto.getGender());
        passenger.setAge(dto.getAge());
        passenger.setSeatNumber(dto.getSeatNumber());
        
        return passenger;
    }
    
    @GetMapping("/ticket/{pnr}")
    public Mono<Booking> getTicketByPnr(@PathVariable String pnr) {
    	
        return bookingService.getTicketByPnr(pnr);
    }
    
    @GetMapping("/booking/history/{emailId}")
    public Flux<Booking> getBookingHistoryByEmail(@PathVariable String emailId) {

    	return bookingService.getBookingHistoryByEmail(emailId);
    }
    
    @DeleteMapping("/booking/cancel/{pnr}")
    public Mono<ResponseEntity<String>> cancelTicket(@PathVariable String pnr) {
                return bookingService.cancelTicket(pnr)
                // thenReturn() switches from Mono<Void> to Mono<String>
                .thenReturn("Ticket with PNR " + pnr + " cancelled successfully.")
                .map(message -> new ResponseEntity<>(message, HttpStatus.OK));
    }
}
