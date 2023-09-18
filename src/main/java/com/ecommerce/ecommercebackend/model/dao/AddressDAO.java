package com.ecommerce.ecommercebackend.model.dao;

import com.ecommerce.ecommercebackend.model.Address;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface AddressDAO  extends ListCrudRepository<Address, Long> {
    List<Address> findByCustomer_Id(Long id);

}
