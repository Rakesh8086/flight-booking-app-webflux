package com.flight.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flight.app.entity.Booking;
import com.flight.app.entity.Flight;
import com.flight.app.entity.Passenger;
import com.flight.app.exception.FlightUnavailableException;
import com.flight.app.exception.ResourceNotFoundException;
import com.flight.app.repository.BookingRepository;
import com.flight.app.service.impl.BookingServiceImpl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private FlightService flightService; 
    @InjectMocks
    private BookingServiceImpl bookingService;

    private Flight testFlight;
    private Booking testBooking;
    private List<Passenger> passengers;
    private final String testFlightId = "100";
    private final String testPNR = "CHUBBFLIGHT101ABC";

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setId(testFlightId);
        testFlight.setTotalSeats(150);
        testFlight.setAvailableSeats(10);
        testFlight.setPrice(100.00);
        testFlight.setScheduleDate(LocalDate.now().plusDays(5));
        testFlight.setDepartureTime(LocalTime.of(10, 0)); // 10 AM
        
        Passenger p1 = new Passenger();
        p1.setName("AAA");
        Passenger p2 = new Passenger();
        p2.setName("BBB");
        passengers = List.of(p1, p2); 

        testBooking = new Booking();
        testBooking.setPassengers(passengers);
        testBooking.setNumberOfSeats(passengers.size());
        testBooking.setUserEmail("AAA@example.com");
    }

    @Test
    void bookTicket_Success_InventoryDecrementedAndBookingSaved() {
        int seatsToBook = passengers.size(); 
        int initialAvailable = testFlight.getAvailableSeats(); 
        int expectedAvailable = initialAvailable - seatsToBook;
        
        when(flightService.getFlightById(testFlightId)).thenReturn(Mono.just(testFlight));
        when(flightService.updateFlightInventory(any(Flight.class))).thenReturn(Mono.just("Updated"));
        
        Booking bookedBooking = new Booking(); 
        bookedBooking.setPnr(testPNR);
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(bookedBooking));

        Mono<String> result = bookingService.bookTicket(testFlightId, testBooking);
        StepVerifier.create(result)
                // Expect the PNR to be returned
                .expectNext(testPNR) 
                .verifyComplete();

        verify(flightService, times(1)).getFlightById(testFlightId);
        verify(flightService, times(1)).updateFlightInventory(argThat(
            f -> f.getAvailableSeats().equals(expectedAvailable)
        ));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void bookTicket_Failure_FlightNotFound() {
        when(flightService.getFlightById(testFlightId)).thenReturn(Mono.empty());
        Mono<String> result = bookingService.bookTicket(testFlightId, testBooking);
        
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException && 
                                        e.getMessage().contains("not found")).verify();
        
        verify(flightService, never()).updateFlightInventory(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTicket_Failure_InsufficientSeats() {
        testFlight.setAvailableSeats(1); 
        when(flightService.getFlightById(testFlightId)).thenReturn(Mono.just(testFlight));

        Mono<String> result = bookingService.bookTicket(testFlightId, testBooking);
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof FlightUnavailableException && 
                                e.getMessage().contains("Insufficient")).verify();
        
        verify(flightService, never()).updateFlightInventory(any());
        verify(bookingRepository, never()).save(any());
    }
    

    @Test
    void getTicketByPnr_Success() {
        testBooking.setPnr(testPNR);
        when(bookingRepository.findByPnr(testPNR)).thenReturn(Mono.just(testBooking));
        Mono<Booking> found = bookingService.getTicketByPnr(testPNR);
        StepVerifier.create(found)
                .expectNext(testBooking)
                .verifyComplete();
    }

    @Test
    void getTicketByPnr_Failure_NotFound() {
        when(bookingRepository.findByPnr(anyString())).thenReturn(Mono.empty());
        Mono<Booking> found = bookingService.getTicketByPnr("NONEXISTENT");
        StepVerifier.create(found)
                .expectErrorMatches(e -> e instanceof ResourceNotFoundException && 
                            e.getMessage().contains("not found")).verify();
    }
    
    @Test
    void getBookingHistoryByEmail_Success() {
        when(bookingRepository.findByUserEmailOrderByBookingDateDesc(anyString())).thenReturn(Flux.just(testBooking));
        Flux<Booking> history = bookingService.getBookingHistoryByEmail("test@example.com");
        StepVerifier.create(history)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getBookingHistoryByEmail_Failure_NotFound_ReturnsEmptyFlux() {
        when(bookingRepository.findByUserEmailOrderByBookingDateDesc(anyString())).thenReturn(Flux.empty());
        Flux<Booking> history = bookingService.getBookingHistoryByEmail("abc@example.com");
        StepVerifier.create(history)
                .expectNextCount(0)
                .verifyComplete();
    }
    
    @Test
    void cancelTicket_Failure_BookingNotFound() {
        when(bookingRepository.findByPnr(anyString())).thenReturn(Mono.empty());
        Mono<Void> resultMono = bookingService.cancelTicket(testPNR);
        StepVerifier.create(resultMono)
                .expectErrorMatches(e -> e instanceof ResourceNotFoundException && 
                                        e.getMessage().contains("not found")).verify();
        
        verify(flightService, never()).getFlightById(any());
    }
}
