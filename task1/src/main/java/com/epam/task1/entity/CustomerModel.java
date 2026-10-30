package com.epam.task1.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerModel {
    private Integer id;
    private String name;
    private String address;
}
