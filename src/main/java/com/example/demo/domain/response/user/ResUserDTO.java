package com.example.demo.domain.response.user;

import java.time.Instant;

import com.example.demo.util.constant.GenderEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUserDTO {
    private long id;
    private String name;
    private String phone;
    private GenderEnum gender;
    private String email;
    private String address;
    private String avatar;
    private Instant createAt;
}
