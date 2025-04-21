package com.example.demo.domain.response.order;

import com.example.demo.util.constant.StatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOrderStatusDTO {
    private long id;
    private StatusEnum status;
}
