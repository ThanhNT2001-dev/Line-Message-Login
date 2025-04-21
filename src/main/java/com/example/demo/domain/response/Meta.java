package com.example.demo.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    private int page;      // Trang hiện tại
    private int pageSize;  // Số lượng bản ghi trong 1 trang
    private int pages;     // Tổng số trang
    private long total;    // Tổng số phần tử

}
