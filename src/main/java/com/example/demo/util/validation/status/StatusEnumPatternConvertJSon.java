package com.example.demo.util.validation.status;

import java.io.IOException;

import com.example.demo.util.constant.StatusEnum;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StatusEnumPatternConvertJSon extends JsonDeserializer<StatusEnum> {

    @Override
    public StatusEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String value = p.getText().toUpperCase(); // Chuyển về chữ hoa để tránh lỗi nhập thường

        for (StatusEnum status : StatusEnum.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }

        // Nếu giá trị không hợp lệ, ném lỗi với message đúng format yêu cầu
        throw new IllegalArgumentException("status must match PENDING|SHIPPED|DELIVERED|CANCELLED");
    }

}
