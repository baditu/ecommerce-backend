package com.ecommerce.ecommercebackend.service;

import com.ecommerce.ecommercebackend.model.Command;
import com.ecommerce.ecommercebackend.model.Customer;
import com.ecommerce.ecommercebackend.model.dao.CommandDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandService {
    private CommandDAO commandDAO;

    public CommandService(CommandDAO commandDAO) {
        this.commandDAO = commandDAO;
    }

    public List<Command> getCommands(Customer user) {
        return commandDAO.findByCustomer(user);
    }

}
