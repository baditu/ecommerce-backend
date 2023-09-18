package com.ecommerce.ecommercebackend.api.controller.command;

import com.ecommerce.ecommercebackend.model.Command;
import com.ecommerce.ecommercebackend.model.Customer;
import com.ecommerce.ecommercebackend.service.CommandService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/command")
public class CommandController {

    private CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @GetMapping
    public List<Command> getCommands(@AuthenticationPrincipal Customer user) {
        return commandService.getCommands(user);
    }
}
