package com.chiragbhisikar.e_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AuthenticationRequestDto {
    @NotBlank(message = "Username is required !")
    @Length(min = 8, max = 50)
//    @Email(message = "Invalid Email !")
    private String username;

    @NotBlank(message = "Password is required !")
    private String password;
}
