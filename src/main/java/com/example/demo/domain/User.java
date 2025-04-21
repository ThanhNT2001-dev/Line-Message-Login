package com.example.demo.domain;

import java.time.Instant;
import java.util.List;

import com.example.demo.util.constant.GenderEnum;
import com.example.demo.util.validation.EnumPattern;
import com.example.demo.util.validation.phone.PhoneNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @PhoneNumber
    private String phone;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @EnumPattern(name = "gender", regexp = "MALE|FEMALE|OTHER")
    private GenderEnum gender;

    @Email(message = "Email invalid format")
    private String email;

    private String address;

    private String avatar;

    private Instant createAt;

    private Instant updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Cart cart;

    @PrePersist
    public void handleBeforeCreate() {
        this.createAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
