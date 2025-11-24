package com.flight.app.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort; 
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flight.app.dto.FlightSearchRequest;
import com.flight.app.entity.Flight;
import com.flight.app.repository.FlightRepository;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// testimg on actual db instead of using mockdb
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FlightControllerTest {

    private WebTestClient webTestClient;
    @LocalServerPort 
    private int port; 
    @Autowired
    private FlightRepository flightRepository;

    private Flight testFlight1;
    private FlightSearchRequest validSearchRequest;
    private final LocalDate testDate = LocalDate.now().plusDays(7);
    private final String baseURI = "/api/v1.0/flight";

    @BeforeEach
    void setup() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port + baseURI)
                .build();
        
        flightRepository.deleteAll().block();

        testFlight1 = new Flight();
        testFlight1.setId(UUID.randomUUID().toString());
        testFlight1.setAirlineName("Air India");
        testFlight1.setFromPlace("DEL");
        testFlight1.setToPlace("BOM");
        testFlight1.setScheduleDate(testDate);
        testFlight1.setDepartureTime(LocalTime.of(8, 0));
        testFlight1.setArrivalTime(LocalTime.of(10, 0));
        testFlight1.setPrice(5000.00);
        testFlight1.setTotalSeats(150);
        testFlight1.setAvailableSeats(10); 

        flightRepository.saveAll(Mono.just(testFlight1)).blockLast();

        validSearchRequest = new FlightSearchRequest();
        validSearchRequest.setFromPlace("DEL");
        validSearchRequest.setToPlace("BOM");
        validSearchRequest.setJourneyDate(testDate);
    }

    @AfterEach
    void cleanUp() {
        flightRepository.deleteAll().block();
    }
        
    @Test
    void addFlightInventory_Success_Returns201AndId() {
        Flight newFlight = new Flight();
        newFlight.setAirlineName("Vistara");
        newFlight.setFromPlace("DEL");
        newFlight.setToPlace("CCU");
        newFlight.setTotalSeats(200);
        newFlight.setScheduleDate(LocalDate.of(2025, 12, 25));
        newFlight.setDepartureTime(LocalTime.of(8, 0));
        newFlight.setArrivalTime(LocalTime.of(10, 0));
        newFlight.setPrice(5000.00);

        webTestClient.post().uri("/airline/inventory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newFlight), Flight.class)
                .exchange()
                .expectStatus().isCreated() 
                .expectBody(String.class)
                .value(response -> {
                    if(!response.contains("successfully")) {
                        throw new AssertionError("Falied to add flight");
                    }
                });
    }

    @Test
    void searchFlights_Success_Returns200AndMatchingFlights() {
        webTestClient.post().uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validSearchRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Flight.class)
                .hasSize(1);
    }

    @Test
    void searchFlights_NoResults_Returns404NotFound() {
        FlightSearchRequest nonExistentSearch = new FlightSearchRequest();
        nonExistentSearch.setFromPlace("BOM");
        nonExistentSearch.setToPlace("HYD");
        nonExistentSearch.setJourneyDate(testDate);

        webTestClient.post().uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(nonExistentSearch)
                .exchange()
                .expectStatus().isNotFound() 
                .expectBody()
                .jsonPath("$.reason").doesNotExist();
    }
}
