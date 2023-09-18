package com.ecommerce.ecommercebackend.api.controller.user;

import com.ecommerce.ecommercebackend.api.model.DataChange;
import com.ecommerce.ecommercebackend.model.Address;
import com.ecommerce.ecommercebackend.model.Customer;
import com.ecommerce.ecommercebackend.model.dao.AddressDAO;
import com.ecommerce.ecommercebackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private AddressDAO addressDAO;
    private SimpMessagingTemplate simpMessagingTemplate;
    private UserService userService;

    public UserController(AddressDAO addressDAO, SimpMessagingTemplate simpMessagingTemplate, UserService userService) {
        this.addressDAO = addressDAO;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(
            @AuthenticationPrincipal Customer user, @PathVariable Long userId) {
        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressDAO.findByCustomer_Id(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> putAddress(
            @AuthenticationPrincipal Customer user,
            @PathVariable Long userId,
            @RequestBody Address address) {


        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        address.setId(null);
        Customer refUser = new Customer();
        refUser.setId(userId);
        address.setCustomer(user);
        Address savedAddress = addressDAO.save(address);
        simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/address",
                new DataChange<>(DataChange.ChangeType.INSERT, address));
        return ResponseEntity.ok(savedAddress);
    }

    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> patchAddress(
            @AuthenticationPrincipal Customer user,
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @RequestBody Address address
    ) {
        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (Objects.equals(address.getId(), addressId)) {
            Optional<Address> optionalOriginalAddress = addressDAO.findById(addressId);
            if (optionalOriginalAddress.isPresent()) {
                if (Objects.equals(optionalOriginalAddress.get().getCustomer().getId(), userId)) {
                    Customer originalUser = optionalOriginalAddress.get().getCustomer();
                    if (Objects.equals(originalUser.getId(), userId)) {
                        address.setCustomer(originalUser);
                        Address savedAddress = addressDAO.save(address);
                        simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/address",
                                new DataChange<>(DataChange.ChangeType.UPDATE, address));
                        return ResponseEntity.ok(savedAddress);
                    }
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
