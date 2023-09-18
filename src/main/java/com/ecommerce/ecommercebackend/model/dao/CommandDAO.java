package com.ecommerce.ecommercebackend.model.dao;

import com.ecommerce.ecommercebackend.model.Command;
import com.ecommerce.ecommercebackend.model.Customer;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CommandDAO extends ListCrudRepository<Command, Long> {
    List<Command> findByCustomer(Customer customer);


}
