package com.epam.task1.service.impl;

import com.epam.task1.entity.CustomerDto;
import com.epam.task1.exception.InternalServerErrorException;
import com.epam.task1.exception.NonExistentCustomerException;
import com.epam.task1.entity.CustomerModel;
import com.epam.task1.repo.CustomerRepo;
import com.epam.task1.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo repo;

    public CustomerServiceImpl(CustomerRepo repo) {
        this.repo = repo;
    }

    @Override
    public CustomerModel get(Integer id) {
        CustomerDto dto = repo.findById(id).orElseThrow(() -> new NonExistentCustomerException(id));
        return CustomerModel.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress()).build();
    }

    @Override
    public Integer create(CustomerModel model) {
        try {
            CustomerDto dto = CustomerDto.builder()
                    .name(model.getName())
                    .address(model.getAddress()).build();
            return repo.save(dto).getId();
        } catch (Exception e) {
            throw new InternalServerErrorException("Can't store customer " + model + e.getMessage());
        }
    }

    @Override
    public Integer update(CustomerModel model) {
        try {
            CustomerDto newDto = repo.findById(model.getId())
                    .orElseGet(CustomerDto::new);
            newDto.setAddress(model.getAddress());
            newDto.setAddress(model.getAddress());
            return repo.save(newDto).getId();
        } catch (Exception e) {
            throw new InternalServerErrorException("Can't update customer " + model + e.getMessage());
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            throw new InternalServerErrorException("Can't delete customer with id " + id + e.getMessage());
        }
    }
}
