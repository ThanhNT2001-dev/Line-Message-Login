package com.example.demo.util.constant;

import com.example.demo.util.validation.status.StatusEnumPatternConvertJSon;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = StatusEnumPatternConvertJSon.class)
public enum StatusEnum {
    PENDING, SHIPPED, DELIVERED, CANCELLED
}
