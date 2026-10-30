package com.epam.task1.repo;

import com.epam.task1.entity.CustomerDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<CustomerDto, Integer> {
}
