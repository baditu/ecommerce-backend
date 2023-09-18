package com.ecommerce.ecommercebackend.service;

import com.ecommerce.ecommercebackend.api.model.LoginBody;
import com.ecommerce.ecommercebackend.api.model.PasswordResetBody;
import com.ecommerce.ecommercebackend.api.model.RegistrationBody;
import com.ecommerce.ecommercebackend.exception.EmailFailureExeception;
import com.ecommerce.ecommercebackend.exception.EmailNotFoundException;
import com.ecommerce.ecommercebackend.exception.UserAlreadyExistsException;
import com.ecommerce.ecommercebackend.exception.UserNotVerifiedException;
import com.ecommerce.ecommercebackend.model.Customer;
import com.ecommerce.ecommercebackend.model.VerificationToken;
import com.ecommerce.ecommercebackend.model.dao.CustomerDAO;
import com.ecommerce.ecommercebackend.model.dao.VerificationTokenDAO;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private CustomerDAO userDAO;

    private VerificationTokenDAO verificationTokenDAO;

    private EncryptionService encryptionService;
    private JWTService jwtService;
    private EmailService emailService;

    public UserService(CustomerDAO userDAO, VerificationTokenDAO verificationTokenDAO, EncryptionService encryptionService, JWTService jwtService, EmailService emailService) {
        this.userDAO = userDAO;
        this.verificationTokenDAO = verificationTokenDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public Customer registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureExeception {

        if(userDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent() ||
                userDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        Customer user = new Customer();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        VerificationToken verificationToken = createVerificationToken(user);
        emailService.sendVerificationEmail(verificationToken);
        return userDAO.save(user);
    }

    private VerificationToken createVerificationToken(Customer user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setCustomer(user);
        user.getVerificationTokens().add(verificationToken);

        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureExeception {
        Optional<Customer> opUser = userDAO.findByUsernameIgnoreCase(loginBody.getUsername());
        if (opUser.isPresent()) {
            Customer user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {

                if(user.isEmailVerified()) {
                    return jwtService.generateJWT(user);
                } else {
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resend = verificationTokens.size() == 0 || verificationTokens.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));

                    if(resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenDAO.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }

                    throw new UserNotVerifiedException(resend);
                }

            }
        }
        return null;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> optionalToken = verificationTokenDAO.findByToken(token);

        if(optionalToken.isPresent()) {
            VerificationToken verificationToken = optionalToken.get();
            Customer user = verificationToken.getCustomer();
            if(!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userDAO.save(user);
                verificationTokenDAO.deleteByCustomer(user);
                return true;
            }
        }
        return false;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureExeception {
        Optional<Customer> optionalCustomer = userDAO.findByEmailIgnoreCase(email);

        if(optionalCustomer.isPresent()) {
            Customer user = optionalCustomer.get();
           String token = jwtService.generatePasswordResetJWT(user);
           emailService.sendPaswordResetEmail(user, token);
        } else {
            throw new EmailNotFoundException();
        }
    }

    public void resetPassword(PasswordResetBody body) {
        String email = jwtService.getResetPasswordEmail(body.getToken());
        Optional <Customer> optionalCustomer = userDAO.findByEmailIgnoreCase(email);

        if (optionalCustomer.isPresent()) {
            Customer user = optionalCustomer.get();
            user.setPassword(encryptionService.encryptPassword(body.getPassword()));
            userDAO.save(user);
        }
    }

    public boolean userHasPermissionToUser(Customer user, Long id) {
        return user.getId() == id;
    }
}
