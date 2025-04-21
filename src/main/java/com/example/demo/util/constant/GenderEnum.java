package com.example.demo.util.constant;

import com.example.demo.util.validation.gender.GenderEnumPatternConvertJSon;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = GenderEnumPatternConvertJSon.class)
public enum GenderEnum {
    MALE, FEMALE, OTHER
}
