package com.springframework.spring6restmvc.services;

import com.springframework.spring6restmvc.model.CustomerDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {

    List<CustomerDTO> getAllCustomers();

    Optional<CustomerDTO> getCustomerById(UUID id);

    CustomerDTO saveCustomer(CustomerDTO customer);

    void updateById(UUID customerId, CustomerDTO customer);

    void deleteById(UUID customerId);

    void patchCustomerById(UUID customerId, CustomerDTO customer);
}
