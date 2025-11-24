package com.flight.app.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Passenger {
    
    private String id; 

    @NotBlank(message = "Passenger name is required.")
    private String name; 
    
    @NotBlank(message = "Passenger gender is required.")
    private String gender; 
    
    @Min(value = 0, message = "Age cannot be negative.")
    private Integer age; 
    
    @NotBlank(message = "Seat number is required.")
    private String seatNumber;

    // the Passenger object is physically inside the Booking document.
}
