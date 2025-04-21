package com.example.demo.domain.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestLoginDTO {
    @Schema(description = "username")
    @NotBlank(message = "username cannot be blank")
    private String username;

    @Schema(description = "password")
    @NotBlank(message = "password cannot be blank")
    private String password;
}
