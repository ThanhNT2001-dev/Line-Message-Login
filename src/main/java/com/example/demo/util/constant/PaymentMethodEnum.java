package com.example.demo.util.constant;

import com.example.demo.util.validation.paymentMethod.PaymentMethodEnumPatternConvertJSon;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = PaymentMethodEnumPatternConvertJSon.class)
public enum PaymentMethodEnum {
    CREDITCARD, PAYPAL, COD
}
