package com.flight.app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "bookings") 
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class Booking {

    @Id
    private String pnr; 
    
    @NotBlank(message = "User name is required.")
    private String userName;
    
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be a valid format.")
    private String userEmail;
    
    @NotBlank(message = "Mobile number is required.")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits.")
    private String mobileNumber;
    
    @NotNull(message = "Booking date is required.")
    private LocalDateTime bookingDate;
    
    @Min(value = 1, message = "Must book at least 1 seat.")
    private Integer numberOfSeats;
    
    @NotBlank(message = "Meal choice is required.")
    private String mealOpted;
    
    private Double totalCost;

    // Relationship to Flight (Foreign Key)
    @NotNull(message = "Flight ID is required.")
    private String flightId; 
    
    @NotNull(message = "Journey date is required.")
    private LocalDate journeyDate; 

    // The Passenger list is embedded directly.
    @NotNull(message = "Passenger details are required.")
    private List<Passenger> passengers; 
}
