package com.ecommerce.ecommercebackend.model.dao;

import com.ecommerce.ecommercebackend.model.Customer;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface CustomerDAO extends ListCrudRepository <Customer, Long> {
    Optional<Customer> findByUsernameIgnoreCase(String username);

    Optional<Customer> findByEmailIgnoreCase(String email);


}
