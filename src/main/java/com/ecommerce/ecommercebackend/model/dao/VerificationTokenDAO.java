package com.ecommerce.ecommercebackend.model.dao;

import com.ecommerce.ecommercebackend.model.Customer;
import com.ecommerce.ecommercebackend.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByCustomer(Customer customer);
}
