package com.springframework.spring6restmvc.repositories;


import com.springframework.spring6restmvc.entities.Beer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TODO
@Testcontainers
@SpringBootTest
@ActiveProfiles("localmysql")
public class MySqlTest {

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    // for not to use the values from localmysql profile properties file
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @Autowired
    DataSource dataSource;

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testListBeers() {
        List<Beer> beers = beerRepository.findAll();

        assertThat(beers.size()).isGreaterThan(0);
    }
}
