package com.example.perso.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.perso.models.LoginRequest;
import com.example.perso.models.SignupRequest;
import com.example.perso.models.SignupResponse;
import com.example.perso.models.User;
import com.example.perso.repository.LoginRepository;

@Service
public class LoginService {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String doLogin(LoginRequest request) {
        Optional<User> users = loginRepository.findByUsername(request.getUsername());

        if (users.isPresent()) {
            return "User details found";
        }

        return "User details not found";
    }

    public SignupResponse doRegister(SignupRequest request) {

        SignupResponse response = new SignupResponse();

        if (isEmailOrUsernameTaken(request.getEmail(), request.getUsername())) {
            response.setResponse("User details Already found");
            return response;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        loginRepository.save(user);

        response.setResponse("User created with id " + user.getId());

        return response;
    }

    // Méthode pour vérifier si l'email ou le nom d'utilisateur existe déjà
    public boolean isEmailOrUsernameTaken(String email, String username) {
        Optional<User> userByEmail = loginRepository.findByEmail(email);
        Optional<User> userByUsername = loginRepository.findByUsername(username);
        return userByEmail.isPresent() || userByUsername.isPresent();
    }

}
