package com.epam.task1.service;

import com.epam.task1.entity.CustomerModel;
import org.apache.coyote.BadRequestException;

public interface CustomerService {
    CustomerModel get(Long Long);

    Long create(CustomerModel customerModel) throws BadRequestException;

    CustomerModel update(CustomerModel model);

    void delete(Long Long);
}
