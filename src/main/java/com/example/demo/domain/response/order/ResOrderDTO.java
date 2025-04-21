package com.example.demo.domain.response.order;

import java.time.Instant;

import com.example.demo.util.constant.PaymentMethodEnum;
import com.example.demo.util.constant.StatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOrderDTO {
    private long id;
    private double totalPrice;
    private StatusEnum status;
    private String shippingAddress;
    private PaymentMethodEnum paymentMethod;
    private String trackingNumber;
    private Instant orderDate;
    private UserOrder user;

    @Getter
    @Setter
    public static class UserOrder {
        private long id;
        private String name;
    }
}
