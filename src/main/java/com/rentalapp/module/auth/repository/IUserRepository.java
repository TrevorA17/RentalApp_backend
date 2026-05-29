package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    User save(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    Optional<User> findByEmail(String email);
}
