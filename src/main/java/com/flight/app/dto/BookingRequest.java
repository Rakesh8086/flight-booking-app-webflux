package com.flight.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @NotBlank
    private String userName;

    @NotBlank
    @Email(message = "Email must be a valid format.")
    private String userEmail;
    
    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be 10 digits.")
    private String mobileNumber; 

    @NotNull
    @Pattern(regexp = "Veg|NonVeg", message = "Meal option must be 'Veg' or 'NonVeg'.")
    private String mealOpted; 
    
    @Valid // This one checks validation on the list elements on PassengerDTO
    @NotEmpty(message = "Passenger details are required for booking.")
    private List<PassengerDTO> passengers;
    
}
