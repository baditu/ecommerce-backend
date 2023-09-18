package com.ecommerce.ecommercebackend.model.dao;

import com.ecommerce.ecommercebackend.model.Product;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductDAO extends ListCrudRepository<Product, Long> {


}
