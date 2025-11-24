package com.flight.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flight.app.entity.Flight;
import com.flight.app.repository.FlightRepository;
import com.flight.app.service.impl.FlightServiceImpl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;
    @InjectMocks
    private FlightServiceImpl flightService;
    private Flight testFlight;
    private final String testFlightPNR = "CHUBBFLIGHT101ABC";

    @BeforeEach
    void setUp() {
        testFlight = new Flight();
        testFlight.setId(testFlightPNR); 
        testFlight.setAirlineName("Air India");
        testFlight.setFromPlace("DEL");
        testFlight.setToPlace("BOM");
        testFlight.setScheduleDate(LocalDate.of(2025, 12, 25));
        testFlight.setDepartureTime(LocalTime.of(8, 0));
        testFlight.setArrivalTime(LocalTime.of(10, 0));
        testFlight.setPrice(5000.00);
        testFlight.setTotalSeats(150);
        testFlight.setAvailableSeats(150);
    }

    @Test
    void addFlight_Success_ReturnsFlightIdAndInitializesAvailableSeats() {
        when(flightRepository.save(any(Flight.class))).thenReturn(Mono.just(testFlight));
        Mono<String> result = flightService.addFlight(testFlight);
        StepVerifier.create(result)
                // PNR is expected to be returned
                .expectNext(testFlightPNR) 
                .verifyComplete();

        verify(flightRepository, times(1)).save(testFlight);
    }
    
    @Test
    void addFlight_ValidationFailure_SamePlace() {
        testFlight.setToPlace("DEL");
        Mono<String> result = flightService.addFlight(testFlight);
        StepVerifier.create(result)
                .expectErrorMessage("Departure and arrival places cannot be the same.")
                .verify();

        verify(flightRepository, never()).save(any(Flight.class));
    }
    
    @Test
    void addFlight_ValidationFailure_ArrivalBeforeDeparture() {
        testFlight.setArrivalTime(LocalTime.of(7, 0));
        Mono<String> result = flightService.addFlight(testFlight);        
        StepVerifier.create(result)
                .expectErrorMessage("Arrival time must be after the departure time.")
                .verify();

        verify(flightRepository, never()).save(any(Flight.class));
    }
    
    @Test
    void searchFlights_Success_ReturnsMatchingFlights() {
        String from = "DEL";
        String to = "BOM";
        LocalDate date = LocalDate.of(2025, 12, 25);
        
        when(flightRepository.findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                from, to, date, 0)).thenReturn(Flux.just(testFlight));
        Flux<Flight> results = flightService.searchFlights(from, to, date);
        StepVerifier.create(results)
                // Expect exactly one element
                .expectNext(testFlight) 
                .verifyComplete();
        
        verify(flightRepository, times(1)).findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                from, to, date, 0);
    }

    @Test
    void searchFlights_EmptyResult_NoMatchingFlights() {
        String from = "DEL";
        String to = "CHE";
        LocalDate date = LocalDate.of(2025, 12, 25);

        when(flightRepository.findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                from, to, date, 0)).thenReturn(Flux.empty());
        Flux<Flight> results = flightService.searchFlights(from, to, date);
        StepVerifier.create(results)
                // Expect no elements
                .expectNextCount(0) 
                .verifyComplete();
    }
   
    @Test
    void getFlightById_Success_ReturnsFlight() {
        when(flightRepository.findById(testFlightPNR)).thenReturn(Mono.just(testFlight));
        Mono<Flight> found = flightService.getFlightById(testFlightPNR);
        StepVerifier.create(found)
                // Check the returned object matches
                .expectNext(testFlight) 
                .verifyComplete();
    }

    @Test
    void getFlightById_NotFound_ReturnsEmptyMono() {
        String notFoundId = "CHUBBFLIGHT101010";
        when(flightRepository.findById(notFoundId)).thenReturn(Mono.empty());
        Mono<Flight> found = flightService.getFlightById(notFoundId);
        StepVerifier.create(found)
                // Expect no elements
                .verifyComplete();
    }
        
    @Test
    void updateFlightInventory_Success() {
        testFlight.setAvailableSeats(50);
        when(flightRepository.save(testFlight)).thenReturn(Mono.just(testFlight));
        Mono<String> result = flightService.updateFlightInventory(testFlight);
        StepVerifier.create(result)
                .expectNextMatches(msg -> msg.contains(testFlightPNR) && msg.contains("updated"))
                .verifyComplete();
        
        verify(flightRepository, times(1)).save(testFlight);
    }
}
