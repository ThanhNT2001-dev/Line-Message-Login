package com.example.demo.domain;

import java.time.Instant;
import java.util.List;

import com.example.demo.util.constant.PaymentMethodEnum;
import com.example.demo.util.constant.StatusEnum;
import com.example.demo.util.validation.EnumPattern;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double totalPrice;

    @EnumPattern(name = "status", regexp = "PENDING|SHIPPED|DELIVERED|CANCELLED")
    private StatusEnum status;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String shippingAddress;

    @EnumPattern(name = "status", regexp = "CREDITCARD|PAYPAL|COD")
    private PaymentMethodEnum paymentMethod;

    @Column(unique = true)
    private String trackingNumber;

    private Instant orderDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderDetail> orderDetails;

    @PrePersist
    public void handleBeforeCreate() {
        this.orderDate = Instant.now();
    }
}
