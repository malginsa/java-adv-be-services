package com.epam.task1.service;

import com.epam.task1.entity.CustomerModel;

public interface CustomerService {
    CustomerModel get(Integer id);

    Integer create(CustomerModel customerModel);

    Integer update(CustomerModel model);

    void delete(Integer id);
}
