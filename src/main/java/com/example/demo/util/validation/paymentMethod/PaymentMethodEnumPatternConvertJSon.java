package com.example.demo.util.validation.paymentMethod;

import java.io.IOException;

import com.example.demo.util.constant.PaymentMethodEnum;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PaymentMethodEnumPatternConvertJSon extends JsonDeserializer<PaymentMethodEnum> {

    @Override
    public PaymentMethodEnum deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        String value = p.getText().toUpperCase(); // Chuyển về chữ hoa để tránh lỗi nhập thường

        for (PaymentMethodEnum item : PaymentMethodEnum.values()) {
            if (item.name().equals(value)) {
                return item;
            }
        }

        throw new IllegalArgumentException("payment method must match CREDITCARD|PAYPAL|COD");
    }

}
