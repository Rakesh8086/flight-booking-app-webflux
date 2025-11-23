package com.flight.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FlightSearchRequest {
    @NotBlank
    private String fromPlace;
    
    @NotBlank
    private String toPlace;
    
    @NotNull
    private LocalDate journeyDate;
}