package com.flight.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDTO {

    @NotBlank
    private String name;    

    @NotBlank
    private String gender;  

    @NotNull
    @Min(value = 0)
    private Integer age;    
    
    @NotBlank
    private String seatNumber;

}
