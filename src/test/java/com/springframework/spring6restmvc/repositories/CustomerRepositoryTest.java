package com.springframework.spring6restmvc.repositories;

import com.springframework.spring6restmvc.entities.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// If we try to autowire any controller or service, we will get an error because of the missing context
// because DataJpaTest bring only the database context and not the application context
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testSaveCustomer(){
        Customer savedCustomer = customerRepository.save(Customer.builder()
                .name("testCustomer")
                .build());

        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
    }

}