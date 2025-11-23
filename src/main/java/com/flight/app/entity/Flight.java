package com.flight.app.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "flights") 
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class Flight {

    @Id 
    private String id; 

    @NotBlank(message = "Airline name is required.")
    private String airlineName;

    @NotBlank(message = "Departure place is required.")
    private String fromPlace;

    @NotBlank(message = "Arrival place is required.")
    private String toPlace;

    @NotNull(message = "Schedule date is required.")
    private LocalDate scheduleDate;

    @NotNull(message = "Departure time is required.")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required.")
    private LocalTime arrivalTime;

    @Min(value = 0, message = "Price cannot be negative.")
    private Double price;

    @Min(value = 1, message = "Total seats must be at least 1.")
    private Integer totalSeats;

    @Min(value = 0, message = "Available seats cannot be negative.")
    private Integer availableSeats;
}