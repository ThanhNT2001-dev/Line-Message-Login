package com.example.demo.domain.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserTokenDTO {
    
    @Schema(description = "id")
    private long id;

    @Schema(description = "email")
    private String email;

    @Schema(description = "name")
    private String name;
}
