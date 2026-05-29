package com.rentalapp.module.auth.repository;

import com.rentalapp.module.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String>, IUserRepository {
}
