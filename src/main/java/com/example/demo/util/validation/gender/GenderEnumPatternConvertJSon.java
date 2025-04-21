package com.example.demo.util.validation.gender;

import java.io.IOException;

import com.example.demo.util.constant.GenderEnum;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class GenderEnumPatternConvertJSon extends JsonDeserializer<GenderEnum>{

    // Convert JSON -> Enum
    @Override
    public GenderEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String value = p.getText().toUpperCase(); // Chuyển về chữ hoa để tránh lỗi nhập thường

        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.name().equals(value)) {
                return gender;
            }
        }

        // Nếu giá trị không hợp lệ, ném lỗi với message đúng format yêu cầu
        throw new IllegalArgumentException("gender must match MALE|FEMALE|OTHER");
    }
}
