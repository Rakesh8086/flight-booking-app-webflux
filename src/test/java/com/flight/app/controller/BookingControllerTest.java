package com.flight.app.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flight.app.entity.Booking;
import com.flight.app.entity.Flight;
import com.flight.app.entity.Passenger;
import com.flight.app.repository.BookingRepository;
import com.flight.app.repository.FlightRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port; 
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private FlightRepository flightRepository;

    private Flight testFlight;
    private Booking mockBooking;
    private final String testPNR = "TESTPNR123";
    private final String testEmail = "user@test.com";
    private String testFlightId; 

    private final String baseURI = "/api/v1.0/flight";

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port + baseURI)
                .build();
                
        bookingRepository.deleteAll().block();
        flightRepository.deleteAll().block();

        testFlightId = UUID.randomUUID().toString();
        testFlight = new Flight();
        testFlight.setId(testFlightId);
        testFlight.setAirlineName("Air India");
        testFlight.setFromPlace("DEL");
        testFlight.setToPlace("BOM");
        testFlight.setTotalSeats(100);
        testFlight.setAvailableSeats(10);
        testFlight.setPrice(10000.00);
        
        LocalDate futureDate = LocalDate.now().plusDays(10); // so we dont get cancellation failed
        testFlight.setScheduleDate(futureDate);
        testFlight.setDepartureTime(LocalTime.of(10, 0));
        
        flightRepository.save(testFlight).block();

        Passenger p1 = new Passenger();
        p1.setName("AAA");
        p1.setAge(34);
        p1.setSeatNumber("1A");
        p1.setGender("Male");

        mockBooking = new Booking();
        mockBooking.setPnr(testPNR); 
        mockBooking.setFlightId(testFlightId); 
        mockBooking.setJourneyDate(futureDate);
        mockBooking.setUserEmail(testEmail);
        mockBooking.setBookingDate(LocalDateTime.now());
        mockBooking.setNumberOfSeats(1);
        mockBooking.setTotalCost(10000.00);
        mockBooking.setPassengers(List.of(p1));
    }

    @AfterEach
    void cleanUp() {
        bookingRepository.deleteAll().block();
        flightRepository.deleteAll().block();
    }
    
    @Test
    void getTicketByPnr_Success_Returns200Ok() {
        bookingRepository.save(mockBooking).block();
        webTestClient.get().uri("/ticket/{pnr}", testPNR)
                .exchange()
                .expectStatus().isOk() // HTTP 200
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.pnr").isEqualTo(testPNR)
                .jsonPath("$.userEmail").isEqualTo(testEmail);
    }

    
    @Test
    void getBookingHistory_Success_Returns200Ok() {
        bookingRepository.save(mockBooking).block();

        webTestClient.get().uri("/booking/history/{emailId}", testEmail)
                .exchange()
                .expectStatus().isOk() 
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Booking.class)
                .hasSize(1);
    }    
}